package com.example.proyectoindoor

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

class depto_crud_sensores_registro : AppCompatActivity() {

    private var idDeptoSeleccionado: Int = 0
    private lateinit var spinnerDepartamento: Spinner
    private lateinit var spinnerTipoRfid: Spinner
    private lateinit var uidEditText: EditText
    private lateinit var btnRegistrar: Button

    // ID del usuario admin obtenido desde la sesión
    private var idUsuarioAdmin: Int = 0

    // Listas para guardar IDs y nombres de departamentos
    private val listaDeptosIds = ArrayList<Int>()
    private val listaDeptosNombres = ArrayList<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_depto_crud_sensores_registro)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Obtener datos de la sesión del admin logueado
        val prefs = getSharedPreferences("session", MODE_PRIVATE)
        idUsuarioAdmin = prefs.getInt("id_usuario", 0)
        idDeptoSeleccionado = prefs.getInt("id_departamento", 0)

        // Referencias
        spinnerDepartamento = findViewById(R.id.spinner_departamento)
        spinnerTipoRfid = findViewById(R.id.spinnerTipoRfid)
        uidEditText = findViewById(R.id.uid1)
        btnRegistrar = findViewById(R.id.btn_RegistrarRfidFinal)

        // Spinner de tipo RFID
        val adapterTipo = ArrayAdapter.createFromResource(
            this,
            R.array.tipo_rfid_array,
            android.R.layout.simple_spinner_item
        )
        adapterTipo.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerTipoRfid.adapter = adapterTipo

        // Cargar departamentos disponibles
        cargarDepartamentos()

        // Acción del botón registrar
        btnRegistrar.setOnClickListener {
            validarYRegistrar()
        }
    }

    // CARGAR DEPARTAMENTOS DISPONIBLES EN EL SPINNER
    private fun cargarDepartamentos() {
        val url = "http://54.89.22.17/listar_departamentos.php"

        val request = StringRequest(
            Request.Method.GET,
            url,
            { response ->
                try {
                    val jsonArray = JSONArray(response)

                    listaDeptosIds.clear()
                    listaDeptosNombres.clear()

                    for (i in 0 until jsonArray.length()) {
                        val obj = jsonArray.getJSONObject(i)
                        val id = obj.getInt("id_departamento")
                        val numero = obj.getString("numero")
                        val torre = obj.getString("torre")

                        listaDeptosIds.add(id)
                        listaDeptosNombres.add("Depto $numero - Torre $torre")
                    }

                    val adapter = ArrayAdapter(
                        this,
                        android.R.layout.simple_spinner_dropdown_item,
                        listaDeptosNombres
                    )
                    spinnerDepartamento.adapter = adapter

                    // Habilitar el spinner
                    spinnerDepartamento.isEnabled = true
                    spinnerDepartamento.isClickable = true

                    // Seleccionar el departamento del admin por defecto si existe
                    if (idDeptoSeleccionado != 0) {
                        val index = listaDeptosIds.indexOf(idDeptoSeleccionado)
                        if (index >= 0) {
                            spinnerDepartamento.setSelection(index)
                        }
                    }

                } catch (e: Exception) {
                    e.printStackTrace()
                    Toast.makeText(this, "Error al cargar departamentos", Toast.LENGTH_SHORT).show()
                }
            },
            { error ->
                Toast.makeText(this, "Error de conexión al cargar departamentos", Toast.LENGTH_SHORT).show()
            }
        )

        Volley.newRequestQueue(this).add(request)
    }

    // VALIDA CAMPOS
    private fun validarYRegistrar() {

        val uid = uidEditText.text.toString().trim()

        if (uid.isEmpty()) {
            SweetAlertDialog(this, SweetAlertDialog.ERROR_TYPE)
                .setTitleText("UID vacío")
                .setContentText("Debe ingresar un UID válido.")
                .show()
            return
        }

        // Obtener ID del departamento seleccionado del spinner
        val posicionDepto = spinnerDepartamento.selectedItemPosition
        idDeptoSeleccionado = if (posicionDepto >= 0 && posicionDepto < listaDeptosIds.size) {
            listaDeptosIds[posicionDepto]
        } else {
            0
        }

        if (idDeptoSeleccionado == 0) {
            SweetAlertDialog(this, SweetAlertDialog.WARNING_TYPE)
                .setTitleText("Falta Departamento")
                .setContentText("Debe seleccionar un departamento.")
                .show()
            return
        }

        registrarSensor(uid)
    }

    // REGISTRA SENSOR EN EL SERVIDOR
    private fun registrarSensor(uid: String) {

        val tipoRfid = spinnerTipoRfid.selectedItem.toString()

        // 📌 OBTENER FECHA ACTUAL DEL CELULAR
        val fechaActual = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            .format(Date())

        val url = "http://54.89.22.17/registrar_sensor.php"

        val request = object : StringRequest(
            Method.POST, url,
            StringRequest@{ response ->

                val json = JSONObject(response)

                if (json.has("error")) {
                    SweetAlertDialog(this, SweetAlertDialog.ERROR_TYPE)
                        .setTitleText("Error")
                        .setContentText(json.getString("error"))
                        .show()
                    return@StringRequest
                }

                SweetAlertDialog(this, SweetAlertDialog.SUCCESS_TYPE)
                    .setTitleText("Registrado")
                    .setContentText("Sensor ${json.getString("uid")} agregado.")
                    .show()

                uidEditText.setText("")
            },
            {
                SweetAlertDialog(this, SweetAlertDialog.ERROR_TYPE)
                    .setTitleText("Error Servidor")
                    .setContentText("No se pudo registrar el sensor.")
                    .show()
            }
        ) {

            override fun getParams(): MutableMap<String, String> {
                val params = HashMap<String, String>()

                params["codigo_sensor"] = uid
                params["tipo"] = tipoRfid
                params["id_usuario"] = idUsuarioAdmin.toString()
                params["id_departamento"] = idDeptoSeleccionado.toString()
                params["estado"] = "ACTIVO"

                // 📌 ENVIAMOS LA FECHA DEL CELULAR
                params["fecha_alta"] = fechaActual

                return params
            }
        }

        Volley.newRequestQueue(this).add(request)
    }
}

