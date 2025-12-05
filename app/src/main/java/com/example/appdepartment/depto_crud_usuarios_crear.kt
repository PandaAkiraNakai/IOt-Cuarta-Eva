package com.example.appdepartment

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import cn.pedant.SweetAlert.SweetAlertDialog
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import org.json.JSONArray
import org.json.JSONObject

class depto_crud_usuarios_crear : AppCompatActivity() {

    private lateinit var editNombre: EditText
    private lateinit var editRut: EditText
    private lateinit var editEmail: EditText
    private lateinit var editTelefono: EditText
    private lateinit var editPassword: EditText
    private lateinit var spinnerRol: Spinner
    private lateinit var spinnerDepto: Spinner
    private lateinit var btnGuardar: Button

    private var idDeptoSeleccionado = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_depto_crud_usuarios_crear)

        editNombre = findViewById(R.id.edit_nombre_usuario)
        editRut = findViewById(R.id.edit_rut_usuario)
        editEmail = findViewById(R.id.edit_email_usuario)
        editTelefono = findViewById(R.id.edit_telefono_usuario)
        editPassword = findViewById(R.id.edit_password_usuario)
        spinnerRol = findViewById(R.id.spinner_rol_usuario)
        spinnerDepto = findViewById(R.id.spinner_depto_usuario)
        btnGuardar = findViewById(R.id.btn_guardar_usuario)

        // ============================
        // SPINNER ROL (MEJORADO)
        // ============================
        val adapterRol = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            arrayOf("ADMIN", "OPERADOR")
        )
        adapterRol.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerRol.adapter = adapterRol

        cargarDepartamentos()

        btnGuardar.setOnClickListener {
            validarYRegistrar()
        }
    }

    private fun cargarDepartamentos() {
        val url = "http://54.89.22.17/listar_departamentos.php"

        val request = StringRequest(
            Request.Method.GET, url,
            { response ->
                val array = JSONArray(response)
                val nombres = ArrayList<String>()
                val ids = ArrayList<Int>()

                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    nombres.add("Depto ${obj.getString("numero")} - Torre ${obj.getString("torre")}")
                    ids.add(obj.getInt("id_departamento"))
                }

                val adapterDepto = ArrayAdapter(
                    this,
                    android.R.layout.simple_spinner_item,
                    nombres
                )
                adapterDepto.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                spinnerDepto.adapter = adapterDepto

                spinnerDepto.setOnItemSelectedListener(object :
                    AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(
                        parent: AdapterView<*>,
                        view: android.view.View?,
                        pos: Int,
                        id: Long
                    ) {
                        idDeptoSeleccionado = ids[pos]
                    }

                    override fun onNothingSelected(parent: AdapterView<*>) {}
                })
            },
            {
                SweetAlertDialog(this, SweetAlertDialog.ERROR_TYPE)
                    .setTitleText("Error")
                    .setContentText("No se pudieron cargar los departamentos")
                    .show()
            }
        )

        Volley.newRequestQueue(this).add(request)
    }

    // =======================================================
    // VALIDAR DATOS DE USUARIO
    // =======================================================
    private fun validarYRegistrar() {

        val nombre = editNombre.text.toString().trim()
        val rut = editRut.text.toString().trim()
        val email = editEmail.text.toString().trim()
        val telefono = editTelefono.text.toString().trim()
        val password = editPassword.text.toString().trim()
        val rol = spinnerRol.selectedItem.toString()

        // Vacíos
        if (nombre.isEmpty() || rut.isEmpty() || email.isEmpty() ||
            telefono.isEmpty() || password.isEmpty() || idDeptoSeleccionado == 0
        ) {
            alerta("Faltan datos", "Completa todos los campos obligatorios.")
            return
        }

        // Validar Rut
        if (!validarRut(rut)) {
            alerta("RUT inválido", "Ingrese un RUT válido. Ej: 12.345.678-5")
            return
        }

        // Validar Email
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            alerta("Email incorrecto", "Ingrese un email válido.")
            return
        }

        // Validar Teléfono Chile
        val regexTelefono = Regex("^\\+569[0-9]{8}\$")

        if (!regexTelefono.matches(telefono)) {
            alerta("Teléfono inválido", "Debes ingresar un número válido: +569XXXXXXXX")
            return
        }

        // Validar Contraseña segura
        if (!validarPassword(password)) {
            alerta(
                "Contraseña insegura",
                "Debe contener:\n- Mínimo 8 caracteres\n- 1 mayúscula\n- 1 minúscula\n- 1 número\n- 1 caracter especial"
            )
            return
        }

        // Si todo ok → Registrar
        registrarUsuario(nombre, rut, email, telefono, password, rol)
    }

    // =======================================================
    // REGISTRAR USUARIO EN EL SERVIDOR
    // =======================================================
    private fun registrarUsuario(
        nombre: String,
        rut: String,
        email: String,
        telefono: String,
        password: String,
        rol: String
    ) {
        val url = "http://54.89.22.17/crear_usuario.php"

        println("📤 Registrando usuario: $nombre - Depto: $idDeptoSeleccionado - Rol: $rol")

        val request = object : StringRequest(
            Method.POST, url,
            apply@{ response ->
                println("📥 Respuesta del servidor: $response")

                val json = JSONObject(response)

                if (json.has("error")) {
                    alerta("Error", json.getString("error"))
                    return@apply
                }

                SweetAlertDialog(this, SweetAlertDialog.SUCCESS_TYPE)
                    .setTitleText("Usuario creado")
                    .setContentText("El usuario fue registrado correctamente")
                    .setConfirmClickListener {
                        it.dismissWithAnimation()
                        // Volver a la lista de usuarios
                        finish()
                    }
                    .show()
            },
            { error ->
                println("❌ Error de conexión: ${error.message}")
                alerta("Error de conexión", "No se pudo contactar al servidor.")
            }
        ) {
            override fun getParams(): MutableMap<String, String> {
                val p = HashMap<String, String>()
                p["nombre"] = nombre
                p["rut"] = rut
                p["email"] = email
                p["telefono"] = telefono
                p["password"] = password
                p["rol"] = rol
                p["id_departamento"] = idDeptoSeleccionado.toString()
                p["estado"] = "ACTIVO" // Asegurar que se cree como ACTIVO

                println("📋 Parámetros enviados: $p")
                return p
            }
        }

        Volley.newRequestQueue(this).add(request)
    }

    // =======================================================
    // UTILIDADES
    // =======================================================

    private fun alerta(titulo: String, mensaje: String) {
        SweetAlertDialog(this, SweetAlertDialog.ERROR_TYPE)
            .setTitleText(titulo)
            .setContentText(mensaje)
            .show()
    }

    // Validación de contraseña fuerte
    private fun validarPassword(pass: String): Boolean {
        val regex = Regex("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@#\$%^&+=!¿?*/._-]).{8,}\$")
        return regex.matches(pass)
    }

    // Validación Rut Chileno real (DV)
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
}
