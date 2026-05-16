package com.example.appdistribuidora.ui.screens

import android.media.RingtoneManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.google.firebase.database.*

@Composable
fun TemperatureScreen(
    onBack: () -> Unit
) {

    // Variable que almacena la temperatura recibida desde Firebase en Fahrenheit.
    var temperaturaF by remember {
        mutableStateOf<Double?>(null)
    }

    // Variable utilizada para mostrar errores de Firebase o conexión.
    var error by remember {
        mutableStateOf<String?>(null)
    }

    // Contexto actual de la aplicación.
    // Se utiliza para vibración y reproducción de sonido.
    val context = LocalContext.current

    // Temperaturas mínima y máxima configurables por el usuario.
    var temperaturaMin by remember {
        mutableStateOf("-5")
    }

    var temperaturaMax by remember {
        mutableStateOf("5")
    }

    // Se ejecuta una sola vez al cargar la pantalla.
    // Escucha cambios en Firebase en tiempo real.
    LaunchedEffect(Unit) {

        val database = FirebaseDatabase.getInstance()

        // Ruta donde se almacena la temperatura del camión.
        val referencia = database.getReference("camion/temperaturaF")

        referencia.addValueEventListener(object : ValueEventListener {

            // Se ejecuta cuando Firebase recibe nuevos datos.
            override fun onDataChange(snapshot: DataSnapshot) {

                // Obtiene el valor de temperatura desde Firebase.
                temperaturaF = snapshot.getValue(Double::class.java)
            }

            // Se ejecuta si ocurre un error de lectura.
            override fun onCancelled(databaseError: DatabaseError) {
                error = databaseError.message
            }
        })
    }

    // Conversión automática de Fahrenheit a Celsius.
    val temperaturaC = temperaturaF?.let {
        (it - 32) * 5 / 9
    }

    // Conversión segura de los límites ingresados por el usuario.
    // Si falla la conversión, usa valores por defecto.
    val minPermitida = temperaturaMin.toDoubleOrNull() ?: -5.0
    val maxPermitida = temperaturaMax.toDoubleOrNull() ?: 5.0

    // Determina si la temperatura está fuera del rango permitido.
    val fueraDeRango = temperaturaC != null &&
            (temperaturaC < minPermitida || temperaturaC > maxPermitida)

    // Se ejecuta automáticamente cuando cambia el estado de fueraDeRango.
    LaunchedEffect(fueraDeRango) {

        if (fueraDeRango) {

            // ---------------- VIBRACIÓN ----------------

            val vibrator = context.getSystemService(Vibrator::class.java)

            // Vibración moderna para Android Oreo o superior.
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

                vibrator?.vibrate(
                    VibrationEffect.createOneShot(
                        1000,
                        VibrationEffect.DEFAULT_AMPLITUDE
                    )
                )

            } else {

                // Compatibilidad con versiones antiguas de Android.
                @Suppress("DEPRECATION")
                vibrator?.vibrate(1000)
            }

            // ---------------- SONIDO ----------------

            // Obtiene el sonido de alarma predeterminado.
            val notification =
                RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                    ?: RingtoneManager.getDefaultUri(
                        RingtoneManager.TYPE_NOTIFICATION
                    )

            // Reproduce el sonido de alerta.
            val ringtone = RingtoneManager.getRingtone(context, notification)
            ringtone.play()
        }
    }

    // ---------------- INTERFAZ ----------------

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),

        horizontalAlignment = Alignment.CenterHorizontally,

        verticalArrangement = Arrangement.Center
    ) {

        // Título principal de la pantalla.
        Text(
            text = "Gestión de temperatura",
            style = MaterialTheme.typography.headlineSmall
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Muestra mensaje de error si Firebase falla.
        if (error != null) {

            Text(
                text = "Error: $error",
                color = MaterialTheme.colorScheme.error
            )

        } else if (temperaturaF == null) {

            // Indicador de carga mientras se espera Firebase.
            CircularProgressIndicator()

            Spacer(modifier = Modifier.height(12.dp))

            Text("Cargando temperatura...")

        } else {

            // Temperatura original recibida desde Firebase.
            Text(
                text = "Temperatura Firebase: %.2f °F"
                    .format(temperaturaF)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Temperatura convertida a Celsius.
            Text(
                text = "Temperatura convertida: %.2f °C"
                    .format(temperaturaC)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Campo para ingresar temperatura mínima permitida.
        OutlinedTextField(
            value = temperaturaMin,

            onValueChange = {
                temperaturaMin = it
            },

            label = {
                Text("Temperatura mínima °C")
            },

            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Campo para ingresar temperatura máxima permitida.
        OutlinedTextField(
            value = temperaturaMax,

            onValueChange = {
                temperaturaMax = it
            },

            label = {
                Text("Temperatura máxima °C")
            },

            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Tarjeta visual de alerta cuando la temperatura sale del rango.
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

        // Botón para regresar al menú principal.
        OutlinedButton(
            onClick = onBack,

            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Volver al menú")
        }
    }
}