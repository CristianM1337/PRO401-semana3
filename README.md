# **DistribuFood: Sistema Móvil de Gestión de Despacho y Monitoreo de Temperatura 🚚❄️**

Este repositorio contiene el código fuente para "DistribuFood", una aplicación móvil nativa desarrollada en Android (Kotlin/Jetpack Compose) que resuelve el caso de estudio de una empresa distribuidora de alimentos.  
El sistema integra la gestión de compras, el cálculo dinámico de tarifas de despacho por geolocalización y el monitoreo en tiempo real de la cadena de frío mediante sensores IoT y Firebase.

## **🎯 Objetivos del Proyecto**

El objetivo principal es proveer una solución tecnológica móvil que permita a los usuarios:

* Autenticarse de forma segura utilizando sus cuentas de Google (Gmail).  
* Explorar un catálogo de productos.  
* Calcular automáticamente el costo de despacho basado en la distancia kilométrica entre la ubicación del usuario y la distribuidora.  
* Para la gestión interna, monitorear la temperatura de los camiones de reparto y emitir alertas sonoras si se interrumpe la cadena de frío.

## **🏗️ Arquitectura y Tecnologías**

El proyecto se sustenta en una arquitectura de microservicios e IoT, utilizando las siguientes tecnologías:

* **Frontend (App Móvil):** Android nativo con Kotlin y Jetpack Compose.  
* **Servicios de Ubicación:** Google Maps SDK for Android y Google Play Services Location (para obtener la latitud/longitud del dispositivo).  
* **Backend & Base de Datos:** Firebase Realtime Database (para almacenar y recuperar datos del catálogo y lecturas del sensor).  
* **Autenticación:** Firebase Authentication (Google Sign-In).  
* **IoT (Simulación):** Tinkercad (Simulación del circuito con NodeMCU ESP8266 y sensor de temperatura TMP36).

## **🚀 Funcionalidades Principales**

### **1\. Autenticación Integrada**

Los usuarios inician sesión exclusivamente mediante el proveedor de identidad de Google. Esto simplifica el registro y aumenta la seguridad del acceso.

### **2\. Catálogo de Productos Sincronizado**

La vista del catálogo obtiene sus datos directamente desde Firebase Realtime Database. Esto permite a los administradores actualizar precios o disponibilidad de productos en tiempo real sin necesidad de actualizar la app.

### **3\. Cálculo de Despacho por Geolocalización**

La aplicación utiliza Google Maps para trazar la ubicación de la distribuidora y la del usuario. Dependiendo del total de la compra y la distancia (calculada internamente), se aplican las siguientes reglas de negocio:

* **Compras ≥ $50.000:** Despacho gratuito (hasta 20 km).  
* **Compras entre $25.000 y $49.999:** Cobro de $150 por kilómetro.  
* **Compras \< $25.000:** Cobro de $300 por kilómetro.

### **4\. Monitoreo IoT de Cadena de Frío**

A través de un sensor simulado que inyecta datos en Firebase, la aplicación cuenta con un panel que lee constantemente el valor de la temperatura en el camión. Si la lectura supera un umbral crítico de grados Celsius, el dispositivo móvil emite una alerta sonora.

## **🛠️ Compilación y Ejecución**

Para clonar y ejecutar este proyecto localmente, sigue estos pasos:

### **Prerrequisitos**

* Tener instalado [Android Studio](https://developer.android.com/studio) (versión Flamingo o superior recomendada).  
* Una cuenta de Firebase activa.  
* Claves API de Google Maps configuradas en la consola de Google Cloud.

### **Pasos de Configuración**

1. **Clonar el repositorio:**  
   git clone \[https://github.com/cristianm1337/pro401-proyecto-app-movil.git\](https://github.com/cristianm1337/pro401-proyecto-app-movil.git)

2. **Abrir el proyecto en Android Studio:** \* Ve a File \> Open... y selecciona la carpeta del proyecto.  
3. **Configurar Firebase (google-services.json):**  
   * El archivo google-services.json original ha sido excluido del repositorio por seguridad. Debes crear un proyecto en tu consola de Firebase, añadir una aplicación Android y descargar el nuevo archivo google-services.json. Colócalo en la ruta app/.  
4. **Configurar la API Key de Google Maps:**  
   * Asegúrate de agregar tu API Key de Google Maps en el archivo AndroidManifest.xml (dentro del tag \<application\>) o mediante la inyección segura desde tu archivo local.properties.  
5. **Sincronizar Gradle:**  
   * Haz clic en *Sync Project with Gradle Files*.  
6. **Ejecutar:**  
   * Conecta un dispositivo físico o inicia un emulador de Android y presiona el botón *Run* (Shift \+ F10).

## **Plan de Pruebas**

El sistema ha sido sometido a un riguroso plan de pruebas documentado, incluyendo 10 casos de uso que validan:

* La correcta denegación/aceptación de la autenticación de Google.  
* La exactitud en el cálculo kilométrico y su conversión matemática a tarifas.  
* La correcta deserialización de los datos (como la conversión de la temperatura en Firebase de °F a °C).  
* Comportamiento ante la negación de permisos de GPS por parte del usuario.