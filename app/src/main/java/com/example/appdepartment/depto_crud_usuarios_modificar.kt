package com.example.appdepartment

import android.os.Bundle
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import cn.pedant.SweetAlert.SweetAlertDialog
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

class depto_crud_usuarios_modificar : AppCompatActivity() {

    private lateinit var editNombre: EditText
    private lateinit var editRut: EditText
    private lateinit var editEmail: EditText
    private lateinit var editTelefono: EditText
    private lateinit var editPassword: EditText
    private lateinit var spinnerRol: Spinner
    private lateinit var spinnerDepto: Spinner
    private lateinit var spinnerEstado: Spinner
    private lateinit var btnGuardar: Button
    private lateinit var btnEliminar: Button

    private var idUsuario: Int = 0
    private var idDeptoSeleccionado: Int = 0
    private val listaDeptosIds = ArrayList<Int>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_depto_crud_usuarios_modificar)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Referencias a los campos
        editNombre = findViewById(R.id.edit_nombre_usuario)
        editRut = findViewById(R.id.edit_rut_usuario)
        editEmail = findViewById(R.id.edit_email_usuario)
        editTelefono = findViewById(R.id.edit_telefono_usuario)
        editPassword = findViewById(R.id.edit_password_usuario)
        spinnerRol = findViewById(R.id.spinner_rol_usuario)
        spinnerDepto = findViewById(R.id.spinner_depto_usuario)
        spinnerEstado = findViewById(R.id.spinner_estado_usuario)
        btnGuardar = findViewById(R.id.btn_actualizar_usuario)
        btnEliminar = findViewById(R.id.btn_eliminar_usuario)

        // Recibir datos del intent
        idUsuario = intent.getIntExtra("id_usuario", 0)
        val nombre = intent.getStringExtra("nombre") ?: ""
        val email = intent.getStringExtra("email") ?: ""
        val rut = intent.getStringExtra("rut") ?: ""
        val telefono = intent.getStringExtra("telefono") ?: ""
        val rol = intent.getStringExtra("rol") ?: "OPERADOR"
        val estadoActual = intent.getStringExtra("estado") ?: "ACTIVO"
        val idDepartamento = intent.getIntExtra("id_departamento", 0)

        // Cargar datos en los campos
        editNombre.setText(nombre)
        editEmail.setText(email)
        editRut.setText(rut)
        editTelefono.setText(telefono)

        // Configurar spinner de rol
        val roles = arrayOf("ADMIN", "OPERADOR")
        val adapterRol = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, roles)
        spinnerRol.adapter = adapterRol
        spinnerRol.setSelection(roles.indexOf(rol))

        // Configurar spinner de estado
        val estados = arrayOf("ACTIVO", "INACTIVO", "BLOQUEADO")
        val adapterEstado = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, estados)
        spinnerEstado.adapter = adapterEstado
        spinnerEstado.setSelection(estados.indexOf(estadoActual))

        // Cargar departamentos
        cargarDepartamentos(idDepartamento)

        // Botón guardar
        btnGuardar.setOnClickListener {
            validarYActualizar()
        }

        // Botón eliminar con confirmación
        btnEliminar.setOnClickListener {
            confirmarEliminacion(nombre)
        }
    }


    private fun cargarDepartamentos(idDeptoActual: Int) {
        val url = "http://54.89.22.17/listar_departamentos.php"

        val request = StringRequest(
            Request.Method.GET, url,
            { response ->
                val array = JSONArray(response)
                val nombres = ArrayList<String>()
                listaDeptosIds.clear()

                var posicionActual = 0
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    val idDepto = obj.getInt("id_departamento")
                    nombres.add("Depto ${obj.getString("numero")} - Torre ${obj.getString("torre")}")
                    listaDeptosIds.add(idDepto)

                    if (idDepto == idDeptoActual) {
                        posicionActual = i
                    }
                }

                val adapterDepto = ArrayAdapter(
                    this,
                    android.R.layout.simple_spinner_item,
                    nombres
                )
                adapterDepto.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                spinnerDepto.adapter = adapterDepto
                spinnerDepto.setSelection(posicionActual)

                spinnerDepto.setOnItemSelectedListener(object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(parent: AdapterView<*>, view: android.view.View?, pos: Int, id: Long) {
                        idDeptoSeleccionado = listaDeptosIds[pos]
                    }
                    override fun onNothingSelected(parent: AdapterView<*>) {}
                })

                // Establecer el departamento actual
                idDeptoSeleccionado = idDeptoActual
            },
            { error ->
                Toast.makeText(this, "Error al cargar departamentos", Toast.LENGTH_SHORT).show()
            }
        )

        Volley.newRequestQueue(this).add(request)
    }

    private fun validarYActualizar() {
        val nombre = editNombre.text.toString().trim()
        val rut = editRut.text.toString().trim()
        val email = editEmail.text.toString().trim()
        val telefono = editTelefono.text.toString().trim()
        val password = editPassword.text.toString().trim()
        val rol = spinnerRol.selectedItem.toString()
        val estado = spinnerEstado.selectedItem.toString()

        // Validar campos obligatorios
        if (nombre.isEmpty() || rut.isEmpty() || email.isEmpty() || telefono.isEmpty()) {
            alerta("Campos incompletos", "Por favor, completa todos los campos obligatorios.")
            return
        }

        // Validar RUT
        if (!validarRut(rut)) {
            alerta("RUT inválido", "Ingrese un RUT válido. Ej: 12.345.678-5")
            return
        }

        // Validar Email
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            alerta("Email incorrecto", "Ingrese un email válido.")
            return
        }

        // Validar Teléfono
        val regexTelefono = Regex("^\\+569[0-9]{8}\$")
        if (!regexTelefono.matches(telefono)) {
            alerta("Teléfono inválido", "Debes ingresar un número válido: +569XXXXXXXX")
            return
        }

        // Validar Contraseña solo si se ingresó una nueva
        if (password.isNotEmpty() && !validarPassword(password)) {
            alerta(
                "Contraseña insegura",
                "Debe contener:\n- Mínimo 8 caracteres\n- 1 mayúscula\n- 1 minúscula\n- 1 número\n- 1 caracter especial"
            )
            return
        }

        // Si todo es válido, actualizar
        actualizarUsuario(nombre, rut, email, telefono, password, rol, estado)
    }

    private fun actualizarUsuario(
        nombre: String,
        rut: String,
        email: String,
        telefono: String,
        password: String,
        rol: String,
        estado: String
    ) {
        val url = "http://54.89.22.17/actualizar_estado_usuario.php"

        val request = object : StringRequest(
            Method.POST, url,
            { response ->
                try {
                    val json = JSONObject(response)

                    if (json.has("error")) {
                        alerta("Error", json.getString("error"))
                    } else {
                        SweetAlertDialog(this, SweetAlertDialog.SUCCESS_TYPE)
                            .setTitleText("Usuario Actualizado")
                            .setContentText("Los datos del usuario se actualizaron correctamente.")
                            .setConfirmText("OK")
                            .setConfirmClickListener {
                                it.dismissWithAnimation()
                                setResult(RESULT_OK)
                                finish()
                            }
                            .show()
                    }
                } catch (e: Exception) {
                    alerta("Error", "Respuesta inesperada del servidor")
                }
            },
            { error ->
                alerta("Error de conexión", "No se pudo contactar al servidor: ${error.message}")
            }
        ) {
            override fun getParams(): MutableMap<String, String> {
                val params = HashMap<String, String>()
                params["id_usuario"] = idUsuario.toString()
                params["nombre"] = nombre
                params["rut"] = rut
                params["email"] = email
                params["telefono"] = telefono
                params["rol"] = rol
                params["estado"] = estado
                params["id_departamento"] = idDeptoSeleccionado.toString()

                // Solo enviar contraseña si se ingresó una nueva
                if (password.isNotEmpty()) {
                    params["password"] = password
                }

                // Si el estado es INACTIVO o BLOQUEADO, enviar fecha_baja
                if (estado == "INACTIVO" || estado == "BLOQUEADO") {
                    val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                    params["fecha_baja"] = sdf.format(Date())
                } else {
                    params["fecha_baja"] = ""
                }

                return params
            }
        }

        Volley.newRequestQueue(this).add(request)
    }

    private fun alerta(titulo: String, mensaje: String) {
        SweetAlertDialog(this, SweetAlertDialog.ERROR_TYPE)
            .setTitleText(titulo)
            .setContentText(mensaje)
            .show()
    }

    private fun validarPassword(pass: String): Boolean {
        val regex = Regex("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@#\$%^&+=!¿?*/._-]).{8,}\$")
        return regex.matches(pass)
    }

    private fun validarRut(rut: String): Boolean {
        val clean = rut.replace(".", "").replace("-", "").lowercase()
        if (clean.length < 8) return false

        val cuerpo = clean.dropLast(1)
        val dv = clean.last()

        var suma = 0
        var multiplo = 2

        for (i in cuerpo.reversed()) {
            suma += (i.toString().toInt() * multiplo)
            multiplo = if (multiplo == 7) 2 else multiplo + 1
        }

        val resultado = 11 - (suma % 11)
        val dvEsperado = when (resultado) {
            11 -> '0'
            10 -> 'k'
            else -> resultado.toString()[0]
        }

        return dv == dvEsperado
    }

    private fun confirmarEliminacion(nombreUsuario: String) {
        SweetAlertDialog(this, SweetAlertDialog.WARNING_TYPE)
            .setTitleText("¿Eliminar Usuario?")
            .setContentText("¿Estás seguro de eliminar a '$nombreUsuario'?\n\nEsta acción NO se puede deshacer.")
            .setConfirmText("Sí, eliminar")
            .setCancelText("Cancelar")
            .showCancelButton(true)
            .setConfirmClickListener { dialog ->
                dialog.dismissWithAnimation()
                eliminarUsuario()
            }
            .setCancelClickListener { dialog ->
                dialog.dismissWithAnimation()
            }
            .show()
    }

    private fun eliminarUsuario() {
        val url = "http://54.89.22.17/eliminar_usuario.php"

        println("🗑️ Eliminando usuario ID: $idUsuario")
        println("🔗 URL: $url")

        val request = object : StringRequest(
            Method.POST, url,
            { response ->
                try {
                    println("📥 Respuesta completa del servidor: $response")
                    println("📏 Longitud de respuesta: ${response.length}")

                    val json = JSONObject(response)

                    // Verificar si hay error
                    if (json.has("error")) {
                        val errorMsg = json.getString("error")
                        println("❌ Error del servidor: $errorMsg")
                        alerta("Error", errorMsg)
                    }
                    // Verificar si fue exitoso (tu API usa "success")
                    else if (json.optBoolean("success", false)) {
                        println("✅ Usuario eliminado exitosamente")
                        SweetAlertDialog(this, SweetAlertDialog.SUCCESS_TYPE)
                            .setTitleText("Usuario Eliminado")
                            .setContentText("El usuario ha sido eliminado correctamente.")
                            .setConfirmText("OK")
                            .setConfirmClickListener {
                                it.dismissWithAnimation()
                                setResult(RESULT_OK)
                                finish()
                            }
                            .show()
                    } else {
                        println("⚠️ Respuesta sin success ni error")
                        alerta("Error", "No se pudo eliminar el usuario - respuesta incompleta")
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    println("❌ Excepción al procesar respuesta: ${e.message}")
                    println("❌ Stack trace: ${e.stackTraceToString()}")
                    alerta("Error al procesar", "Respuesta del servidor: $response\n\nError: ${e.message}")
                }
            },
            { error ->
                error.printStackTrace()
                println("❌ Error de Volley: ${error.javaClass.simpleName}")
                println("❌ Mensaje: ${error.message}")

                val mensajeError = when {
                    error.networkResponse != null -> {
                        val statusCode = error.networkResponse.statusCode
                        val headers = error.networkResponse.headers
                        val data = try {
                            String(error.networkResponse.data, Charsets.UTF_8)
                        } catch (e: Exception) {
                            "No se pudo leer el contenido"
                        }

                        println("📊 Status Code: $statusCode")
                        println("📋 Headers: $headers")
                        println("📄 Contenido de respuesta: $data")

                        when (statusCode) {
                            500 -> "Error 500: Error interno del servidor PHP.\n\nPosibles causas:\n- Error de sintaxis en el PHP\n- Problema con la base de datos\n- Campo faltante en la tabla\n\nContenido: $data"
                            404 -> "Error 404: El archivo eliminar_usuario.php no existe en el servidor"
                            403 -> "Error 403: Acceso prohibido al archivo PHP"
                            else -> "Error HTTP $statusCode\n\nRespuesta del servidor:\n$data"
                        }
                    }
                    else -> "Error de red: ${error.message ?: "Sin conexión al servidor"}"
                }

                Toast.makeText(this, mensajeError, Toast.LENGTH_LONG).show()
                alerta("Error de conexión", mensajeError)
            }
        ) {
            override fun getParams(): MutableMap<String, String> {
                val params = HashMap<String, String>()
                params["id_usuario"] = idUsuario.toString()
                println("📤 Parámetros POST a enviar: $params")
                return params
            }

            override fun getHeaders(): MutableMap<String, String> {
                val headers = HashMap<String, String>()
                headers["Content-Type"] = "application/x-www-form-urlencoded; charset=UTF-8"
                println("📨 Headers: $headers")
                return headers
            }
        }

        // Configurar timeout más largo
        request.setRetryPolicy(
            com.android.volley.DefaultRetryPolicy(
                10000, // 10 segundos de timeout
                0, // No reintentar
                com.android.volley.DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
            )
        )

        Volley.newRequestQueue(this).add(request)
    }
}