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
import com.example.appdistribuidora.ui.screens.CatalogoScreen
import com.example.appdistribuidora.ui.DespachoScreen
import com.example.appdistribuidora.ui.LoginScreen
import com.example.appdistribuidora.ui.MenuScreen
import com.example.appdistribuidora.ui.screens.TemperatureScreen
import com.example.appdistribuidora.ui.theme.AppDistribuidoraTheme

class MainActivity : ComponentActivity() {

    // Launcher utilizado para solicitar el permiso de ubicación al usuario.
    // Este permiso es necesario para calcular la distancia del despacho.
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

        // Permite que la aplicación use un diseño de borde a borde.
        enableEdgeToEdge()

        // Solicita permiso de ubicación al iniciar la aplicación.
        solicitarPermisoUbicacion()

        setContent {
            AppDistribuidoraTheme {

                // Estado principal de navegación.
                // Define qué pantalla se muestra actualmente.
                var currentScreen by remember {
                    mutableStateOf<AppScreen>(AppScreen.Login)
                }

                // Control de navegación entre pantallas según el valor actual.
                when (val screen = currentScreen) {

                    // Pantalla de inicio de sesión.
                    is AppScreen.Login -> LoginScreen(
                        onLoginSuccess = {
                            currentScreen = AppScreen.Menu
                        }
                    )

                    // Pantalla de menú principal.
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

                    // Pantalla de catálogo de productos.
                    is AppScreen.Catalogo -> CatalogoScreen(
                        onGoToDespacho = { montoCalculado, itemsSeleccionados ->
                            currentScreen = AppScreen.Despacho(
                                totalCompraInicial = montoCalculado,
                                items = itemsSeleccionados
                            )
                        },
                        onBack = {
                            currentScreen = AppScreen.Menu
                        }
                    )

                    // Pantalla de monitoreo de temperatura.
                    is AppScreen.Temperatura -> TemperatureScreen(
                        onBack = {
                            currentScreen = AppScreen.Menu
                        }
                    )

                    // Pantalla de cálculo de despacho.
                    is AppScreen.Despacho -> DespachoScreen(
                        totalCompraInicial = screen.totalCompraInicial,
                        itemsCarrito = screen.items,
                        activity = this@MainActivity,
                        onBack = {
                            currentScreen = AppScreen.Menu
                        }
                    )
                }
            }
        }
    }

    // Solicita el permiso de ubicación fina si aún no ha sido concedido.
    // Este permiso permite obtener la ubicación actual del dispositivo.
    private fun solicitarPermisoUbicacion() {
        val permisoConcedido = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (!permisoConcedido) {
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }
}