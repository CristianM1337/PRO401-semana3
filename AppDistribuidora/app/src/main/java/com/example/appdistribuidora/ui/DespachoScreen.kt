package com.example.appdistribuidora.ui

import android.util.Log
import androidx.activity.ComponentActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.appdistribuidora.logic.calcularCostoDespacho
import com.example.appdistribuidora.logic.calcularDistancia
import com.example.appdistribuidora.logic.obtenerUbicacionActual
import com.google.firebase.database.FirebaseDatabase
import java.text.NumberFormat
import java.util.Locale

@Composable
fun DespachoScreen(
    totalCompraInicial: Double?,
    activity: ComponentActivity,
    onBack: () -> Unit
) {
    var montoIngresado by remember { mutableStateOf(totalCompraInicial?.toInt()?.toString() ?: "") }
    var resultadoTexto by remember { mutableStateOf("") }
    val formatoPeso = NumberFormat.getCurrencyInstance(Locale("es", "CL"))

    val customTextSelectionColors = TextSelectionColors(
        handleColor = Color(0xFF14B8A6),
        backgroundColor = Color(0xFF5EEAD4)
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .imePadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Spacer(modifier = Modifier.height(80.dp))

            Text(
                text = "Cálculo de despacho por distancia GPS",
                color = Color(0xFF14B8A6),
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.height(24.dp))

            CompositionLocalProvider(
                LocalTextSelectionColors provides customTextSelectionColors
            ) {
                OutlinedTextField(
                    value = montoIngresado,
                    onValueChange = { nuevoValor ->
                        if (nuevoValor.all { it.isDigit() }) {
                            montoIngresado = nuevoValor
                        }
                    },
                    label = { Text("Ingrese monto de compra") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    readOnly = totalCompraInicial != null,
                    modifier = Modifier.fillMaxWidth(0.9f),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF14B8A6),
                        unfocusedBorderColor = Color(0xFF5EEAD4),
                        focusedLabelColor = Color(0xFF0F766E),
                        unfocusedLabelColor = Color.Black,
                        cursorColor = Color(0xFF0F766E),
                        focusedTextColor = Color.Black,
                        unfocusedTextColor = Color.Black
                    )
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            Button(
                onClick = {
                    val montoCompra = montoIngresado.toIntOrNull()

                    if (montoCompra == null || montoCompra <= 0) {
                        resultadoTexto = "Por favor, ingrese un monto válido mayor a $0"
                        Log.d("APP_DESPACHO", "Entrada inválida en monto de compra")
                    } else {
                        resultadoTexto = "Calculando despacho..."

                        obtenerUbicacionActual(
                            activity = activity,
                            onLocationReceived = { latUsuario, lonUsuario ->

                                val database = FirebaseDatabase.getInstance()
                                val ref = database.getReference("ubicaciones")

                                val datos = mapOf(
                                    "latitud" to latUsuario,
                                    "longitud" to lonUsuario,
                                    "timestamp" to System.currentTimeMillis()
                                )

                                ref.push().setValue(datos)
                                    .addOnSuccessListener {
                                        Log.d("APP_DESPACHO", "Ubicación guardada correctamente en Firebase")
                                    }
                                    .addOnFailureListener { error ->
                                        Log.e("APP_DESPACHO", "Error al guardar ubicación en Firebase", error)
                                        resultadoTexto = "No se pudo guardar la ubicación en Firebase. Intente nuevamente."
                                    }

                                val latBodega = -33.4372
                                val lonBodega = -70.6506

                                val distanciaKm = calcularDistancia(
                                    latUsuario,
                                    lonUsuario,
                                    latBodega,
                                    lonBodega
                                )

                                val costoDespacho = calcularCostoDespacho(montoCompra, distanciaKm)

                                val mensajeCosto = if (costoDespacho == 0.0) {
                                    "Despacho GRATIS 🎉"
                                } else {
                                    "Costo de despacho: ${formatoPeso.format(costoDespacho)}"
                                }

                                resultadoTexto = buildString {
                                    appendLine("📦 Monto de compra: ${formatoPeso.format(montoCompra)}")
                                    appendLine("📍 Distancia a bodega: ${"%.2f".format(distanciaKm)} km")
                                    appendLine("🚚 $mensajeCosto")
                                    appendLine("☁️ Ubicación registrada en Firebase ")
                                }

                                Log.d("APP_DESPACHO", "Monto compra: $montoCompra")
                                Log.d("APP_DESPACHO", "Lat usuario: $latUsuario")
                                Log.d("APP_DESPACHO", "Lon usuario: $lonUsuario")
                                Log.d("APP_DESPACHO", "Distancia: ${"%.2f".format(distanciaKm)} km")
                                Log.d("APP_DESPACHO", "Costo: ${formatoPeso.format(costoDespacho)}")
                            },
                            onError = {
                                resultadoTexto =
                                    "No se pudo obtener la ubicación. Activa el GPS e intenta nuevamente."
                            }
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth(0.6f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF14B8A6)
                )
            ) {
                Text("Calcular despacho", color = Color.White)
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedButton(
                onClick = onBack,
                modifier = Modifier.fillMaxWidth(0.6f)
            ) {
                Text("Volver")
            }

            if (resultadoTexto.isNotEmpty()) {
                Spacer(modifier = Modifier.height(28.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .background(
                            color = Color(0xFF5EEAD4),
                            shape = RoundedCornerShape(20.dp)
                        )
                        .padding(20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = resultadoTexto,
                        color = Color.Black
                    )
                }
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}