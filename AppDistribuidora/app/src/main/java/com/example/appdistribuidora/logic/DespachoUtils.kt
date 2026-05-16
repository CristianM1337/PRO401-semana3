package com.example.appdistribuidora.logic

import android.annotation.SuppressLint
import android.location.Location
import android.util.Log
import androidx.activity.ComponentActivity
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority

// Calcula la distancia entre dos puntos geográficos usando latitud y longitud.
// El resultado se entrega en kilómetros.
fun calcularDistancia(
    lat1: Double,
    lon1: Double,
    lat2: Double,
    lon2: Double
): Double {
    // Punto de origen, correspondiente a la ubicación actual del usuario.
    val origen = Location("origen").apply {
        latitude = lat1
        longitude = lon1
    }

    // Punto de destino, correspondiente a la ubicación de la distribuidora.
    val destino = Location("destino").apply {
        latitude = lat2
        longitude = lon2
    }

    // distanceTo entrega el resultado en metros, por eso se divide en 1000.
    return origen.distanceTo(destino) / 1000.0
}

// Calcula el costo del despacho según las reglas del negocio.
// - Compras desde $50.000 y hasta 20 km: despacho gratis.
// - Compras entre $25.000 y $49.999: $150 por kilómetro.
// - Compras menores a $25.000: $300 por kilómetro.
fun calcularCostoDespacho(montoCompra: Int, distanciaKm: Double): Double {
    return when {
        montoCompra >= 50000 && distanciaKm <= 20 -> 0.0
        montoCompra in 25000..49999 -> distanciaKm * 150
        else -> distanciaKm * 300
    }
}

// Obtiene la ubicación actual del dispositivo mediante los servicios de Google.
// Se usa SuppressLint porque el permiso se solicita previamente desde MainActivity.
@SuppressLint("MissingPermission")
fun obtenerUbicacionActual(
    activity: ComponentActivity,
    onLocationReceived: (Double, Double) -> Unit,
    onError: () -> Unit
) {
    // Cliente encargado de acceder a la ubicación del dispositivo.
    val fusedLocationClient = LocationServices.getFusedLocationProviderClient(activity)

    // Solicita la ubicación actual con alta precisión.
    fusedLocationClient.getCurrentLocation(
        Priority.PRIORITY_HIGH_ACCURACY,
        null
    )
        .addOnSuccessListener { location ->
            if (location != null) {
                // Si la ubicación existe, se envían latitud y longitud al callback.
                onLocationReceived(location.latitude, location.longitude)
            } else {
                // Si no se obtiene ubicación, se informa el error.
                Log.d("APP_DESPACHO", "Ubicación no disponible")
                onError()
            }
        }
        .addOnFailureListener {
            // Manejo de error cuando falla el servicio de ubicación.
            Log.d("APP_DESPACHO", "Error al obtener ubicación")
            onError()
        }
}