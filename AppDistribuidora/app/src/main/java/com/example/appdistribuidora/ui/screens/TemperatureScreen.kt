
package com.example.appdistribuidora.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.firebase.database.*
import androidx.compose.ui.platform.LocalContext
import android.media.RingtoneManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator

@Composable
fun TemperatureScreen(
    onBack: () -> Unit
) {

    var temperaturaF by remember { mutableStateOf<Double?>(null) }
    var error by remember { mutableStateOf<String?>(null) }

    val context = LocalContext.current

    var temperaturaMin by remember { mutableStateOf("-5") }
    var temperaturaMax by remember { mutableStateOf("5") }

    LaunchedEffect(Unit) {
        val database = FirebaseDatabase.getInstance()
        val referencia = database.getReference("temperatura")

        referencia.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                temperaturaF = snapshot.getValue(Double::class.java)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                error = databaseError.message
            }
        })
    }

    val temperaturaC = temperaturaF?.let {
        (it - 32) * 5 / 9

    }
    val minPermitida = temperaturaMin.toDoubleOrNull() ?: -5.0
    val maxPermitida = temperaturaMax.toDoubleOrNull() ?: 5.0

    val fueraDeRango = temperaturaC != null &&
            (temperaturaC < minPermitida || temperaturaC > maxPermitida)

    LaunchedEffect(fueraDeRango) {
        if (fueraDeRango) {

            // Vibración
            val vibrator = context.getSystemService(Vibrator::class.java)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(
                    VibrationEffect.createOneShot(
                        1000,
                        VibrationEffect.DEFAULT_AMPLITUDE
                    )
                )
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(1000)
            }

            // Sonido de alarma/notificación
            val notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

            val ringtone = RingtoneManager.getRingtone(context, notification)
            ringtone.play()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

        Text(
            text = "Gestión de temperatura",
            style = MaterialTheme.typography.headlineSmall
        )

        Spacer(modifier = Modifier.height(24.dp))

        if (error != null) {
            Text(
                text = "Error: $error",
                color = MaterialTheme.colorScheme.error
            )
        } else if (temperaturaF == null) {
            CircularProgressIndicator()

            Spacer(modifier = Modifier.height(12.dp))

            Text("Cargando temperatura...")
        } else {
            Text(
                text = "Temperatura Firebase: %.2f °F".format(temperaturaF)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Temperatura convertida: %.2f °C".format(temperaturaC)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = temperaturaMin,
            onValueChange = { temperaturaMin = it },
            label = { Text("Temperatura mínima °C") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = temperaturaMax,
            onValueChange = { temperaturaMax = it },
            label = { Text("Temperatura máxima °C") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(24.dp))

        if (fueraDeRango) {

            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {

                Text(
                    text = "⚠ ALERTA: Temperatura fuera del rango permitido",
                    modifier = Modifier.padding(16.dp),
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedButton(
            onClick = onBack,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Volver al menú")
        }
    }
}