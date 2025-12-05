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
import java.text.SimpleDateFormat
import java.util.*

class depto_crud_usuarios_modificar : AppCompatActivity() {

    private lateinit var txtNombre: TextView
    private lateinit var txtEmail: TextView
    private lateinit var txtRol: TextView
    private lateinit var spinnerEstado: Spinner
    private lateinit var btnGuardar: Button

    private var idUsuario: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_depto_crud_usuarios_modificar)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Referencias
        txtNombre = findViewById(R.id.txt_nombre_usuario)
        txtEmail = findViewById(R.id.txt_email_usuario)
        txtRol = findViewById(R.id.txt_rol_usuario)
        spinnerEstado = findViewById(R.id.spinner_estado_usuario)
        btnGuardar = findViewById(R.id.btn_actualizar_usuario)

        // Recibir datos
        idUsuario = intent.getIntExtra("id_usuario", 0)
        val nombre = intent.getStringExtra("nombre") ?: ""
        val email = intent.getStringExtra("email") ?: ""
        val rol = intent.getStringExtra("rol") ?: ""
        val estadoActual = intent.getStringExtra("estado") ?: "ACTIVO"

        // Mostrar datos (solo lectura)
        txtNombre.text = nombre
        txtEmail.text = email
        txtRol.text = rol

        // Spinner de estado
        val estados = arrayOf("ACTIVO", "INACTIVO", "BLOQUEADO")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, estados)
        spinnerEstado.adapter = adapter
        spinnerEstado.setSelection(estados.indexOf(estadoActual))

        // Botón guardar
        btnGuardar.setOnClickListener {
            actualizarEstadoUsuario()
        }
    }

    private fun actualizarEstadoUsuario() {
        val nuevoEstado = spinnerEstado.selectedItem.toString()

        val url = "http://54.89.22.17/actualizar_estado_usuario.php"

        val request = object : StringRequest(
            Method.POST, url,
            { response ->
                SweetAlertDialog(this, SweetAlertDialog.SUCCESS_TYPE)
                    .setTitleText("Estado Actualizado")
                    .setContentText("El estado del usuario se cambió correctamente.")
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
                    .setContentText("No se pudo actualizar el usuario: ${error.message}")
                    .show()
            }
        ) {
            override fun getParams(): MutableMap<String, String> {
                val params = HashMap<String, String>()
                params["id_usuario"] = idUsuario.toString()
                params["estado"] = nuevoEstado

                // Si el estado es INACTIVO o BLOQUEADO, enviar fecha_baja
                if (nuevoEstado == "INACTIVO" || nuevoEstado == "BLOQUEADO") {
                    val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                    params["fecha_baja"] = sdf.format(Date())
                } else {
                    params["fecha_baja"] = ""  // Limpiar fecha_baja si se reactiva
                }

                return params
            }
        }

        Volley.newRequestQueue(this).add(request)
    }
}