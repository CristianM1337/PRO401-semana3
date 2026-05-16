package com.example.appdistribuidora.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Paleta de colores utilizada en la interfaz principal de la aplicación.
private val ColorPrimary = Color(0xFF16A34A)
private val ColorPrimaryLight = Color(0xFFDCFCE7)
private val ColorPrimaryDark = Color(0xFF15803D)
private val ColorSurface = Color(0xFFF9FAFB)
private val ColorBorder = Color(0xFFE5E7EB)
private val ColorTextSecondary = Color(0xFF6B7280)

// Pantalla principal de navegación de la aplicación.
// Permite acceder al catálogo, cálculo de despacho,
// monitoreo de temperatura y cierre de sesión.
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
            .background(ColorSurface)
            .statusBarsPadding()
    ) {
        // Header con logo y nombre
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(horizontal = 24.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Ícono de app (snowflake)
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(ColorPrimary),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "❄", fontSize = 28.sp)
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "DistribuFood",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF111827)
            )
            Text(
                text = "Distribuidora de alimentos",
                style = MaterialTheme.typography.bodySmall,
                color = ColorTextSecondary
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Badge de bienvenida
            Surface(
                color = ColorPrimaryLight,
                shape = RoundedCornerShape(50.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "⭐", fontSize = 12.sp)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "¡Bienvenida!",
                        fontSize = 13.sp,
                        color = ColorPrimaryDark,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Label sección
        Text(
            text = "MENÚ PRINCIPAL",
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = ColorTextSecondary,
            letterSpacing = 1.sp,
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
        )

        // Opciones del menú
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Componente reutilizable para representar una opción del menú principal.
            MenuItemCard(
                icon = Icons.Default.ShoppingCart,
                title = "Ver Catálogo de Productos",
                subtitle = "Explorar y seleccionar productos",
                tintColor = ColorPrimary,
                bgColor = ColorPrimaryLight,
                onClick = onGoToCatalogo
            )

            MenuItemCard(
                icon = Icons.Default.LocalShipping,
                title = "Ir a cálculo de despacho",
                subtitle = "Calcular costo de envío",
                tintColor = Color(0xFF2563EB),
                bgColor = Color(0xFFEFF6FF),
                onClick = onGoToDespacho
            )

            // Fila con 3 stats (48 productos / 12 categorías / 3 en carrito)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.White)
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // Componente visual utilizado para mostrar estadísticas rápidas.
                StatItem(value = "48", label = "Productos")
                Divider(
                    modifier = Modifier
                        .height(32.dp)
                        .width(0.5.dp),
                    color = ColorBorder
                )
                StatItem(value = "12", label = "Categorías")
                Divider(
                    modifier = Modifier
                        .height(32.dp)
                        .width(0.5.dp),
                    color = ColorBorder
                )
                StatItem(value = "3", label = "En carrito")
            }

            MenuItemCard(
                icon = Icons.Default.Thermostat,
                title = "Gestión de temperatura",
                subtitle = "Monitorear flota en tiempo real",
                tintColor = Color(0xFFDC2626),
                bgColor = Color(0xFFFFF1F2),
                onClick = onGoToTemperatura
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        // Separador y opción Cerrar sesión al fondo
        Divider(color = ColorBorder, thickness = 0.5.dp, modifier = Modifier.padding(horizontal = 16.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onLogout() }
                .padding(horizontal = 24.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFFEF2F2)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.ExitToApp,
                        contentDescription = null,
                        tint = Color(0xFFDC2626),
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Cerrar sesión",
                        fontWeight = FontWeight.Medium,
                        fontSize = 14.sp,
                        color = Color(0xFF111827)
                    )
                    Text(
                        text = "Salir de tu cuenta",
                        fontSize = 12.sp,
                        color = ColorTextSecondary
                    )
                }
            }
            Icon(
                imageVector = Icons.Default.ExitToApp,
                contentDescription = null,
                tint = ColorTextSecondary,
                modifier = Modifier.size(16.dp)
            )
        }

        Text(
            text = "DistribuFood © 2026 · v2.1.0",
            fontSize = 11.sp,
            color = ColorTextSecondary,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
                .wrapContentWidth(Alignment.CenterHorizontally)
        )
    }
}

@Composable
private fun MenuItemCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    tintColor: Color,
    bgColor: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White)
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(bgColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = tintColor,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = title,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = Color(0xFF111827)
                )
                Text(
                    text = subtitle,
                    fontSize = 12.sp,
                    color = ColorTextSecondary
                )
            }
        }
        Icon(
            imageVector = Icons.Default.ExitToApp,
            contentDescription = null,
            tint = ColorTextSecondary,
            modifier = Modifier.size(16.dp)
        )
    }
}

@Composable
private fun StatItem(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            color = Color(0xFF111827)
        )
        Text(
            text = label,
            fontSize = 11.sp,
            color = ColorTextSecondary
        )
    }
}