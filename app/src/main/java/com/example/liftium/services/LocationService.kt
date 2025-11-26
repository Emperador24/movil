package com.example.liftium.services

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import com.example.liftium.R // Importa los recursos (para el icono)
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

// Servicio para rastrear la ubicación del usuario en primer plano
class LocationService : Service() {

    private val binder = LocationBinder()
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback

    // Flujo de datos para emitir la ubicación
    // Usamos SharedFlow para emitir a todos los colectores (la UI)
    private val _locationFlow = MutableSharedFlow<Location>(replay = 1)
    val locationFlow = _locationFlow.asSharedFlow()

    companion object {
        const val ACTION_START = "LocationService.ACTION_START"
        const val ACTION_STOP = "LocationService.ACTION_STOP"
        private const val NOTIFICATION_CHANNEL_ID = "liftium_location_channel"
        private const val NOTIFICATION_ID = 42 // Un ID cualquiera para la notificación
        private const val LOCATION_UPDATE_INTERVAL_MS = 1500L // 1.5 segundos
        private const val FASTEST_UPDATE_INTERVAL_MS = 1000L // 1 segundo
    }

    // El Binder que permite a la UI (ViewModel/Screen) conectarse a este servicio
    inner class LocationBinder : Binder() {
        // Devuelve esta instancia del servicio para que los clientes puedan llamar
        // a sus métodos públicos, como el locationFlow
        fun getService(): LocationService = this@LocationService
    }

    // Se llama cuando la UI se "ata" (bind) al servicio
    override fun onBind(intent: Intent): IBinder {
        return binder
    }

    override fun onCreate() {
        super.onCreate()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // Configura cómo queremos las actualizaciones
        locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY, // Máxima precisión (para navegación)
            LOCATION_UPDATE_INTERVAL_MS
        )
            .setWaitForAccurateLocation(true)
            .setMinUpdateIntervalMillis(FASTEST_UPDATE_INTERVAL_MS)
            .build()

        // Define qué hacer cuando llega una nueva ubicación
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult.lastLocation?.let { location ->
                    // Emite la nueva ubicación al Flow
                    // tryEmit no suspende, es seguro llamarlo aquí
                    _locationFlow.tryEmit(location)
                    // Log para depuración
                    Log.d("LocationService", "Nueva ubicación: ${location.latitude}, ${location.longitude}, Bearing: ${location.bearing}")
                }
            }
        }
    }

    // Se llama cuando se inicia el servicio (con startService)
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                startForegroundService()
                startLocationUpdates()
            }
            ACTION_STOP -> {
                stopLocationUpdates()
                stopSelf() // Detiene el servicio
            }
        }
        // No reiniciar el servicio si el sistema lo mata
        return START_NOT_STICKY
    }

    // Inicia las actualizaciones de ubicación
    private fun startLocationUpdates() {
        // Comprueba los permisos (aunque ya deberían estar dados por la UI)
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Log.e("LocationService", "Faltan permisos de ubicación para iniciar updates")
            return
        }

        Log.d("LocationService", "Iniciando actualizaciones de ubicación")
        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper() // Looper principal
        )
    }

    // Detiene las actualizaciones
    private fun stopLocationUpdates() {
        Log.d("LocationService", "Deteniendo actualizaciones de ubicación")
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    // Inicia el servicio en primer plano y muestra la notificación
    private fun startForegroundService() {
        // 1. Crear el canal de notificación (solo para Android 8.0+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                "Navegación de Gimnasio",
                NotificationManager.IMPORTANCE_LOW // Poca importancia (no hace sonido)
            )
            val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }

        // 2. Construir la notificación
        val notification: Notification = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setContentTitle("Navegación activa")
            .setContentText("Siguiendo la ruta al gimnasio...")
            // Usamos un icono de fitness que ya tienes en res/drawable
            .setSmallIcon(R.drawable.fitness_center_24px)
            .setOngoing(true) // No se puede descartar
            .build()

        // 3. Iniciar el servicio en primer plano
        startForeground(NOTIFICATION_ID, notification)
    }

    override fun onDestroy() {
        super.onDestroy()
        // Asegurarse de detener las actualizaciones si el servicio es destruido
        stopLocationUpdates()
    }
}