
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
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState

// ── Paleta de colores del tema visual ──────────────────────────────────────────
// Todas las constantes de color se agrupan aquí arriba para facilitar el
// mantenimiento y evitar referencias a variables declaradas más abajo.
private val ColorPrimary      = Color(0xFF16A34A) // Verde principal (botones, íconos activos)
private val ColorPrimaryDark  = Color(0xFF15803D) // Verde oscuro (texto sobre fondo claro)
private val ColorPrimaryLight = Color(0xFFDCFCE7) // Verde pastel (fondos de chip/badge)
private val ColorBorder       = Color(0xFFE5E7EB) // Gris claro (bordes de tarjetas)
private val ColorSurface      = Color(0xFFF9FAFB) // Fondo general de la pantalla
private val ColorTextSecondary= Color(0xFF6B7280) // Gris medio (textos secundarios)

// ── Modelo de datos del resumen de pedido ─────────────────────────────────────
data class ItemPedido(val nombre: String, val cantidad: Int, val precio: Double)

// ══════════════════════════════════════════════════════════════════════════════
// SeccionMapaDespacho
// Declarada a nivel de archivo (top-level), marcada como `private`
// para que solo sea visible dentro de este archivo.
// ══════════════════════════════════════════════════════════════════════════════
/**
 * Muestra el mapa de Google Maps con dos marcadores:
 *   • La bodega/distribuidora (punto de origen del despacho).
 *   • La ubicación actual del cliente obtenida por GPS (punto de destino).
 *
 * @param latitudUsuario  Latitud del dispositivo, recibida desde obtenerUbicacionActual().
 * @param longitudUsuario Longitud del dispositivo, recibida desde obtenerUbicacionActual().
 * @param modifier        Permite al composable padre controlar tamaño y forma del mapa.
 */
@Composable
private fun SeccionMapaDespacho(
    latitudUsuario: Double,
    longitudUsuario: Double,
    modifier: Modifier = Modifier
) {
    // Coordenadas fijas de la bodega central (punto de origen del despacho).
    // `remember` evita que se recree el objeto LatLng en cada recomposición.
    val coordenadasLocal = remember { LatLng(-33.4372, -70.6506) }

    // Coordenadas dinámicas del cliente según el GPS.
    // `remember(latitudUsuario, longitudUsuario)` recalcula el objeto SOLO si
    // alguno de los parámetros cambia, optimizando recomposiciones innecesarias.
    val coordenadasUsuario = remember(latitudUsuario, longitudUsuario) {
        LatLng(latitudUsuario, longitudUsuario)
    }

    // Estado de la cámara del mapa: controla el centro y el nivel de zoom.
    // Se inicializa centrado en el usuario con zoom 12 (vista de barrio/comuna).
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(coordenadasUsuario, 12f)
    }

    // Composable principal que renderiza el mapa nativo de Google Maps.
    // Recibe el modifier del padre (tamaño, forma redondeada, etc.).
    GoogleMap(
        modifier = modifier,
        cameraPositionState = cameraPositionState
    ) {
        // Marcador de la bodega (origen). Aparece con globo de info al pulsarlo.
        Marker(
            state   = MarkerState(position = coordenadasLocal),
            title   = "Distribuidora Central",
            snippet = "Punto de despacho de productos"
        )

        // Marcador de la ubicación actual del cliente (destino del despacho).
        Marker(
            state   = MarkerState(position = coordenadasUsuario),
            title   = "Dirección de Destino",
            snippet = "Ubicación actual del cliente"
        )
    }
}

