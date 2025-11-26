package com.example.liftium.ui.screens

import android.Manifest
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.graphics.Canvas
import android.os.IBinder
import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.liftium.R
import com.example.liftium.model.MapViewModel
import com.example.liftium.services.LocationService
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
//import com.google.android.gms.maps.model.MapStyleOptions
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapType
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch
import androidx.compose.ui.graphics.Color
import android.app.Application
import android.graphics.Bitmap
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.ui.graphics.toArgb
import androidx.lifecycle.ViewModelProvider
import androidx.core.graphics.createBitmap
import androidx.compose.foundation.isSystemInDarkTheme
import com.google.android.gms.maps.model.MapStyleOptions


// Estado para manejar el servicio de ubicación
private data class ServiceState(
    val bound: Boolean = false,
    val service: LocationService? = null
)

@RequiresApi(Build.VERSION_CODES.Q)
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun GymFinderScreen(modifier: Modifier = Modifier) {
    //  ESTADO Y CONTEXTO
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // ViewModel
    val application = (LocalContext.current.applicationContext as Application)

    val viewModel: MapViewModel = viewModel(
        factory = ViewModelProvider.AndroidViewModelFactory.getInstance(application)
    )
    val uiState by viewModel.uiState.collectAsState()

    // Estado del servicio de ubicación
    var serviceState by remember { mutableStateOf(ServiceState()) }

    // Estado del mapa (para mover la cámara)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(uiState.userLocation, 12f)
    }

    // Estado de la UI
    var isNavigating by remember { mutableStateOf(false) }
    var mapType by remember { mutableStateOf(MapType.NORMAL) }
    var isMapLoaded by remember { mutableStateOf(false) }

    //  PERMISOS
    val locationPermissions = rememberMultiplePermissionsState(
        permissions = listOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    )

    // 2. TEMA DÍA/NOCHE
    val mapStyle = if (isSystemInDarkTheme()) {
        // Usa el estilo oscuro que ya tenías
        MapStyleOptions.loadRawResourceStyle(context, R.raw.map_style)
    } else {
        // Usa el nuevo estilo claro
        MapStyleOptions.loadRawResourceStyle(context, R.raw.map_style_light)
    }

    //  EFECTOS

    // 1. Efecto para manejar la conexión al LocationService
    DisposableEffect(locationPermissions.allPermissionsGranted) {
        if (!locationPermissions.allPermissionsGranted) {
            return@DisposableEffect onDispose {}
        }
        val connection = object : ServiceConnection {
            override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
                val binder = service as LocationService.LocationBinder
                serviceState = ServiceState(bound = true, service = binder.getService())
                Log.d("GymFinderScreen", "Servicio conectado")
            }
            override fun onServiceDisconnected(name: ComponentName?) {
                serviceState = ServiceState(bound = false, service = null)
                Log.d("GymFinderScreen", "Servicio desconectado")
            }
        }
        Intent(context, LocationService::class.java).also { intent ->
            context.bindService(intent, connection, Context.BIND_AUTO_CREATE)
        }
        onDispose {
            Log.d("GymFinderScreen", "Desconectando servicio")
            context.unbindService(connection)
            serviceState = ServiceState(bound = false, service = null)
        }
    }

    // 2. Efecto para reaccionar a las actualizaciones de ubicación del servicio
    LaunchedEffect(serviceState.service, isNavigating, isMapLoaded) {
        if (!isMapLoaded) return@LaunchedEffect
        val service = serviceState.service ?: return@LaunchedEffect
        service.locationFlow
            .filterNotNull()
            .collect { location ->
                viewModel.updateUserLocation(location)
                if (cameraPositionState.position.target != uiState.userLocation) {
                    cameraPositionState.animate(
                        CameraUpdateFactory.newLatLngZoom(uiState.userLocation, 17f)
                    )
                }
                if (isNavigating) {
                    cameraPositionState.animate(
                        CameraUpdateFactory.newCameraPosition(
                            CameraPosition(uiState.userLocation, 18f, 30f, location.bearing)
                        )
                    )
                }
            }
    }

    // 3. Efecto para mover la cámara cuando el ViewModel actualiza la ubicación
    LaunchedEffect(uiState.userLocation, isMapLoaded) {
        if (!isNavigating && isMapLoaded) {
            cameraPositionState.move(
                CameraUpdateFactory.newLatLng(uiState.userLocation)
            )
        }
    }

    // 4. Efecto para mostrar errores en el Snackbar
    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.clearErrorMessage()
        }
    }

    //  INTERFAZ DE USUARIO (UI)
    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { padding ->
        if (!locationPermissions.allPermissionsGranted) {
            PermissionRequestScreen(
                onGrantClick = { locationPermissions.launchMultiplePermissionRequest() }
            )
        } else {
            // Contenedor principal del mapa y botones
            Box(Modifier.fillMaxSize().padding(padding)) {

                // Mapa de Google
                GoogleMap(
                    modifier = Modifier.fillMaxSize(),
                    cameraPositionState = cameraPositionState,
                    onMapLoaded = { isMapLoaded = true },
                    properties = MapProperties(
                        mapType = mapType,
                        mapStyleOptions = mapStyle,
                        isBuildingEnabled = true,
                        isMyLocationEnabled = false
                    ),
                    uiSettings = MapUiSettings(
                        zoomControlsEnabled = false,
                        myLocationButtonEnabled = false
                    )
                ) {
                    //  Marcadores y Polilíneas

                    // 1. Marcador del Usuario (Corredor)
                    Marker(
                        state = MarkerState(position = uiState.userLocation),
                        icon = bitmapDescriptorFromVector(
                            context,
                            R.drawable.ic_runner,
                            tintColor = Color(0xFFFF9800)
                        ),
                        rotation = uiState.userBearing,
                        anchor = Offset(0.5f, 0.5f),
                        flat = true,
                        zIndex = 100f
                    )

                    // 2. Marcadores de Gimnasios
                    uiState.gyms.forEach { gym ->
                        val isSelected = uiState.selectedGym?.id == gym.id

                        Marker(
                            state = MarkerState(position = gym.toLatLng()),
                            title = gym.name,
                            snippet = "Toca para trazar ruta",
                            icon = bitmapDescriptorFromVector(context,
                                R.drawable.fitness_center_24px,
                                tintColor = Color(0xFFFF9800)
                            ),
                            zIndex = if (isSelected) 10f else 0f,
                            onInfoWindowClick = {
                                viewModel.getDirectionsToGym(gym)
                            }
                        )
                    }

                    // 3. Polilínea de la Ruta
                    if (uiState.routeData.routePoints.isNotEmpty()) {
                        Polyline(
                            points = uiState.routeData.routePoints,
                            color = Color(0xFFFF9800), // Naranja
                            width = 15f
                        )
                    }
                }

                //  Botones Flotantes
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalAlignment = Alignment.End
                ) {
                    FloatingActionButton(onClick = { viewModel.fetchGymsAroundUser() }) {
                        Icon(Icons.Default.Search, contentDescription = "Buscar Gimnasios Cercanos")
                    }
                    FloatingActionButton(onClick = {
                        scope.launch {
                            if (isMapLoaded) {
                                cameraPositionState.animate(
                                    CameraUpdateFactory.newLatLngZoom(uiState.userLocation, 17f)
                                )
                            }
                        }
                    }) {
                        Icon(Icons.Default.GpsFixed, contentDescription = "Centrar en Ubicación")
                    }
                    if (uiState.selectedGym != null) {
                        FloatingActionButton(
                            onClick = {
                                isNavigating = !isNavigating
                                val intent = Intent(context, LocationService::class.java).apply {
                                    action = if (isNavigating) LocationService.ACTION_START else LocationService.ACTION_STOP
                                }
                                context.startService(intent)
                                if (!isNavigating) {
                                    viewModel.clearRoute()
                                }
                            },
                            containerColor = if (isNavigating) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                        ) {
                            Icon(
                                if (isNavigating) Icons.Default.Close else Icons.Default.Navigation,
                                contentDescription = if (isNavigating) "Detener Navegación" else "Iniciar Navegación"
                            )
                        }
                    }
                }
                FloatingActionButton(
                    onClick = {
                        mapType = if (mapType == MapType.NORMAL) MapType.SATELLITE else MapType.NORMAL
                    },
                    modifier = Modifier.align(Alignment.TopEnd).padding(16.dp)
                ) {
                    Icon(Icons.Default.Layers, contentDescription = "Tipo de Mapa")
                }
                if (uiState.isLoadingGyms || uiState.isLoadingRoute) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                AnimatedVisibility(
                    visible = uiState.routeData.routePoints.isNotEmpty() && !isNavigating,
                    modifier = Modifier.align(Alignment.BottomCenter),
                    enter = slideInVertically(initialOffsetY = { it }),
                    exit = slideOutVertically(targetOffsetY = { it })
                ) {
                    RouteInfoCard(
                        gymName = uiState.selectedGym?.name ?: "Gimnasio",
                        eta = uiState.routeData.eta,
                        distance = uiState.routeData.distance,
                        onStartClick = {
                            isNavigating = true
                            val intent = Intent(context, LocationService::class.java).apply {
                                action = LocationService.ACTION_START
                            }
                            context.startService(intent)
                        },
                        onCloseClick = {
                            viewModel.clearRoute()
                        }
                    )
                }
            }
        }
    }
}

