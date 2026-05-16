package com.example.appdistribuidora.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

// ---------------- TEMA OSCURO ----------------

// Define la paleta de colores utilizada cuando el dispositivo
// se encuentra en modo oscuro.
private val DarkColorScheme = darkColorScheme(

    // Color principal del tema oscuro.
    primary = Purple80,

    // Color secundario utilizado en componentes complementarios.
    secondary = PurpleGrey80,

    // Color terciario utilizado en detalles visuales.
    tertiary = Pink80
)


// ---------------- TEMA CLARO ----------------

// Define la paleta de colores utilizada cuando el dispositivo
// se encuentra en modo claro.
private val LightColorScheme = lightColorScheme(

    // Color principal del tema claro.
    primary = Purple40,

    // Color secundario utilizado en componentes complementarios.
    secondary = PurpleGrey40,

    // Color terciario utilizado en detalles visuales.
    tertiary = Pink40

    /*
    Colores adicionales que podrían personalizarse:

    background = Color(0xFFFFFBFE),
    surface = Color(0xFFFFFBFE),

    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,

    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F),
    */
)


// ---------------- TEMA PRINCIPAL DE LA APP ----------------

@Composable
fun AppDistribuidoraTheme(

    // Detecta automáticamente si el sistema usa modo oscuro.
    darkTheme: Boolean = isSystemInDarkTheme(),

    // Dynamic Color disponible en Android 12 o superior.
    dynamicColor: Boolean = true,

    // Contenido visual que utilizará este tema.
    content: @Composable () -> Unit
) {

    // Determina qué esquema de colores utilizar.
    val colorScheme = when {

        // Usa colores dinámicos de Android 12+.
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {

            val context = LocalContext.current

            if (darkTheme) {
                dynamicDarkColorScheme(context)
            } else {
                dynamicLightColorScheme(context)
            }
        }

        // Usa el tema oscuro definido manualmente.
        darkTheme -> DarkColorScheme

        // Usa el tema claro definido manualmente.
        else -> LightColorScheme
    }

    // Aplica el tema Material Design a toda la aplicación.
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}