// ══════════════════════════════════════════════════════════════════════════════
// Pantalla principal de cálculo de despacho
// ══════════════════════════════════════════════════════════════════════════════
@Composable
fun DespachoScreen(
    totalCompraInicial: Double?,    // Monto del carrito si se viene del catálogo; null si es acceso directo
    itemsCarrito: List<ItemPedido> = emptyList(), // Lista de ítems desde el carrito
    activity: ComponentActivity,    // Necesaria para solicitar permisos de ubicación
    onBack: () -> Unit              // Callback para volver a la pantalla anterior
) {
    // Monto de compra mostrado/editado en el campo de texto
    var montoIngresado by remember { mutableStateOf(totalCompraInicial?.toInt()?.toString() ?: "") }

    // Resultado del cálculo de distancia bodega ↔ cliente (km)
    var resultadoDistancia by remember { mutableStateOf<Double?>(null) }

    // Resultado del cálculo de costo de despacho ($ CLP; 0.0 = gratis)
    var resultadoCosto by remember { mutableStateOf<Double?>(null) }

    // Indicador de carga mientras se espera la respuesta del GPS
    var cargando by remember { mutableStateOf(false) }

    // Mensaje de error visible en pantalla (vacío = sin error)
    var errorMsg by remember { mutableStateOf("") }

    // Estados para almacenar las coordenadas GPS del usuario ─
    // Al declararlas como `mutableStateOf`, cualquier asignación dispara una
    // recomposición y el mapa se dibuja automáticamente con los nuevos valores.
    var latitudUsuario by remember { mutableStateOf<Double?>(null) }
    var longitudUsuario by remember { mutableStateOf<Double?>(null) }

    val formatoPeso = NumberFormat.getCurrencyInstance(Locale("es", "CL"))

    // El subtotal se toma del total real del carrito; 0.0 si se entró directo
    val subtotalProductos = totalCompraInicial ?: 0.0

    // ── Estructura raíz de la pantalla ────────────────────────────────────────
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ColorSurface)
            .statusBarsPadding()
    ) {

        // ── Top bar ───────────────────────────────────────────────────────────
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

        // ── Contenido scrolleable ─────────────────────────────────────────────
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {

            // ── Card resumen del pedido ───────────────────────────────────────
            // Solo se renderiza si el carrito tiene ítems reales.
            // Al acceder directamente desde el menú, itemsCarrito = emptyList()
            // y esta card —con su Spacer inferior— desaparece por completo.
            if (itemsCarrito.isNotEmpty()) {
                Card(
                    modifier  = Modifier.fillMaxWidth(),
                    colors    = CardDefaults.cardColors(containerColor = Color.White),
                    shape     = RoundedCornerShape(14.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                    border    = CardDefaults.outlinedCardBorder()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {

                        // Encabezado con conteo dinámico de productos
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier         = Modifier.size(28.dp).clip(RoundedCornerShape(7.dp)).background(ColorPrimaryLight),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("📦", fontSize = 14.sp)
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text       = "Resumen del pedido",
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize   = 14.sp,
                                    color      = Color(0xFF111827)
                                )
                                // Pluraliza correctamente según la cantidad de ítems
                                val n = itemsCarrito.size
                                Text(
                                    text     = "$n producto${if (n != 1) "s" else ""} seleccionado${if (n != 1) "s" else ""}",
                                    fontSize = 11.sp,
                                    color    = ColorTextSecondary
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // ── Itera sobre los ítems REALES recibidos del carrito ─
                        // Cada ItemPedido ya trae:
                        //   • nombre   → nombre del producto
                        //   • cantidad → unidades seleccionadas
                        //   • precio   → precio unitario × cantidad (calculado en CatalogoScreen)
                        itemsCarrito.forEach { item ->
                            Row(
                                modifier              = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(text = item.nombre,         fontSize = 13.sp, color = Color(0xFF374151))
                                    Text(text = "x${item.cantidad}", fontSize = 11.sp, color = ColorTextSecondary)
                                }
                                Text(
                                    text       = "$${item.precio.toInt().formatCLP()}",
                                    fontSize   = 13.sp,
                                    fontWeight = FontWeight.Medium,
                                    color      = Color(0xFF111827)
                                )
                            }
                        }

                        Divider(color = ColorBorder, thickness = 0.5.dp, modifier = Modifier.padding(vertical = 8.dp))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Subtotal productos", fontWeight = FontWeight.Medium, fontSize = 14.sp, color = Color(0xFF374151))
                            Text(
                                text       = "$${subtotalProductos.toInt().formatCLP()}",
                                fontWeight = FontWeight.Bold,
                                fontSize   = 16.sp,
                                color      = Color(0xFF111827)
                            )
                        }
                    }
                }

                // Spacer DENTRO del if: no deja espacio fantasma al estar oculta
                Spacer(modifier = Modifier.height(12.dp))
            }
            // ── Card: Cálculo de despacho ───────────────────────────────────
            Card(
                modifier  = Modifier.fillMaxWidth(),
                colors    = CardDefaults.cardColors(containerColor = Color.White),
                shape     = RoundedCornerShape(14.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                border    = CardDefaults.outlinedCardBorder()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {

                    // Encabezado de la card
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
                                tint     = Color(0xFF2563EB),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text       = "Cálculo de despacho",
                                fontWeight = FontWeight.SemiBold,
                                fontSize   = 14.sp,
                                color      = Color(0xFF111827)
                            )
                            Text(
                                text     = "Basado en tu ubicación actual",
                                fontSize = 11.sp,
                                color    = ColorTextSecondary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Campo de monto manual: solo visible cuando no hay carrito previo
                    if (totalCompraInicial == null) {
                        OutlinedTextField(
                            value       = montoIngresado,
                            onValueChange = { v -> if (v.all { it.isDigit() }) montoIngresado = v },
                            label       = { Text("Ingrese monto de compra ($)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine  = true,
                            modifier    = Modifier.fillMaxWidth(),
                            colors      = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor   = ColorPrimary,
                                unfocusedBorderColor = ColorBorder,
                                focusedLabelColor    = ColorPrimary
                            )
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                    }

                    // Llamada real a SeccionMapaDespacho ─────
                    // Se muestra solo cuando latitudUsuario y longitudUsuario tienen
                    // valor (es decir, después de que el GPS respondió exitosamente).
                    // El operador `!!` es seguro aquí porque el `if` ya garantiza
                    // que ambos valores son no-nulos antes de entrar al bloque.
                    if (latitudUsuario != null && longitudUsuario != null) {
                        SeccionMapaDespacho(
                            latitudUsuario  = latitudUsuario!!,
                            longitudUsuario = longitudUsuario!!,
                            modifier        = Modifier
                                .fillMaxWidth()
                                .height(200.dp)                      // Altura fija dentro del scroll
                                .clip(RoundedCornerShape(10.dp))    // Esquinas redondeadas para coherencia visual
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    // Fila con la distancia calculada (visible tras el cálculo exitoso)
                    if (resultadoDistancia != null) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "Distancia calculada", fontSize = 13.sp, color = ColorTextSecondary)
                            Text(
                                text       = "${"%.1f".format(resultadoDistancia)} km",
                                fontSize   = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color      = Color(0xFF111827)
                            )
                        }
                    }

                    // Mensaje de error (GPS apagado, monto inválido, falla de red, etc.)
                    if (errorMsg.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Surface(color = Color(0xFFFFF1F2), shape = RoundedCornerShape(8.dp)) {
                            Text(
                                text     = errorMsg,
                                modifier = Modifier.padding(10.dp),
                                fontSize = 12.sp,
                                color    = Color(0xFFDC2626)
                            )
                        }
                    }

                    // Banner con el costo de despacho (verde = gratis / naranja = con costo)
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
                                    text     = if (resultadoCosto == 0.0) "🎉" else "🚚",
                                    fontSize = 20.sp
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text       = if (resultadoCosto == 0.0) "¡Despacho GRATIS!" else "Costo de despacho",
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize   = 14.sp,
                                        color      = if (resultadoCosto == 0.0) ColorPrimaryDark else Color(0xFF92400E)
                                    )
                                    // Solo muestra el valor monetario si el despacho tiene costo
                                    if (resultadoCosto!! > 0) {
                                        Text(
                                            text     = formatoPeso.format(resultadoCosto),
                                            fontSize = 13.sp,
                                            color    = Color(0xFF92400E)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        } // fin Column scrolleable

        // ── Barra de acción inferior fija ─────────────────────────────────────
        Surface(color = Color.White, shadowElevation = 4.dp) {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {

                Button(
                    onClick = {
                        // Validación: el monto debe ser un entero positivo
                        val montoCompra = montoIngresado.toIntOrNull()
                        if (montoCompra == null || montoCompra <= 0) {
                            errorMsg = "Por favor, ingresa un monto válido mayor a $0"
                            return@Button
                        }
                        errorMsg = ""
                        cargando = true

                        // Solicita la ubicación GPS al sistema operativo.
                        // La Activity es necesaria para manejar el diálogo de permisos.
                        obtenerUbicacionActual(
                            activity = activity,
                            onLocationReceived = { latGPS, lonGPS ->
                                cargando = false

                                // Se guardan las coordenadas en el estado de Compose.
                                // Esto dispara una recomposición que hace visible el
                                // bloque `if (latitudUsuario != null)` y renderiza el mapa.
                                latitudUsuario  = latGPS
                                longitudUsuario = lonGPS

                                // Persiste la ubicación en Firebase para trazabilidad del pedido
                                val ref = FirebaseDatabase.getInstance().getReference("ubicaciones")
                                ref.push().setValue(
                                    mapOf(
                                        "latitud"   to latGPS,
                                        "longitud"  to lonGPS,
                                        "timestamp" to System.currentTimeMillis()
                                    )
                                )
                                    .addOnSuccessListener { Log.d("APP_DESPACHO", "Ubicación guardada en Firebase") }
                                    .addOnFailureListener { e -> Log.e("APP_DESPACHO", "Error Firebase", e) }

                                // Coordenadas fijas de la bodega (origen del despacho)
                                val latBodega = -33.4372
                                val lonBodega = -70.6506

                                // Distancia en km usando la fórmula de Haversine (implementada en calcularDistancia)
                                val distanciaKm   = calcularDistancia(latGPS, lonGPS, latBodega, lonBodega)

                                // Costo según reglas de negocio: monto de compra y distancia
                                val costoDespacho = calcularCostoDespacho(montoCompra, distanciaKm)

                                resultadoDistancia = distanciaKm
                                resultadoCosto     = costoDespacho

                                Log.d("APP_DESPACHO", "Distancia: ${"%.2f".format(distanciaKm)} km | Costo: $costoDespacho")
                            },
                            onError = {
                                cargando = false
                                errorMsg = "No se pudo obtener la ubicación. Activa el GPS e intenta nuevamente."
                            }
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape    = RoundedCornerShape(10.dp),
                    colors   = ButtonDefaults.buttonColors(containerColor = ColorPrimary),
                    enabled  = !cargando
                ) {
                    // Indicador de progreso mientras el GPS responde
                    if (cargando) {
                        CircularProgressIndicator(
                            modifier    = Modifier.size(18.dp),
                            color       = Color.White,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text(
                        text       = if (cargando) "Calculando..." else "Confirmar Pedido",
                        fontWeight = FontWeight.SemiBold
                    )
                }

                // Enlace secundario para volver sin confirmar
                TextButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
                    Text(text = "< Volver al Catálogo", color = ColorTextSecondary, fontSize = 13.sp)
                }
            }
        }
    }
}

// ── Extensión de formato numérico ─────────────────────────────────────────────
// Convierte un Int en string con puntos de miles al estilo CLP: 10000 → "10.000"
private fun Int.formatCLP(): String = String.format("%,d", this).replace(',', '.')
