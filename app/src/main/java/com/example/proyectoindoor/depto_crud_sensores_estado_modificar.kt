package com.example.proyectoindoor

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.ArrayAdapter
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import cn.pedant.SweetAlert.SweetAlertDialog
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import org.json.JSONArray
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class depto_crud_sensores_estado_modificar : AppCompatActivity() {

    private lateinit var editUid: EditText
    private lateinit var spinnerEstado: Spinner
    private lateinit var spinnerTipo: Spinner
    private lateinit var spinnerUsuario: Spinner
    private lateinit var btnGuardar: Button
    private lateinit var btnEliminar: Button

    private var idSensor: Int = 0
    private var idDepartamento: Int = 0
    private var idUsuarioActual: Int = 0
    
    // Lista para guardar los IDs de usuarios
    private val listaUsuariosIds = ArrayList<Int>()
    private val listaUsuariosNombres = ArrayList<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_depto_crud_sensores_estado_modificar)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        editUid = findViewById(R.id.edit_uid)
        spinnerEstado = findViewById(R.id.spinner_estado)
        spinnerTipo = findViewById(R.id.spinner_tipo)
        spinnerUsuario = findViewById(R.id.spinner_usuario)
        btnGuardar = findViewById(R.id.btn_guardar_cambios)
        btnEliminar = findViewById(R.id.btn_eliminar_sensor2)

        // === RECIBIR DATOS ===
        idSensor = intent.getIntExtra("id_sensor", 0)
        val uid = intent.getStringExtra("codigo_sensor")
        val estado = intent.getStringExtra("estado")
        val tipo = intent.getStringExtra("tipo")
        idUsuarioActual = intent.getIntExtra("id_usuario", 0)
        idDepartamento = intent.getIntExtra("id_departamento", 0)

        editUid.setText(uid)

        // 🔒 DESHABILITAR EDICIÓN - Solo cambiar estado
        editUid.isEnabled = false
        editUid.isFocusable = false
        editUid.alpha = 0.6f

        val estados = arrayOf("ACTIVO", "INACTIVO", "BLOQUEADO", "PERDIDO")
        spinnerEstado.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, estados)
        spinnerEstado.setSelection(estados.indexOf(estado))

        val tipos = arrayOf("TARJETA", "LLAVERO")
        spinnerTipo.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, tipos)
        spinnerTipo.setSelection(tipos.indexOf(tipo))

        // 🔒 DESHABILITAR CAMBIO DE TIPO
        spinnerTipo.isEnabled = false
        spinnerTipo.isClickable = false
        spinnerTipo.alpha = 0.6f

        // Cargar usuarios del departamento
        cargarUsuariosDepto()

        btnGuardar.setOnClickListener {
            actualizarSensor()
        }

        btnEliminar.setOnClickListener {
            confirmarEliminarSensor()
        }
    }

    private fun cargarUsuariosDepto() {
        val url = "http://54.89.22.17/listar_usuarios_depto.php?id_departamento=$idDepartamento"

        val request = StringRequest(
            Request.Method.GET,
            url,
            { response ->
                try {
                    val jsonArray = JSONArray(response)
                    
                    listaUsuariosIds.clear()
                    listaUsuariosNombres.clear()

                    for (i in 0 until jsonArray.length()) {
                        val obj = jsonArray.getJSONObject(i)
                        val idUsuario = obj.getInt("id_usuario")
                        val nombre = obj.getString("nombre")
                        val rol = obj.getString("rol")

                        listaUsuariosIds.add(idUsuario)
                        listaUsuariosNombres.add("$nombre ($rol)")
                    }

                    val adapter = ArrayAdapter(
                        this,
                        android.R.layout.simple_spinner_dropdown_item,
                        listaUsuariosNombres
                    )
                    spinnerUsuario.adapter = adapter

                    // Seleccionar el usuario actual del sensor
                    val indexActual = listaUsuariosIds.indexOf(idUsuarioActual)
                    if (indexActual >= 0) {
                        spinnerUsuario.setSelection(indexActual)
                    }

                    // 🔒 DESHABILITAR CAMBIO DE USUARIO
                    spinnerUsuario.isEnabled = false
                    spinnerUsuario.isClickable = false
                    spinnerUsuario.alpha = 0.6f

                } catch (e: Exception) {
                    e.printStackTrace()
                }
            },
            { error ->
                error.printStackTrace()
            }
        )

        Volley.newRequestQueue(this).add(request)
    }

    private fun actualizarSensor() {
        val uidNuevo = editUid.text.toString().trim()
        val estadoNuevo = spinnerEstado.selectedItem.toString()
        val tipoNuevo = spinnerTipo.selectedItem.toString()
        
        // El usuario NO se puede cambiar, mantener el actual
        val idUsuarioNuevo = idUsuarioActual

        if (uidNuevo.isEmpty()) {
            SweetAlertDialog(this, SweetAlertDialog.ERROR_TYPE)
                .setTitleText("Error")
                .setContentText("Debe ingresar un UID válido")
                .show()
            return
        }

        val url = "http://54.89.22.17/actualizar_sensor.php"

        val request = object : StringRequest(
            Method.POST, url,
            { response ->
                SweetAlertDialog(this, SweetAlertDialog.SUCCESS_TYPE)
                    .setTitleText("Estado Actualizado")
                    .setContentText("El estado del sensor se cambió correctamente.")
                    .setConfirmText("OK")
                    .setConfirmClickListener {
                        setResult(RESULT_OK)
                        finish()
                    }
                    .show()
            },
            { error ->
                SweetAlertDialog(this, SweetAlertDialog.ERROR_TYPE)
                    .setTitleText("Error")
                    .setContentText("No se pudo actualizar el sensor: ${error.message}")
                    .show()
            }
        ) {
            override fun getParams(): MutableMap<String, String> {
                val params = HashMap<String, String>()
                params["id_sensor"] = idSensor.toString()
                params["codigo_sensor"] = uidNuevo
                params["estado"] = estadoNuevo
                params["tipo"] = tipoNuevo
                params["id_usuario"] = idUsuarioNuevo.toString()
                
                // Si el estado es INACTIVO, PERDIDO o BLOQUEADO, enviar fecha_baja
                val estadosBaja = listOf("INACTIVO", "PERDIDO", "BLOQUEADO")
                if (estadoNuevo in estadosBaja) {
                    val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                    params["fecha_baja"] = sdf.format(Date())
                }
                
                return params
            }
        }

        Volley.newRequestQueue(this).add(request)
    }

    private fun confirmarEliminarSensor() {
        SweetAlertDialog(this, SweetAlertDialog.WARNING_TYPE)
            .setTitleText("¿Eliminar sensor?")
            .setContentText("Esta acción no se puede deshacer. Se eliminarán también los eventos asociados.")
            .setConfirmText("Sí, eliminar")
            .setCancelText("Cancelar")
            .setConfirmClickListener { dialog ->
                dialog.dismiss()
                eliminarSensor()
            }
            .setCancelClickListener { dialog ->
                dialog.dismiss()
            }
            .show()
    }

    private fun eliminarSensor() {
        val url = "http://54.89.22.17/eliminar_sensor.php"

        val loadingDialog = SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE)
        loadingDialog.titleText = "Eliminando..."
        loadingDialog.setCancelable(false)
        loadingDialog.show()

        val request = object : StringRequest(
            Method.POST, url,
            { response ->
                loadingDialog.dismiss()
                SweetAlertDialog(this, SweetAlertDialog.SUCCESS_TYPE)
                    .setTitleText("Eliminado")
                    .setContentText("El sensor ha sido eliminado correctamente.")
                    .setConfirmText("OK")
                    .setConfirmClickListener {
                        setResult(RESULT_OK)
                        finish()
                    }
                    .show()
            },
            { error ->
                loadingDialog.dismiss()
                SweetAlertDialog(this, SweetAlertDialog.ERROR_TYPE)
                    .setTitleText("Error")
                    .setContentText("No se pudo eliminar el sensor")
                    .show()
            }
        ) {
            override fun getParams(): MutableMap<String, String> {
                val params = HashMap<String, String>()
                params["id_sensor"] = idSensor.toString()
                return params
            }
        }

        Volley.newRequestQueue(this).add(request)
    }
}

