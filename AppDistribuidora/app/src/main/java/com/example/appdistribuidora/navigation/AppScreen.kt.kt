package com.example.appdistribuidora.navigation
import com.example.appdistribuidora.ui.ItemPedido

sealed class AppScreen {
    object Login : AppScreen()
    object Menu : AppScreen()
    object Catalogo : AppScreen()

    object Temperatura : AppScreen()
    // Permitimos que sea Double? para que pueda ser nulo al entrar desde el menú
    data class Despacho(
        val totalCompraInicial: Double?,
        // Lista de ítems seleccionados en el catálogo.
        // Valor por defecto emptyList() cubre el acceso directo desde el menú
        // (AppScreen.Despacho(null)), sin necesidad de cambiar esa llamada.
        val items: List<ItemPedido> = emptyList()
    ) : AppScreen()

}