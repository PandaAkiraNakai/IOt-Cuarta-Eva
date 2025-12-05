# Cambios: Historial de Acceso para Operadores (Solo Lectura)

## Resumen
Se ha implementado la funcionalidad para que los **operadores (usuarios normales)** puedan ver el historial completo de accesos del departamento en **modo solo lectura**, sin los controles de habilitación/deshabilitación que tiene el administrador.

## Archivos Creados

### 1. `depto_control_listado_acceso_readonly.kt`
**Ubicación:** `/app/src/main/java/com/example/proyectoindoor/`

**Propósito:** Actividad que muestra el detalle de un evento de acceso en modo solo lectura.

**Características:**
- Muestra información completa del evento:
  - Estado (PERMITIDO/DENEGADO)
  - Fecha y hora
  - Usuario que realizó el acceso
  - Tipo de evento
  - Código del sensor
- **NO incluye botones** de "Permitir Acceso" ni "Denegar Acceso"
- Colores diferenciados según el resultado (verde para PERMITIDO, rojo para DENEGADO)
- Indicador visual "(Solo lectura)" en el subtítulo

### 2. `activity_depto_control_listado_acceso_readonly.xml`
**Ubicación:** `/app/src/main/res/layout/`

**Propósito:** Layout XML para la pantalla de detalle en modo solo lectura.

**Características:**
- Diseño similar al del administrador pero sin botones de acción
- Logo de la aplicación en la parte superior
- Título: "Detalle del Evento"
- Subtítulo: "(Solo lectura)"
- 5 campos de información:
  1. Estado
  2. Fecha
  3. Usuario
  4. Tipo de evento
  5. Sensor

## Archivos Modificados

### 1. `depto_usuario_historial.kt`
**Cambios realizados:**

#### Antes:
- Mostraba solo los eventos del **usuario actual**
- Usaba el endpoint: `listar_eventos_usuario.php?id_usuario=X`
- No permitía ver detalles al hacer clic

#### Después:
- Muestra **todos los eventos del departamento** (como el admin)
- Usa el endpoint: `listar_eventos.php?id_departamento=X`
- Incluye nombre de usuario en cada evento
- Permite hacer clic en un evento para ver detalles
- Al hacer clic, abre `depto_control_listado_acceso_readonly` (sin botones de control)

**Variables agregadas:**
```kotlin
private val listaEventosData = ArrayList<JSONObject>()
private var idDepartamento: Int = 0
```

**Nuevo listener de clic:**
```kotlin
listView.setOnItemClickListener { _, _, position, _ ->
    if (listaEventosData.isNotEmpty() && position < listaEventosData.size) {
        val evento = listaEventosData[position]
        val intent = Intent(this, depto_control_listado_acceso_readonly::class.java)
        // ... pasar datos del evento
        startActivity(intent)
    }
}
```

### 2. `AndroidManifest.xml`
**Cambio realizado:**
- Agregada la nueva actividad `depto_control_listado_acceso_readonly`

```xml
<activity
    android:name=".depto_control_listado_acceso_readonly"
    android:exported="false" />
```

## Comparación: Admin vs Operador

### Administrador
- **Actividad de listado:** `depto_control_listado`
- **Actividad de detalle:** `depto_control_listado_acceso`
- **Puede:**
  - Ver todos los eventos del departamento ✓
  - Ver detalles de cada evento ✓
  - **Permitir acceso manualmente** ✓
  - **Denegar acceso manualmente** ✓
  - **Activar la barrera** ✓

### Operador (Usuario Normal)
- **Actividad de listado:** `depto_usuario_historial`
- **Actividad de detalle:** `depto_control_listado_acceso_readonly`
- **Puede:**
  - Ver todos los eventos del departamento ✓
  - Ver detalles de cada evento ✓
  - **Permitir acceso manualmente** ✗
  - **Denegar acceso manualmente** ✗
  - **Activar la barrera** ✗

## Flujo de Usuario (Operador)

1. **Login:** Usuario normal inicia sesión
2. **Menú principal:** Selecciona "Ver Historial de Accesos"
3. **Lista de eventos:** Ve todos los eventos del departamento con:
   - Icono de estado (✅/❌)
   - Tipo de evento
   - Fecha y hora
   - Código del sensor
   - Nombre del usuario
4. **Clic en evento:** Al tocar un evento, se abre la pantalla de detalle
5. **Detalle (solo lectura):** Muestra toda la información del evento
   - **NO hay botones** de acción
   - Indicador visual "(Solo lectura)"

## Endpoints Utilizados

### Por el Operador:
- **GET** `http://54.89.22.17/listar_eventos.php?id_departamento=X`
  - Devuelve todos los eventos del departamento
  - Incluye información de usuarios y sensores

### Por el Admin (sin cambios):
- **GET** `http://54.89.22.17/listar_eventos.php?id_departamento=X`
- **POST** `http://54.89.22.17/registrar_evento_manual.php`
- **POST** `http://54.89.22.17/control_servo.php`

## Pruebas Recomendadas

1. **Login como operador**
2. **Navegar a "Ver Historial"**
3. **Verificar que se muestran todos los eventos del departamento** (no solo los del usuario)
4. **Hacer clic en varios eventos**
5. **Verificar que NO aparecen botones de acción**
6. **Verificar que se muestra el texto "(Solo lectura)"**
7. **Comparar con la vista del administrador** para confirmar las diferencias

## Estado del Proyecto

✅ **Compilación exitosa:** `BUILD SUCCESSFUL`
✅ **Archivos creados:** 2 nuevos archivos
✅ **Archivos modificados:** 2 archivos existentes
✅ **Sin errores de compilación**
✅ **Warnings:** Solo deprecaciones menores (getColor)

## Próximos Pasos (Opcional)

Si se desea mejorar aún más:

1. **Filtros:** Agregar filtros por fecha, tipo de evento, resultado
2. **Búsqueda:** Permitir buscar por usuario o sensor
3. **Exportar:** Opción para exportar el historial a PDF o CSV
4. **Estadísticas:** Mostrar gráficos de accesos permitidos vs denegados
5. **Notificaciones:** Alertar al operador de eventos importantes

---

**Autor:** Sistema de Control de Acceso IoT  
**Fecha:** 2025-12-04  
**Versión:** 1.0

