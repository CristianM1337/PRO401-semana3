package com.example.appdistribuidora.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class Producto(
    val nombre: String,
    val precio: Double,
    val requiereFrio: Boolean
)

private val ColorPrimary = Color(0xFF16A34A)
private val ColorPrimaryLight = Color(0xFFDCFCE7)
private val ColorPrimaryDark = Color(0xFF15803D)
private val ColorFrio = Color(0xFF2563EB)
private val ColorFrioLight = Color(0xFFEFF6FF)
private val ColorSecoLight = Color(0xFFFFFBEB)
private val ColorSeco = Color(0xFFD97706)
private val ColorSurface = Color(0xFFF9FAFB)
private val ColorBorder = Color(0xFFE5E7EB)
private val ColorTextSecondary = Color(0xFF6B7280)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CatalogoScreen(
    onGoToDespacho: (Double) -> Unit,
    onBack: () -> Unit
) {
    val listaProductos = remember {
        listOf(
            Producto("Mix de mariscos congelados", 5000.0, true),
            Producto("Lomo vetado al vacío 1kg", 12000.0, true),
            Producto("Salmón congelado", 7000.0, true),
            Producto("Caja de hamburguesas 10u", 8000.0, true),
            Producto("Aceite maravilla 1lt", 2500.0, false),
            Producto("Arroz grado 2 1kg", 1800.0, false),
            Producto("Pack 12 Cervezas", 7000.0, false),
            Producto("Sal 1kg", 500.0, false)
        )
    }

    val cantidades = remember { mutableStateListOf(*Array(listaProductos.size) { 0 }) }
    val totalCompra = listaProductos.zip(cantidades).sumOf { (prod, cant) -> prod.precio * cant }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ColorSurface)
            .statusBarsPadding()
    ) {
        // Header con TopAppBar estilo mockup
        Surface(
            color = Color.White,
            shadowElevation = 0.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Volver",
                        tint = Color(0xFF374151)
                    )
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
                            text = "Distribuidora de alimentos",
                            fontSize = 10.sp,
                            color = ColorTextSecondary
                        )
                    }
                }
            }
        }

        // Título catálogo
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            Text(
                text = "Catálogo",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF111827)
            )
            Text(
                text = "Máximo 5 unidades por producto",
                fontSize = 12.sp,
                color = ColorTextSecondary
            )
        }

        Divider(color = ColorBorder, thickness = 0.5.dp)

        // Lista de productos
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .background(Color.White),
            contentPadding = PaddingValues(vertical = 4.dp)
        ) {
            itemsIndexed(listaProductos) { index, producto ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Info del producto
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = producto.nombre,
                            fontWeight = FontWeight.Medium,
                            fontSize = 14.sp,
                            color = Color(0xFF111827)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "$${producto.precio.toInt().format()}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = ColorPrimary
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            if (producto.requiereFrio) {
                                Surface(
                                    color = ColorFrioLight,
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        text = "❄ FRÍO",
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                        fontSize = 10.sp,
                                        color = ColorFrio,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            } else {
                                Surface(
                                    color = ColorSecoLight,
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        text = "● SECO",
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                        fontSize = 10.sp,
                                        color = ColorSeco,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }

                    // Controles de cantidad al estilo mockup
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Botón menos
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (cantidades[index] > 0) ColorSurface else Color(0xFFF3F4F6)),
                            contentAlignment = Alignment.Center
                        ) {
                            TextButton(
                                onClick = { if (cantidades[index] > 0) cantidades[index]-- },
                                modifier = Modifier.size(28.dp),
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Text(
                                    "−",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = Color(0xFF374151)
                                )
                            }
                        }

                        Text(
                            text = "${cantidades[index]}",
                            modifier = Modifier
                                .padding(horizontal = 10.dp)
                                .widthIn(min = 16.dp),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF111827)
                        )

                        // Botón más — verde
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(ColorPrimary),
                            contentAlignment = Alignment.Center
                        ) {
                            TextButton(
                                onClick = { if (cantidades[index] < 5) cantidades[index]++ },
                                modifier = Modifier.size(28.dp),
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Text(
                                    "+",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }

                if (index < listaProductos.size - 1) {
                    Divider(
                        color = ColorBorder,
                        thickness = 0.5.dp,
                        modifier = Modifier.padding(horizontal = 20.dp)
                    )
                }
            }
        }

        // Panel inferior: total + botones
        Surface(
            color = Color.White,
            shadowElevation = 4.dp
        ) {
            Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Total a pagar",
                        fontWeight = FontWeight.Medium,
                        fontSize = 14.sp,
                        color = Color(0xFF374151)
                    )
                    Text(
                        text = "$${totalCompra.toInt().format()}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp,
                        color = if (totalCompra > 0) ColorPrimary else ColorTextSecondary
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = { onGoToDespacho(totalCompra) },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = totalCompra > 0,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = ColorPrimary)
                ) {
                    Text(
                        text = "Continuar a Despacho",
                        fontWeight = FontWeight.SemiBold
                    )
                }

                TextButton(
                    onClick = onBack,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "< Volver al Menú",
                        color = ColorTextSecondary,
                        fontSize = 13.sp
                    )
                }
            }
        }
    }
}

// Extensión para formatear números con puntos (formato CLP)
private fun Int.format(): String {
    return String.format("%,d", this).replace(',', '.')
}
