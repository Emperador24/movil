package com.example.liftium.model

// Importa el modelo de LatLng de Google Maps (el que usa el Composable)
import com.google.android.gms.maps.model.LatLng
// Importa las clases de la API de Directions (la que llamamos por internet)
import com.google.maps.DirectionsApi
import com.google.maps.GeoApiContext
import com.google.maps.model.TravelMode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException

// Data class para guardar el resultado de la API de Directions
data class DirectionsData(
    val routePoints: List<LatLng> = emptyList(), // Los puntos para dibujar la línea
    val eta: String = "", // Tiempo estimado de llegada (ej. "15 mins")
    val distance: String = "" // Distancia total (ej. "5.2 km")
)

// Un objeto (Singleton) que maneja la llamada a la API
object DirectionsRepository {

    // Esta es una función suspendida, se debe llamar desde una corutina (ViewModel)
    // Se ejecuta en el hilo de IO (Internet) para no bloquear la UI
    suspend fun getDirections(apiKey: String, origin: LatLng, destination: LatLng): DirectionsData? {
        return withContext(Dispatchers.IO) {
            try {
                // 1. Configura el contexto de la API con tu clave
                val context = GeoApiContext.Builder()
                    .apiKey(apiKey)
                    .build()

                // 2. Crea la petición
                val request = DirectionsApi.newRequest(context)
                    .origin(com.google.maps.model.LatLng(origin.latitude, origin.longitude))
                    .destination(com.google.maps.model.LatLng(destination.latitude, destination.longitude))
                    .mode(TravelMode.DRIVING) // Modo de viaje (coche)

                // 3. Ejecuta la llamada (sincrónica, pero dentro de Dispatchers.IO)
                val result = request.await()

                // 4. Procesa la respuesta
                if (result.routes.isNotEmpty()) {
                    val route = result.routes[0] // Tomamos la primera ruta
                    val leg = route.legs[0] // El primer tramo

                    // 5. Decodifica la polilínea (la línea de la ruta)
                    // La API devuelve un string codificado, decodePath lo convierte en lista de LatLng
                    val decodedPath = route.overviewPolyline.decodePath() // Esto da List<com.google.maps.model.LatLng>

                    // 6. Convierte los puntos al formato que entiende el mapa de Compose
                    val routePoints = decodedPath.map {
                        // Convertimos de com.google.maps.model.LatLng a com.google.android.gms.maps.model.LatLng
                        LatLng(it.lat, it.lng)
                    }

                    // 7. Obtiene el tiempo (ETA) y la distancia
                    val eta = leg.duration.humanReadable
                    val distance = leg.distance.humanReadable

                    // 8. Devuelve nuestro objeto con los datos listos
                    DirectionsData(routePoints = routePoints, eta = eta, distance = distance)
                } else {
                    // No se encontraron rutas
                    null
                }
            } catch (e: IOException) {
                // Error de red o de API
                e.printStackTrace()
                null
            } catch (e: Exception) {
                // Otro error
                e.printStackTrace()
                null
            }
        }
    }
}