# AppDepartment — Módulos Depto (v6.9)

![AppDepartment](app/src/main/res/mipmap/ic_launcher.png)

[![Build](https://img.shields.io/badge/build-gradle-brightgreen)](https://gradle.org) [![Kotlin](https://img.shields.io/badge/kotlin-2.0.21-blue)](https://kotlinlang.org) [![Android SDK](https://img.shields.io/badge/Android%20SDK-36-yellow)]() [![Version](https://img.shields.io/badge/version-6.9-blue)]() [![Status](https://img.shields.io/badge/status-development-orange)]

---

## Resumen rápido

- **Nombre:** AppDepartment
- **Versión:** 6.9
- **Alcance de este README:** documentación enfocada exclusivamente en los módulos cuyo nombre comienza con `depto` (actividades, layouts y lógica servidor/cliente relacionados con gestión de departamentos, sensores y usuarios).
- **Autor:** GitHub Copilot
- **Licencia:** MIT

---

## Índice

- [Descripción](#descripción)
- [Alcance y convenciones](#alcance-y-convenciones)
- [Funcionalidades clave (depto)](#funcionalidades-clave-depto)
- [Pantallas / Activities importantes](#pantallas--activities-importantes)
- [Endpoints API usados por `depto*`](#endpoints-api-usados-por-depto)
- [Cómo compilar / instalar (rápido)](#cómo-compilar--instalar-rápido)
- [Requisitos del servidor y notas](#requisitos-del-servidor-y-notas)
- [Comportamientos y limitaciones conocidas](#comportamientos-y-limitaciones-conocidas)
- [Contacto / Créditos](#contacto--créditos)
- [Licencia](#licencia)

---

## Descripción

AppDepartment agrupa los módulos Android (Kotlin) para gestionar departamentos, sensores y usuarios dentro de la app. Este README documenta únicamente los ficheros `depto*` (cliente Android) y los endpoints PHP relevantes que la app consume.


## Alcance y convenciones

- Solo se documentan archivos y pantallas cuyo nombre inicia con `depto`.
- Rutas clave del cliente: `app/src/main/java/com/example/appdepartment/` y `app/src/main/res/layout/`.
- Las URLs del servidor aparecen en el código (por ejemplo `http://54.89.22.17/...`). Asegúrate de actualizar las IP/host si cambias entorno.


## Funcionalidades clave (depto)

- Autenticación (login) con persistencia de sesión en `SharedPreferences`.
- Roles: `ADMIN` y `OPERADOR` con vistas y permisos diferenciados.
  - El admin puede crear/editar/eliminar usuarios, gestionar sensores y ver listados.
  - El operador puede ver listados, ver historial de accesos y acciones de lectura (no edición).
- CRUD de usuarios con validaciones:
  - Validación de RUT chileno (DV), formato de teléfono Chile (+569...), validación básica de email.
  - Al crear: el usuario se registra con estado `ACTIVO` por defecto.
  - Al modificar (admin): puede editar nombre, RUT, email, teléfono, contraseña (opcional), rol, departamento y estado.
  - El admin puede eliminar usuarios (botón seguro con confirmación).
- CRUD de sensores (asociados a departamentos): registro, edición y cambiar estado (activar/desactivar).
  - Nota: por diseño el admin sólo puede activar/desactivar sensores y, opcionalmente, administrar usuarios del departamento.
- Historial de accesos: admin puede ver y gestionar; operador puede ver solo en modo lectura.


## Pantallas / Activities importantes (ruta: `app/src/main/java/com/example/appdepartment`)

- `depto_login.kt` — Login y gestión de sesión.
- `depto_gestion_adm.kt` — Menú principal del admin.
- `depto_usuario_bienvenida.kt` — Home del operador.
- `depto_crud_usuarios.kt` — Lista y navegación hacia crear/editar usuarios.
- `depto_crud_usuarios_crear.kt` — Formulario y validaciones para crear usuario.
- `depto_crud_usuarios_modificar.kt` — Formulario completo para editar usuario (admin): ahora carga RUT y teléfono automáticamente y permite eliminar usuario.
- `depto_crud_sensores_*` — Varios ficheros para CRUD de sensores (registro, listar por departamento, modificar estado).
- `depto_control_listado*.kt` — Listados y controles de acceso; incluye variante readonly para operadores.


## Endpoints API usados por `depto*`

A continuación los endpoints que la app `depto*` espera en el servidor (ejemplo base: `http://54.89.22.17/`):

- `listar_departamentos.php` — Devuelve departamentos (id, numero, torre).
- `listar_usuarios_depto.php?id_departamento=...` — Debe devolver lista de usuarios del departamento **incluyendo** `id_usuario, nombre, rut, email, telefono, rol, estado`.
- `crear_usuario.php` — POST para crear usuario (espera nombre, rut, email, telefono, password, rol, id_departamento, estado opcional).
- `obtener_usuario.php?id_usuario=...` — (opcional) devuelve datos completos de un usuario; la app ahora recibe rut/telefono directamente desde la lista para evitar peticiones extras.
- `actualizar_estado_usuario.php` — POST para actualizar estado (y, en la versión actualizada, también acepta actualizar todos los campos del usuario si se envían).
- `eliminar_usuario.php` — POST para eliminar usuario (se soporta DELETE físico; se recomienda considerar soft-delete con `estado='ELIMINADO'` si se quiere mantener historial).

Recomendación: los endpoints deben usar prepared statements (o un framework con ORM) y devolver JSON consistente con campos `error` o `success` para un parsing sencillo en la app.


## Cómo compilar / instalar (rápido)

1. Preparar SDK/Android Studio con SDK que la app requiere.
2. Desde el root del proyecto ejecutar (Linux/macOS):

   ```bash
   ./gradlew assembleDebug
   # o para instalar en dispositivo/emulador conectado:
   ./gradlew installDebug
   ```

3. Si instalas en un dispositivo real, habilita `USB debugging` y acepta el permiso.


## Requisitos del servidor y notas

- Base de datos MySQL/MariaDB `MovilesIOT` (ejemplo). Tabla `usuarios` con columnas mínimas: `id_usuario, nombre, rut, email, telefono, password, rol, estado, id_departamento, fecha_baja`.
- Archivos PHP deben estar en el servidor y devolver JSON correctamente formado y con encabezado `Content-Type: application/json; charset=utf-8`.
- Para depuración de errores 500, activa log de errores PHP o añade `error_reporting(E_ALL); ini_set('display_errors', 1);` temporalmente en los endpoints de pruebas.


## Comportamientos y limitaciones conocidas

- Al crear un usuario la app ahora cierra la pantalla de creación y la lista se recarga en `onResume()`; si no se ven cambios, comprobar la respuesta de `listar_usuarios_depto.php` y cache de Volley.
- El formulario de modificación pre-carga `rut` y `telefono` desde la lista para evitar peticiones adicionales.
- El admin puede eliminar usuarios — la app envía `id_usuario` a `eliminar_usuario.php`. Si recibes error 500, revisa logs del servidor y la implementación del endpoint (ver ejemplo en la carpeta docs).
- Recursos gráficos: algunos layouts utilizan drawables personalizados (icons y colores). Si faltan recursos al linkear, revisa `app/src/main/res/drawable/` y `values/colors.xml`.


## Contacto / Créditos

- Autor (este README): GitHub Copilot
- Código original / autores del proyecto: Sergio Cubelli, Victor Manzano


## Licencia

Este proyecto se publica bajo la licencia MIT.

---

Notas finales

Este README está pensado para ser una guía práctica y enfocada en los módulos `depto*` que se están manteniendo. Si quieres más documentación (diagramas de flujo, tests unitarios o scripts de despliegue), puedo generarlos aparte como siguientes pasos.
