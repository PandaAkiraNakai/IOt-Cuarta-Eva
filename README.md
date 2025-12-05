# AppDepartment — Módulos `depto*` (ProyectoIndoor)

> Documentación enfocada exclusivamente en las pantallas y lógicas `depto*` del proyecto.

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


## Endpoints detectados

> Observación: varios endpoints usan HTTP sin cifrar (http://). Revisar seguridad antes de usar en producción.

- POST http://54.89.22.17/login.php — login (params: `email`, `password`). Respuesta: JSON con `rol`, `id_usuario`, `id_departamento`, `nombre`.
- GET http://54.89.22.17/listar_departamentos.php — lista de departamentos.
- POST http://54.89.22.17/registrar_sensor.php — registrar sensor (`codigo_sensor`, `tipo`, `id_usuario`, `id_departamento`, `estado`, `fecha_alta`).
- GET http://54.89.22.17/listar_sensores.php?id_departamento=... — listar sensores por departamento.
- POST http://54.89.22.17/eliminar_sensor.php — eliminar sensor (`id_sensor`).
- POST http://34.206.129.152/actualizar_sensor.php — actualizar sensor (incluye `fecha_baja` opcional).
- POST http://54.89.22.17/crear_usuario.php — crear usuario (nombre, rut, email, telefono, password, rol, id_departamento).
- Otros: `apiconsultausu.php`, `consulta.php`, `listar_usuarios_depto.php` (uso en listados y autenticación).


## Requisitos y configuración

- JDK 11 (project usa Java 11 y `kotlinOptions.jvmTarget = "11"`).
- Android SDK (API 36) — `compileSdk = 36`, `targetSdk = 36`.
- `minSdk = 24`.
- Android Gradle Plugin 8.13.1, Kotlin 2.0.21 (ver `gradle/libs.versions.toml`).
- Permisos importantes en `AndroidManifest.xml`: `INTERNET`, `CAMERA`.
- `depto_login` está registrado como activity lanzadora (launcher).


## Cómo compilar y ejecutar (rápido)

1. Instala JDK 11 y configura Android SDK (API 36).
2. Desde la raíz del proyecto:

```bash
chmod +x ./gradlew
./gradlew clean assembleDebug
```

3. APK generado:

```
app/build/outputs/apk/debug/app-debug.apk
```

4. Para instalar en un dispositivo/emulador conectado:

```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
```


## Notas de seguridad y recomendaciones

- Transportes: migrar todos los endpoints a HTTPS antes de producción.
- Polling: `switches.kt` realiza peticiones cada segundo; considerar WebSockets o reducir frecuencia.
- Sesión: la app guarda `id_usuario`, `id_departamento`, `rol`, `nombre` en `SharedPreferences`.
- Validaciones: `depto_crud_usuarios_crear.kt` valida RUT y teléfono (`+569...`) — ajusta si cambian reglas.
- APIs deprecadas: existen usos de `startActivityForResult` en código legado; migrar a Activity Result API.


## Estructura de archivos (rutas clave)

- `app/src/main/java/com/example/proyectoindoor/depto_*.kt`
- `app/src/main/res/layout/activity_depto_*.xml`
- `app/build.gradle.kts`
- `gradle/libs.versions.toml`
- `app/src/main/AndroidManifest.xml`


---

## Autor

Sergio Cubelli
Victor Manzano

