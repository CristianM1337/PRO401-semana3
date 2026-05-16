package com.example.appdistribuidora.navigation

import com.example.appdistribuidora.ui.ItemPedido

// Clase sellada utilizada para manejar la navegación entre pantallas.
// Cada objeto representa una pantalla distinta dentro de la aplicación.
sealed class AppScreen {

    // Pantalla de inicio de sesión
    object Login : AppScreen()

    // Pantalla principal del menú
    object Menu : AppScreen()

    // Pantalla de catálogo de productos
    object Catalogo : AppScreen()

    // Pantalla de monitoreo de temperatura
    object Temperatura : AppScreen()

    // Pantalla de despacho.
    // Se utiliza data class porque necesita recibir parámetros.
    data class Despacho(

        // Monto inicial recibido desde el carrito o catálogo
        val totalCompraInicial: Double?,

        // Lista de productos seleccionados por el usuario.
        // Por defecto se utiliza una lista vacía para evitar errores
        // cuando se accede directamente desde el menú principal.
        val items: List<ItemPedido> = emptyList()

    ) : AppScreen()
}