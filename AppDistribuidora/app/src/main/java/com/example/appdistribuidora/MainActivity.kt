package com.example.appdistribuidora

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import com.example.appdistribuidora.navigation.AppScreen
import com.example.appdistribuidora.ui.CatalogoScreen
import com.example.appdistribuidora.ui.DespachoScreen
import com.example.appdistribuidora.ui.LoginScreen
import com.example.appdistribuidora.ui.MenuScreen
import com.example.appdistribuidora.ui.screens.TemperatureScreen
import com.example.appdistribuidora.ui.theme.AppDistribuidoraTheme

class MainActivity : ComponentActivity() {

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                Log.d("APP_DESPACHO", "Permiso de ubicación concedido")
            } else {
                Log.d("APP_DESPACHO", "Permiso de ubicación denegado")
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        solicitarPermisoUbicacion()

        setContent {
            AppDistribuidoraTheme {

                var currentScreen by remember {
                    mutableStateOf<AppScreen>(AppScreen.Login)
                }

                when (val screen = currentScreen) {

                    is AppScreen.Login -> LoginScreen(
                        onLoginSuccess = {
                            currentScreen = AppScreen.Menu
                        }
                    )

                    is AppScreen.Menu -> MenuScreen(
                        onGoToCatalogo = {
                            currentScreen = AppScreen.Catalogo
                        },

                        onGoToDespacho = {
                            currentScreen = AppScreen.Despacho(null)
                        },

                        onGoToTemperatura = {
                            currentScreen = AppScreen.Temperatura
                        },

                        onLogout = {
                            currentScreen = AppScreen.Login
                        }
                    )

                    is AppScreen.Catalogo -> CatalogoScreen(
                        onGoToDespacho = { montoCalculado ->
                            currentScreen = AppScreen.Despacho(montoCalculado)
                        },

                        onBack = {
                            currentScreen = AppScreen.Menu
                        }
                    )

                    is AppScreen.Temperatura -> TemperatureScreen(
                        onBack = {
                            currentScreen = AppScreen.Menu
                        }
                    )

                    is AppScreen.Despacho -> DespachoScreen(
                        totalCompraInicial = screen.totalCompraInicial,
                        activity = this@MainActivity,

                        onBack = {
                            currentScreen = AppScreen.Menu
                        }
                    )
                }
            }
        }
    }

    private fun solicitarPermisoUbicacion() {

        if (
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {

            requestPermissionLauncher.launch(
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        }
    }
}