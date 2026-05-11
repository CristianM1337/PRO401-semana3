package com.example.appdistribuidora.navigation

sealed class AppScreen {
    object Login : AppScreen()
    object Menu : AppScreen()
    object Catalogo : AppScreen()

    object Temperatura : AppScreen()
    // Permitimos que sea Double? para que pueda ser nulo al entrar desde el menú
    data class Despacho(val totalCompraInicial: Double?) : AppScreen()
}