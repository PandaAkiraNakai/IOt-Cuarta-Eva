# AppDepartment — Módulos Depto

---

## Índice

- [Descripción breve](#descripción-breve)
- [Alcance](#alcance)
- [Funcionalidades principales](#funcionalidades-principales)
- [Mapeo de pantallas](#mapeo-de-pantallas)
- [Dependencias principales](#dependencias-principales)
- [Endpoints detectados](#endpoints-detectados)
- [Requisitos y configuración](#requisitos-y-configuración)
- [Cómo compilar y ejecutar (rápido)](#cómo-compilar-y-ejecutar-rápido)
- [Notas de seguridad y recomendaciones](#notas-de-seguridad-y-recomendaciones)
- [Estructura de archivos (rutas clave)](#estructura-de-archivos-rutas-clave)
- [Próximos pasos sugeridos](#próximos-pasos-sugeridos)

---

## Descripción breve

Esta aplicación Android (Kotlin) contiene módulos para la gestión de departamentos, sensores y usuarios. Este README describe únicamente los archivos cuyo nombre comienza por `depto` (actividades y layouts relevantes para administración y operación).


## Alcance

Documentamos las actividades `depto*` y sus layouts asociados: autenticación, CRUD de sensores, CRUD de usuarios, listados y paneles de administración. Se excluye código antiguo o no relacionado con `depto`.


## Funcionalidades principales

- Login por email/contraseña con redirección según rol (ADMIN / OPERADOR).
- Panel administrativo para gestionar sensores y usuarios.
- Registro, edición y eliminación de sensores (asociados a departamentos y usuarios).
- Creación de usuarios con validaciones (RUT chileno, formato de teléfono, contraseña segura).
- Listados con búsqueda y selección para ver/editar detalles.
- Historial de accesos por usuario.


## Mapeo de pantallas

Listado principal de Activities (ruta: `app/src/main/java/com/example/proyectoindoor`):

- `depto_login.kt` — Login; guarda sesión en `SharedPreferences`.
- `depto_gestion_adm.kt` — Menú admin (acceso a CRUD sensores/usuarios y listados).
- `depto_crud_sensores.kt` — Menú de sensores (Registrar / Modificar estados).
- `depto_crud_sensores_registro.kt` — Registro de sensor (formulario + POST).
- `depto_crud_sensores_estado.kt` — Lista de sensores por departamento.
- `depto_crud_sensores_estado_modificar.kt` — Editar / eliminar sensor (POST).
- `depto_crud_usuarios.kt` — Entrada al flujo de usuarios.
- `depto_crud_usuarios_crear.kt` — Crear usuario (validaciones + POST).
- `depto_crud_usuarios_modificar.kt`, `depto_usuarios_modificar_usu.kt` — Plantillas/edición.
- `depto_control_listado.kt`, `depto_control_listado_acceso.kt` — Listados y controles de acceso.
- `depto_usuario_bienvenida.kt` — Bienvenida para operadores.
- `depto_usuario_historial.kt` — Historial de accesos.

Cada Activity tiene su layout en `app/src/main/res/layout/activity_depto_*.xml`.


## Dependencias principales

Extraídas de `app/build.gradle.kts` y uso en código `depto*`:

- `com.android.volley:volley:1.2.1` — comunicación HTTP/REST.
- `com.github.f0ris.sweetalert:library:1.6.2` — diálogos tipo SweetAlert.
- `com.airbnb.android:lottie:6.7.0` — animaciones Lottie (presente en build.gradle).
- AndroidX (core-ktx, appcompat, material, activity, constraintlayout, cardview).

# IOt-Cuarta-Eva 🚀

![Project Logo](app/src/main/res/mipmap/ic_launcher.png)

[![Build](https://img.shields.io/badge/build-gradle-brightgreen)](https://gradle.org) [![Kotlin](https://img.shields.io/badge/kotlin-2.0.21-blue)](https://kotlinlang.org) [![Android SDK](https://img.shields.io/badge/Android%20SDK-36-yellow)]() [![Status](https://img.shields.io/badge/status-development-orange)]

✨ Descripción

- Aplicación Android (Kotlin) para gestión de departamentos, sensores y usuarios. Incluye flujos de autenticación por rol (ADMIN / OPERADOR), CRUD de sensores y usuarios, listados y registro de accesos.

🔎 Características principales

- Login con persistencia de sesión (SharedPreferences).
- Panel administrativo para CRUD de sensores y usuarios.
- Registro y edición de sensores (asignación por departamento/usuario).
- Listados con búsqueda y control de accesos.
- Presentaciones y diálogos enriquecidos (Lottie + SweetAlert).

📁 Estructura clave

- Código: `app/src/main/java/` (activities y clases, muchas con prefijo `depto_*`).
- Layouts: `app/src/main/res/layout/` (layouts `activity_depto_*.xml`).
- Manifest: `app/src/main/AndroidManifest.xml` (permisos: `INTERNET`, `CAMERA`).
- Build: `app/build.gradle.kts` y `gradle/libs.versions.toml`.

⚙️ Requisitos

- JDK 11
- Android SDK (API 36)
- `minSdk = 24`, `targetSdk = 36`

💻 Cómo compilar (rápido)

Desde la raíz del proyecto:

```bash
chmod +x ./gradlew
./gradlew clean assembleDebug
```

Instalar APK en dispositivo/emulador conectado:

```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

🔐 Notas de seguridad

- Atención: el proyecto usa `android:usesCleartextTraffic="true"` y varios endpoints descubiertos usan HTTP (no cifrado). Migrar a HTTPS antes de producción.
- Revisa almacenamiento de credenciales en `SharedPreferences` y política de expiración/renovación de sesión.
- `switches.kt` y otros módulos realizan polling frecuente; considerar WebSockets o reducción de frecuencia.

🌐 Endpoints detectados (ejemplos)

- POST http://54.89.22.17/login.php — Login (email, password). Respuesta JSON con `rol`, `id_usuario`, `id_departamento`, `nombre`.
- GET http://54.89.22.17/listar_departamentos.php
- POST http://54.89.22.17/registrar_sensor.php
- GET http://54.89.22.17/listar_sensores.php?id_departamento=...
- POST http://54.89.22.17/eliminar_sensor.php

⚠️ Estos endpoints fueron detectados en el código; aparecen como llamadas HTTP en varias Activities.

🧭 Mapeo rápido de Activities (ruta: `app/src/main/java/com/example/proyectoindoor`)

- `depto_login.kt` — Login (launcher)
- `depto_gestion_adm.kt` — Menú admin
- `depto_crud_sensores*.kt` — Flujos de gestión de sensores
- `depto_crud_usuarios*.kt` — Flujos de gestión de usuarios
- `depto_control_listado*.kt` — Listados y controles
- `depto_usuario_historial.kt` — Historial de accesos

🧩 Dependencias destacadas

- `com.android.volley:volley:1.2.1`
- `com.github.f0ris.sweetalert:library:1.6.2`
- `com.airbnb.android:lottie:6.7.0`
- AndroidX (core-ktx, appcompat, material, activity, constraintlayout, cardview)

✍️ Autores / Créditos

- Sergio Cubelli (Sergio el Nazer)
- Victor Manzano (Victor el Nazi)


