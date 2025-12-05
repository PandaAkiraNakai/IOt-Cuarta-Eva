package com.example.appdepartment

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import cn.pedant.SweetAlert.SweetAlertDialog
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

class depto_control_listado_acceso : AppCompatActivity() {

    private lateinit var txtEstado: TextView
    private lateinit var txtFecha: TextView
    private lateinit var txtUsuario: TextView
    private lateinit var btnPermitir: Button
    private lateinit var btnDenegar: Button

    private var idSensor: Int = 0
    private var idUsuario: Int = 0
    private var idDeptoAdmin: Int = 0
    private var codigoSensor: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_depto_control_listado_acceso)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Obtener id_departamento de la sesión del admin
        val prefs = getSharedPreferences("session", MODE_PRIVATE)
        idDeptoAdmin = prefs.getInt("id_departamento", 0)

        // Referencias a los TextViews
        txtEstado = findViewById(R.id.estado_texto)
        txtFecha = findViewById(R.id.fecha_texto)
        txtUsuario = findViewById(R.id.usuario_texto2)
        btnPermitir = findViewById(R.id.btn_permitir_acceso)
        btnDenegar = findViewById(R.id.btn_denegar_acceso)

        // Obtener datos del intent
        idSensor = intent.getIntExtra("id_sensor", 0)
        idUsuario = intent.getIntExtra("id_usuario", 0)
        codigoSensor = intent.getStringExtra("codigo_sensor") ?: "N/A"
        val fechaHora = intent.getStringExtra("fecha_hora") ?: ""
        val resultado = intent.getStringExtra("resultado") ?: ""
        val nombreUsuario = intent.getStringExtra("nombre_usuario") ?: "Desconocido"

        // Mostrar datos del evento seleccionado
        txtEstado.text = resultado
        txtFecha.text = fechaHora
        txtUsuario.text = nombreUsuario

        // Cambiar color según resultado
        if (resultado == "PERMITIDO") {
            txtEstado.setTextColor(resources.getColor(android.R.color.holo_green_dark))
        } else {
            txtEstado.setTextColor(resources.getColor(android.R.color.holo_red_dark))
        }

        // Botón Permitir Acceso
        btnPermitir.setOnClickListener {
            SweetAlertDialog(this, SweetAlertDialog.WARNING_TYPE)
                .setTitleText("¿Permitir Acceso?")
                .setContentText("Se abrirá la barrera para el sensor $codigoSensor")
                .setConfirmText("Sí, abrir")
                .setCancelText("Cancelar")
                .setConfirmClickListener { dialog ->
                    dialog.dismissWithAnimation()
                    permitirAcceso()
                }
                .setCancelClickListener { dialog ->
                    dialog.dismissWithAnimation()
                }
                .show()
        }

        // Botón Denegar Acceso
        btnDenegar.setOnClickListener {
            SweetAlertDialog(this, SweetAlertDialog.WARNING_TYPE)
                .setTitleText("¿Denegar Acceso?")
                .setContentText("Se registrará el acceso denegado para el sensor $codigoSensor")
                .setConfirmText("Sí, denegar")
                .setCancelText("Cancelar")
                .setConfirmClickListener { dialog ->
                    dialog.dismissWithAnimation()
                    denegarAcceso()
                }
                .setCancelClickListener { dialog ->
                    dialog.dismissWithAnimation()
                }
                .show()
        }
    }

    private fun permitirAcceso() {
        // 1. Registrar evento de apertura manual
        registrarEvento("APERTURA_MANUAL", "PERMITIDO") { success ->
            if (success) {
                // 2. Activar el servo
                activarServo()
            }
        }
    }

    private fun denegarAcceso() {
        registrarEvento("CIERRE_MANUAL", "DENEGADO") { success ->
            if (success) {
                SweetAlertDialog(this, SweetAlertDialog.SUCCESS_TYPE)
                    .setTitleText("Acceso Denegado")
                    .setContentText("Se ha registrado el acceso denegado")
                    .setConfirmClickListener { dialog ->
                        dialog.dismissWithAnimation()
                        setResult(RESULT_OK)
                        finish()
                    }
                    .show()
            }
        }
    }

    private fun registrarEvento(tipoEvento: String, resultado: String, callback: (Boolean) -> Unit) {
        val url = "http://54.89.22.17/registrar_evento_manual.php"

        val request = object : StringRequest(
            Method.POST, url,
            { response ->
                try {
                    val json = JSONObject(response)
                    if (json.has("success") && json.getBoolean("success")) {
                        callback(true)
                    } else {
                        SweetAlertDialog(this, SweetAlertDialog.ERROR_TYPE)
                            .setTitleText("Error")
                            .setContentText(json.optString("error", "Error al registrar evento"))
                            .show()
                        callback(false)
                    }
                } catch (e: Exception) {
                    SweetAlertDialog(this, SweetAlertDialog.ERROR_TYPE)
                        .setTitleText("Error")
                        .setContentText("Error procesando respuesta: ${e.message}")
                        .show()
                    callback(false)
                }
            },
            { error ->
                SweetAlertDialog(this, SweetAlertDialog.ERROR_TYPE)
                    .setTitleText("Error de conexión")
                    .setContentText("No se pudo conectar al servidor")
                    .show()
                callback(false)
            }
        ) {
            override fun getParams(): MutableMap<String, String> {
                val params = HashMap<String, String>()
                if (idSensor != 0) params["id_sensor"] = idSensor.toString()
                if (idUsuario != 0) params["id_usuario"] = idUsuario.toString()
                params["id_departamento"] = idDeptoAdmin.toString()
                params["tipo_evento"] = tipoEvento
                params["resultado"] = resultado
                
                // Enviar fecha/hora del celular
                val fechaActual = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
                params["fecha_hora"] = fechaActual
                
                return params
            }
        }

        Volley.newRequestQueue(this).add(request)
    }

    private fun activarServo() {
        val url = "http://54.89.22.17/control_servo.php"

        val request = object : StringRequest(
            Method.POST, url,
            { response ->
                try {
                    val json = JSONObject(response)
                    if (json.has("success") && json.getBoolean("success")) {
                        SweetAlertDialog(this, SweetAlertDialog.SUCCESS_TYPE)
                            .setTitleText("¡Acceso Permitido!")
                            .setContentText("La barrera se está abriendo...")
                            .setConfirmClickListener { dialog ->
                                dialog.dismissWithAnimation()
                                setResult(RESULT_OK)
                                finish()
                            }
                            .show()
                    } else {
                        SweetAlertDialog(this, SweetAlertDialog.ERROR_TYPE)
                            .setTitleText("Error")
                            .setContentText("No se pudo activar la barrera")
                            .show()
                    }
                } catch (e: Exception) {
                    SweetAlertDialog(this, SweetAlertDialog.ERROR_TYPE)
                        .setTitleText("Error")
                        .setContentText("Error: ${e.message}")
                        .show()
                }
            },
            { error ->
                SweetAlertDialog(this, SweetAlertDialog.ERROR_TYPE)
                    .setTitleText("Error de conexión")
                    .setContentText("No se pudo conectar al servidor")
                    .show()
            }
        ) {
            override fun getParams(): MutableMap<String, String> {
                val params = HashMap<String, String>()
                params["accion"] = "ABRIR"
                return params
            }
        }

        Volley.newRequestQueue(this).add(request)
    }
}