// Composable para la tarjeta de información de ruta
@Composable
private fun RouteInfoCard(
    gymName: String,
    eta: String,
    distance: String,
    onStartClick: () -> Unit,
    onCloseClick: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp),
        elevation = CardDefaults.cardElevation(8.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = gymName,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = onCloseClick, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Default.Close, contentDescription = "Cerrar Ruta")
                }
            }
            Spacer(Modifier.size(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                InfoChip(
                    icon = Icons.Default.Timer,
                    text = eta
                )
                InfoChip(
                    icon = Icons.AutoMirrored.Filled.DirectionsRun,
                    text = distance
                )
            }
            Spacer(Modifier.size(16.dp))
            Button(
                onClick = onStartClick,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Navigation, contentDescription = null)
                Spacer(Modifier.size(8.dp))
                Text("INICIAR NAVEGACIÓN")
            }
        }
    }
}

// Pequeño chip para mostrar ETA y Distancia
@Composable
private fun InfoChip(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.width(4.dp))
        Text(text, fontSize = 16.sp, fontWeight = FontWeight.Medium)
    }
}

// Composable para pedir permisos
@Composable
private fun PermissionRequestScreen(onGrantClick: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            "Permiso de Ubicación Necesario",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.size(12.dp))
        Text(
            "Liftium necesita acceso a tu ubicación precisa para encontrar gimnasios cercanos y mostrarte cómo llegar."
        )
        Spacer(Modifier.size(24.dp))
        Button(onClick = onGrantClick) {
            Text("Conceder Permiso")
        }
    }
}

// Helper para convertir un icono Vector de Drawable en un BitmapDescriptor para el mapa
private fun bitmapDescriptorFromVector(
    context: Context,
    vectorResId: Int,
    tintColor: Color? = null
): BitmapDescriptor {
    val vectorDrawable = ContextCompat.getDrawable(context, vectorResId)!!
    vectorDrawable.setBounds(0, 0, vectorDrawable.intrinsicWidth, vectorDrawable.intrinsicHeight)

    tintColor?.let { color ->
        vectorDrawable.setTint(color.toArgb())
    }

    val bitmap = Bitmap.createBitmap(
        vectorDrawable.intrinsicWidth,
        vectorDrawable.intrinsicHeight,
        Bitmap.Config.ARGB_8888
    )
    val canvas = Canvas(bitmap)
    vectorDrawable.draw(canvas)
    return BitmapDescriptorFactory.fromBitmap(bitmap)
}