package com.example.appdistribuidora.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Modelo de datos para los productos
data class Producto(
    val nombre: String,
    val precio: Double,
    val requiereFrio: Boolean
)

@Composable
fun CatalogoScreen(
    onGoToDespacho: (Double) -> Unit,
    onBack: () -> Unit
) {
    // Lista de productos de la distribuidora
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

    // Estado para las cantidades de cada producto
    val cantidades = remember { mutableStateListOf(*Array(listaProductos.size) { 0 }) }

    // Cálculo dinámico del total de la compra
    val totalCompra = listaProductos.zip(cantidades).sumOf { (prod, cant) -> prod.precio * cant }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .statusBarsPadding()
    ) {
        // Cabecera
        Text(
            text = "Catálogo de Productos",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Text(
            text = "Selecciona hasta 5 unidades por producto",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Lista de productos con Scroll eficiente
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            itemsIndexed(listaProductos) { index, producto ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = producto.nombre,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "$${producto.precio.toInt()}",
                                    color = MaterialTheme.colorScheme.primary
                                )
                                if (producto.requiereFrio) {
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Surface(
                                        color = Color(0xFFE3F2FD),
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Text(
                                            text = "❄️ FRÍO",
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                                            fontSize = 10.sp,
                                            color = Color(0xFF1976D2),
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }

                        // Controles de cantidad
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            FilledTonalIconButton(
                                onClick = { if (cantidades[index] > 0) cantidades[index]-- },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Text("-", fontWeight = FontWeight.Bold)
                            }

                            Text(
                                text = "${cantidades[index]}",
                                modifier = Modifier.padding(horizontal = 12.dp),
                                style = MaterialTheme.typography.bodyLarge
                            )

                            FilledTonalIconButton(
                                onClick = { if (cantidades[index] < 5) cantidades[index]++ },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Text("+", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // Panel inferior con resumen y navegación
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            tonalElevation = 4.dp,
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Total a pagar:", style = MaterialTheme.typography.titleMedium)
                    Text(
                        text = "$${totalCompra.toInt()}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = { onGoToDespacho(totalCompra) },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = totalCompra > 0,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Continuar a Despacho")
                }

                TextButton(
                    onClick = onBack,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Volver al Menú")
                }
            }
        }
    }
}