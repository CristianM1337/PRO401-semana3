
package com.example.appdistribuidora.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.firebase.database.*

@Composable
fun TemperatureScreen(
    onBack: () -> Unit
) {

    var temperaturaF by remember { mutableStateOf<Double?>(null) }
    var error by remember { mutableStateOf<String?>(null) }

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

        OutlinedButton(
            onClick = onBack,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Volver al menú")
        }
    }
}