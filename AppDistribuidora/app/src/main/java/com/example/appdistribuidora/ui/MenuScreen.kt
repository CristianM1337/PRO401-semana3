package com.example.appdistribuidora.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun MenuScreen(
    onGoToCatalogo: () -> Unit,
    onGoToDespacho: () -> Unit,
    onGoToTemperatura: () -> Unit,
    onLogout: () -> Unit
) {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),

        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Text(
            "Menú principal",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onGoToCatalogo,
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
        ) {
            Text("🛒 Ver Catálogo de Productos")
        }

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = onGoToDespacho,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Ir a cálculo de despacho")
        }

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = onGoToTemperatura,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("🌡 Gestión de temperatura")
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedButton(
            onClick = onLogout,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Cerrar sesión")
        }
    }
}