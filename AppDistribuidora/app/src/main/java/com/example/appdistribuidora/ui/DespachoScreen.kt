
package com.example.appdistribuidora.ui

import android.util.Log
import androidx.activity.ComponentActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.appdistribuidora.logic.calcularCostoDespacho
import com.example.appdistribuidora.logic.calcularDistancia
import com.example.appdistribuidora.logic.obtenerUbicacionActual
import com.google.firebase.database.FirebaseDatabase
import java.text.NumberFormat
import java.util.Locale

private val ColorPrimary = Color(0xFF16A34A)
private val ColorPrimaryLight = Color(0xFFDCFCE7)
private val ColorBorder = Color(0xFFE5E7EB)
private val ColorSurface = Color(0xFFF9FAFB)
private val ColorTextSecondary = Color(0xFF6B7280)

// Datos de ejemplo del mockup (pantalla 4 del informe)
data class ItemPedido(val nombre: String, val cantidad: Int, val precio: Double)

@Composable
fun DespachoScreen(
    totalCompraInicial: Double?,
    activity: ComponentActivity,
    onBack: () -> Unit
) {
    var montoIngresado by remember { mutableStateOf(totalCompraInicial?.toInt()?.toString() ?: "") }
    var resultadoDistancia by remember { mutableStateOf<Double?>(null) }
    var resultadoCosto by remember { mutableStateOf<Double?>(null) }
    var cargando by remember { mutableStateOf(false) }
    var errorMsg by remember { mutableStateOf("") }
    val formatoPeso = NumberFormat.getCurrencyInstance(Locale("es", "CL"))

    // Simulamos la lista de productos del mockup cuando viene del catálogo
    // En producción esto vendría del estado del carrito
    val itemsResumen = listOf(
        ItemPedido("Mix de mariscos congelados", 2, 10000.0),
        ItemPedido("Lomo vetado al vacío 1kg", 1, 12000.0),
        ItemPedido("Salmón congelado", 3, 21000.0),
        ItemPedido("Aceite maravilla 1lt", 2, 5000.0)
    )
    val subtotalProductos = totalCompraInicial ?: itemsResumen.sumOf { it.precio }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ColorSurface)
            .statusBarsPadding()
    ) {
        // TopBar estilo mockup
        Surface(color = Color.White) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Volver", tint = Color(0xFF374151))
                }
                Spacer(modifier = Modifier.width(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(ColorPrimary),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("❄", fontSize = 16.sp)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "DistribuFood",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = Color(0xFF111827)
                        )
                        Text(
                            text = "Cálculo de despacho",
                            fontSize = 10.sp,
                            color = ColorTextSecondary
                        )
                    }
                }
            }
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            // ── CARD: Resumen del pedido ──
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(14.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(RoundedCornerShape(7.dp))
                                .background(ColorPrimaryLight),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("📦", fontSize = 14.sp)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "Resumen del pedido",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp,
                                color = Color(0xFF111827)
                            )
                            Text(
                                text = "${itemsResumen.size} productos seleccionados",
                                fontSize = 11.sp,
                                color = ColorTextSecondary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    itemsResumen.forEach { item ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = item.nombre,
                                    fontSize = 13.sp,
                                    color = Color(0xFF374151)
                                )
                                Text(
                                    text = "x${item.cantidad}",
                                    fontSize = 11.sp,
                                    color = ColorTextSecondary
                                )
                            }
                            Text(
                                text = "$${item.precio.toInt().formatCLP()}",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF111827)
                            )
                        }
                    }

                    Divider(color = ColorBorder, thickness = 0.5.dp, modifier = Modifier.padding(vertical = 8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Subtotal productos",
                            fontWeight = FontWeight.Medium,
                            fontSize = 14.sp,
                            color = Color(0xFF374151)
                        )
                        Text(
                            text = "$${subtotalProductos.toInt().formatCLP()}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = Color(0xFF111827)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // ── CARD: Cálculo de despacho con mapa (placeholder) ──
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(14.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(RoundedCornerShape(7.dp))
                                .background(Color(0xFFEFF6FF)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = Color(0xFF2563EB),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "Cálculo de despacho",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp,
                                color = Color(0xFF111827)
                            )
                            Text(
                                text = "Basado en tu ubicación actual",
                                fontSize = 11.sp,
                                color = ColorTextSecondary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Si ingresa monto manualmente (entrada desde menú sin carrito)
                    if (totalCompraInicial == null) {
                        OutlinedTextField(
                            value = montoIngresado,
                            onValueChange = { v -> if (v.all { it.isDigit() }) montoIngresado = v },
                            label = { Text("Ingrese monto de compra ($)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = ColorPrimary,
                                unfocusedBorderColor = ColorBorder,
                                focusedLabelColor = ColorPrimary
                            )
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                    }

                    // Placeholder mapa (azul redondeado)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFFBFDBFE)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = Color(0xFF1D4ED8),
                                modifier = Modifier.size(32.dp)
                            )
                            Text(
                                text = if (resultadoDistancia != null)
                                    "${"%.1f".format(resultadoDistancia)} km"
                                else "GPS activo",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = Color(0xFF1D4ED8)
                            )
                        }
                    }

                    // Resultado distancia calculada
                    if (resultadoDistancia != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Distancia calculada",
                                fontSize = 13.sp,
                                color = ColorTextSecondary
                            )
                            Text(
                                text = "${"%.1f".format(resultadoDistancia)} km",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF111827)
                            )
                        }
                    }

                    // Error
                    if (errorMsg.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Surface(
                            color = Color(0xFFFFF1F2),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = errorMsg,
                                modifier = Modifier.padding(10.dp),
                                fontSize = 12.sp,
                                color = Color(0xFFDC2626)
                            )
                        }
                    }

                    // Resultado costo
                    if (resultadoCosto != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Surface(
                            color = if (resultadoCosto == 0.0) ColorPrimaryLight else Color(0xFFFFF7ED),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (resultadoCosto == 0.0) "🎉" else "🚚",
                                    fontSize = 20.sp
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = if (resultadoCosto == 0.0) "¡Despacho GRATIS!" else "Costo de despacho",
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 14.sp,
                                        color = if (resultadoCosto == 0.0) ColorPrimaryDark else Color(0xFF92400E)
                                    )
                                    if (resultadoCosto!! > 0) {
                                        Text(
                                            text = formatoPeso.format(resultadoCosto),
                                            fontSize = 13.sp,
                                            color = Color(0xFF92400E)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        // Botón inferior fijo: Confirmar Pedido
        Surface(color = Color.White, shadowElevation = 4.dp) {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                Button(
                    onClick = {
                        val montoCompra = montoIngresado.toIntOrNull()
                        if (montoCompra == null || montoCompra <= 0) {
                            errorMsg = "Por favor, ingresa un monto válido mayor a $0"
                            return@Button
                        }
                        errorMsg = ""
                        cargando = true

                        obtenerUbicacionActual(
                            activity = activity,
                            onLocationReceived = { latUsuario, lonUsuario ->
                                cargando = false

                                val database = FirebaseDatabase.getInstance()
                                val ref = database.getReference("ubicaciones")
                                val datos = mapOf(
                                    "latitud" to latUsuario,
                                    "longitud" to lonUsuario,
                                    "timestamp" to System.currentTimeMillis()
                                )
                                ref.push().setValue(datos)
                                    .addOnSuccessListener {
                                        Log.d("APP_DESPACHO", "Ubicación guardada en Firebase")
                                    }
                                    .addOnFailureListener { e ->
                                        Log.e("APP_DESPACHO", "Error Firebase", e)
                                    }

                                val latBodega = -33.4372
                                val lonBodega = -70.6506
                                val distanciaKm = calcularDistancia(latUsuario, lonUsuario, latBodega, lonBodega)
                                val costoDespacho = calcularCostoDespacho(montoCompra, distanciaKm)

                                resultadoDistancia = distanciaKm
                                resultadoCosto = costoDespacho

                                Log.d("APP_DESPACHO", "Distancia: ${"%.2f".format(distanciaKm)} km | Costo: $costoDespacho")
                            },
                            onError = {
                                cargando = false
                                errorMsg = "No se pudo obtener la ubicación. Activa el GPS e intenta nuevamente."
                            }
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = ColorPrimary),
                    enabled = !cargando
                ) {
                    if (cargando) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text(
                        text = if (cargando) "Calculando..." else "Confirmar Pedido",
                        fontWeight = FontWeight.SemiBold
                    )
                }

                TextButton(
                    onClick = onBack,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "< Volver al Catálogo",
                        color = ColorTextSecondary,
                        fontSize = 13.sp
                    )
                }
            }
        }
    }
}

private val ColorPrimaryDark = Color(0xFF15803D)

private fun Int.formatCLP(): String = String.format("%,d", this).replace(',', '.')
 
