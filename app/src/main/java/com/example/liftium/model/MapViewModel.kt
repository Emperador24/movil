package com.example.liftium.model

import android.app.Application
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

// Muevo la data class del Gym (antes era 'private' en GymFinderScreen)
data class GymPoi(val id: Long, val name: String?, val lat: Double, val lon: Double) {
    // Helper para convertirlo a LatLng de Google Maps
    fun toLatLng(): LatLng = LatLng(lat, lon)
}

// El estado de la UI del mapa
data class MapUiState(
    // Ubicación por defecto (Bogotá). Se actualizará con el GPS.
    val userLocation: LatLng = LatLng(4.60971, -74.08175),
    val userBearing: Float = 0f, // Orientación del usuario (para la flecha)
    val gyms: List<GymPoi> = emptyList(), // Lista de gimnasios cercanos
    val routeData: DirectionsData = DirectionsData(), // Contiene puntos, ETA y distancia
    val isLoadingGyms: Boolean = false, // Muestra spinner al buscar gyms
    val isLoadingRoute: Boolean = false, // Muestra spinner al trazar ruta
    val errorMessage: String? = null,
    val selectedGym: GymPoi? = null // El gym al que estamos navegando
)

// El ViewModel. Hereda de AndroidViewModel para poder acceder al Context
// y leer la API Key del Manifest.
class MapViewModel(application: Application) : AndroidViewModel(application) {

    // Flujo de datos privados
    private val _uiState = MutableStateFlow(MapUiState())
    // Flujo de datos público (solo lectura) para la UI
    // CORRECCIÓN: El tipo es StateFlow y se usa la función .asStateFlow()
    val uiState: StateFlow<MapUiState> = _uiState.asStateFlow()

    // Variable para guardar la API key
    private var apiKey: String = ""

    init {
        // Al iniciar, leemos la API Key del AndroidManifest.xml
        loadApiKey()
    }

    // Lee la API Key que pusiste en AndroidManifest.xml
    private fun loadApiKey() {
        try {
            val app: Application = getApplication()
            val ai: ApplicationInfo = app.packageManager.getApplicationInfo(
                app.packageName,
                PackageManager.GET_META_DATA
            )
            val bundle = ai.metaData
            apiKey = bundle.getString("com.google.android.geo.API_KEY") ?: ""
            if (apiKey.isEmpty() || apiKey == "TU_API_KEY_VA_AQUI") {
                Log.e("MapViewModel", "API Key no encontrada en AndroidManifest.xml")
                _uiState.update { it.copy(errorMessage = "API Key no configurada.") }
            }
        } catch (e: Exception) {
            Log.e("MapViewModel", "Error al leer API Key", e)
            _uiState.update { it.copy(errorMessage = "Error al leer API Key.") }
        }
    }

    // --- Funciones de Ubicación ---

    // Esta función será llamada por el futuro LocationService
    fun updateUserLocation(location: android.location.Location) {
        _uiState.update {
            it.copy(
                userLocation = LatLng(location.latitude, location.longitude),
                userBearing = location.bearing // La orientación de la flecha
            )
        }

        // (Opcional) Si estamos en una ruta, re-calcula la distancia/tiempo restante
        // Por ahora lo dejamos simple.
    }

    // --- Funciones de Gimnasios (Overpass) ---

