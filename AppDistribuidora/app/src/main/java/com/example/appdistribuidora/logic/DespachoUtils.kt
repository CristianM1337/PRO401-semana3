package com.example.appdistribuidora.logic

import android.annotation.SuppressLint
import android.location.Location
import android.util.Log
import androidx.activity.ComponentActivity
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority

fun calcularDistancia(
    lat1: Double,
    lon1: Double,
    lat2: Double,
    lon2: Double
): Double {
    val origen = Location("origen").apply {
        latitude = lat1
        longitude = lon1
    }

    val destino = Location("destino").apply {
        latitude = lat2
        longitude = lon2
    }

    return origen.distanceTo(destino) / 1000.0
}

fun calcularCostoDespacho(montoCompra: Int, distanciaKm: Double): Double {
    return when {
        montoCompra >= 50000 && distanciaKm <= 20 -> 0.0
        montoCompra in 25000..49999 -> distanciaKm * 150
        else -> distanciaKm * 300
    }
}

@SuppressLint("MissingPermission")
fun obtenerUbicacionActual(
    activity: ComponentActivity,
    onLocationReceived: (Double, Double) -> Unit,
    onError: () -> Unit
) {
    val fusedLocationClient = LocationServices.getFusedLocationProviderClient(activity)

    fusedLocationClient.getCurrentLocation(
        Priority.PRIORITY_HIGH_ACCURACY,
        null
    )
        .addOnSuccessListener { location ->
            if (location != null) {
                onLocationReceived(location.latitude, location.longitude)
            } else {
                Log.d("APP_DESPACHO", "Ubicación no disponible")
                onError()
            }
        }
        .addOnFailureListener {
            Log.d("APP_DESPACHO", "Error al obtener ubicación")
            onError()
        }
}