    // Inicia la búsqueda de gimnasios
    fun fetchGymsAroundUser(radiusMeters: Int = 2000) {
        // Ejecuta en una corutina del ViewModel
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingGyms = true, errorMessage = null) }
            val center = _uiState.value.userLocation
            try {
                // Usamos la lógica de Overpass que movimos aquí
                val gyms = fetchOverpassGyms(center, radiusMeters)
                _uiState.update { it.copy(isLoadingGyms = false, gyms = gyms) }
            } catch (e: Exception) {
                Log.e("MapViewModel", "Overpass error", e)
                _uiState.update {
                    it.copy(isLoadingGyms = false, errorMessage = e.message ?: "Error de red")
                }
            }
        }
    }

    // --- Funciones de Ruta (Directions API) ---

    // Llama al repositorio para obtener la ruta
    fun getDirectionsToGym(gym: GymPoi) {
        // No intentes si no hay API key
        if (apiKey.isEmpty() || apiKey == "TU_API_KEY_VA_AQUI") {
            _uiState.update { it.copy(errorMessage = "API Key no válida.") }
            return
        }

        viewModelScope.launch {
            _uiState.update {
                it.copy(isLoadingRoute = true, selectedGym = gym, errorMessage = null)
            }

            val origin = _uiState.value.userLocation
            val destination = gym.toLatLng()

            // Llama al repositorio que creamos en el paso anterior
            val directions = DirectionsRepository.getDirections(apiKey, origin, destination)

            if (directions != null) {
                // Ruta encontrada, actualiza el estado
                _uiState.update {
                    it.copy(isLoadingRoute = false, routeData = directions)
                }
            } else {
                // Ruta no encontrada
                _uiState.update {
                    it.copy(
                        isLoadingRoute = false,
                        errorMessage = "No se pudo encontrar una ruta."
                    )
                }
            }
        }
    }

    // Limpia la ruta dibujada
    fun clearRoute() {
        _uiState.update {
            it.copy(
                routeData = DirectionsData(), // Resetea los datos de la ruta
                selectedGym = null,
                isLoadingRoute = false
            )
        }
    }

    // Limpia mensajes de error
    fun clearErrorMessage() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    // --- Lógica de Overpass (movida desde GymFinderScreen) ---

    // Busca gyms (leisure=fitness_centre) alrededor de (lat,lon).
    private suspend fun fetchOverpassGyms(center: LatLng, radiusMeters: Int): List<GymPoi> {
        val query = """
            [out:json][timeout:25];
            (
              node["leisure"="fitness_centre"](around:$radiusMeters,${center.latitude},${center.longitude});
              way["leisure"="fitness_centre"](around:$radiusMeters,${center.latitude},${center.longitude});
              relation["leisure"="fitness_centre"](around:$radiusMeters,${center.latitude},${center.longitude});
            );
            out center;
        """.trimIndent()
        val url = URL("https://overpass-api.de/api/interpreter")

        // Esta parte se ejecuta en el hilo IO (Internet)
        // CORRECCIÓN: 'withContext' ahora se importa correctamente
        return withContext(Dispatchers.IO) {
            val conn = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                doOutput = true
                setRequestProperty("Content-Type", "application/x-www-form-urlencoded")
                connectTimeout = 15000
                readTimeout = 20000
            }
            val body = "data=" + URLEncoder.encode(query, "UTF-8")
            BufferedWriter(OutputStreamWriter(conn.outputStream, Charsets.UTF_8)).use { it.write(body) }
            val code = conn.responseCode
            val sb = StringBuilder()
            val reader = BufferedReader(conn.inputStream.reader())
            reader.useLines { lines -> lines.forEach { sb.append(it) } }
            if (code !in 200..299) throw RuntimeException("Error de Overpass: HTTP $code")

            // Llama a la función de parseo
            parseOverpass(sb.toString())
        }
    }

    // Parseo básico (sin libs extra) del JSON de Overpass
    private fun parseOverpass(json: String): List<GymPoi> {
        val out = mutableListOf<GymPoi>()
        val root = JSONObject(json)
        val arr = root.optJSONArray("elements") ?: return out
        for (i in 0 until arr.length()) {
            val el = arr.getJSONObject(i)
            val id = el.optLong("id")
            val tags = el.optJSONObject("tags")
            val name = tags?.optString("name")?.takeIf { it.isNotBlank() } ?: tags?.optString("brand")
            var lat = el.optDouble("lat", Double.NaN)
            var lon = el.optDouble("lon", Double.NaN)
            if (lat.isNaN() || lon.isNaN()) {
                val center = el.optJSONObject("center")
                if (center != null) {
                    lat = center.optDouble("lat", Double.NaN)
                    lon = center.optDouble("lon", Double.NaN)
                }
            }
            if (!lat.isNaN() && !lon.isNaN()) out += GymPoi(id, name, lat, lon)
        }
        return out
    }
}