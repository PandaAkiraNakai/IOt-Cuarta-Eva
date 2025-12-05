package com.example.appdepartment

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
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
        val rol = prefs.getString("rol", "") ?: ""

        // Referencias a los TextViews
        txtEstado = findViewById(R.id.estado_texto)
        txtFecha = findViewById(R.id.fecha_texto)
        txtUsuario = findViewById(R.id.usuario_texto2)
        btnPermitir = findViewById(R.id.btn_permitir_acceso)
        btnDenegar = findViewById(R.id.btn_denegar_acceso)

        // Si el rol no es ADMIN, ocultar botones de acción (solo lectura)
        if (rol != "ADMIN") {
            btnPermitir.visibility = android.view.View.GONE
            btnDenegar.visibility = android.view.View.GONE
        }

        // Obtener datos del intent
        idSensor = intent.getIntExtra("id_sensor", 0)
        idUsuario = intent.getIntExtra("id_usuario", 0)
        codigoSensor = intent.getStringExtra("codigo_sensor") ?: "N/A"
        val fechaHora = intent.getStringExtra("fecha_hora") ?: ""
        val resultado = intent.getStringExtra("resultado") ?: ""
        val nombreUsuario = intent.getStringExtra("nombre_usuario") ?: "Desconocido"

        // Logs de depuración
        println("🔍 onCreate - idSensor: $idSensor")
        println("🔍 onCreate - idUsuario: $idUsuario")
        println("🔍 onCreate - idDeptoAdmin: $idDeptoAdmin")
        println("🔍 onCreate - codigoSensor: $codigoSensor")
        println("🔍 onCreate - rol: $rol")

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
        val prefs = getSharedPreferences("session", MODE_PRIVATE)
        // Fallback: si idUsuario no está definido aquí, tomar de la sesión
        val usuarioSesion = prefs.getInt("id_usuario", 0)
        val deptoSesion = prefs.getInt("id_departamento", 0)

        val url = "http://54.89.22.17/registrar_evento_manual.php"

        val request = object : StringRequest(
            Method.POST, url,
            { response ->
                try {
                    // Mostrar respuesta cruda para depuración
                    println("📥 Respuesta registrarEvento: $response")

                    val json = JSONObject(response)
                    if (json.has("success") && json.getBoolean("success")) {
                        // Mostrar mensaje opcional del servidor (acepta "message" o "mensaje")
                        val msg = json.optString("message", json.optString("mensaje", "Evento registrado"))
                        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()

                        // Construir los valores que se enviarán en el broadcast
                        // Primero intentar obtenerlos de la respuesta JSON, si no usar los calculados
                        val idUsuarioEnvio = json.optInt("id_usuario", if (idUsuario != 0) idUsuario else usuarioSesion)
                        val idDeptoBroadcast = json.optInt("id_departamento", if (idDeptoAdmin != 0) idDeptoAdmin else deptoSesion)

                        println("✅ Evento registrado - id_evento: ${json.optInt("id_evento", 0)}")
                        println("✅ Broadcasting con id_usuario=$idUsuarioEnvio, id_departamento=$idDeptoBroadcast")

                        // Enviar broadcast para notificar que se registró un evento
                        val b = Intent("com.example.appdepartment.EVENTO_REGISTRADO")
                        b.putExtra("id_usuario", idUsuarioEnvio)
                        b.putExtra("id_departamento", idDeptoBroadcast)
                        sendBroadcast(b)

                        callback(true)
                    } else {
                        val err = json.optString("error", "Error al registrar evento")
                        SweetAlertDialog(this, SweetAlertDialog.ERROR_TYPE)
                            .setTitleText("Error")
                            .setContentText(err)
                            .show()
                        callback(false)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    SweetAlertDialog(this, SweetAlertDialog.ERROR_TYPE)
                        .setTitleText("Error")
                        .setContentText("Error procesando respuesta: ${e.message}")
                        .show()
                    callback(false)
                }
            },
            { error ->
                error.printStackTrace()
                println("❌ Error de Volley: ${error.javaClass.simpleName}")
                println("❌ Mensaje: ${error.message}")

                val mensajeDetallado = when {
                    error.networkResponse != null -> {
                        val statusCode = error.networkResponse.statusCode
                        val data = try {
                            String(error.networkResponse.data, Charsets.UTF_8)
                        } catch (e: Exception) {
                            "No se pudo leer el contenido"
                        }
                        println("❌ Status Code: $statusCode")
                        println("❌ Respuesta del servidor: $data")
                        "Error $statusCode del servidor:\n$data"
                    }
                    else -> "Error de red: ${error.message ?: "Sin conexión"}"
                }

                Toast.makeText(this, mensajeDetallado, Toast.LENGTH_LONG).show()

                SweetAlertDialog(this, SweetAlertDialog.ERROR_TYPE)
                    .setTitleText("Error de conexión")
                    .setContentText(mensajeDetallado)
                    .show()
                callback(false)
            }
        ) {
            override fun getParams(): MutableMap<String, String> {
                val params = HashMap<String, String>()
                // id_sensor: si es 0 o no existe, NO enviarlo (el servidor lo tratará como NULL)
                if (idSensor != 0) {
                    params["id_sensor"] = idSensor.toString()
                    println("📌 Enviando id_sensor: $idSensor")
                } else {
                    println("⚠️ registrarEvento: idSensor = 0, NO se enviará (será NULL en servidor)")
                }

                // id_usuario: preferir variable local (viene por intent), si es 0 tomar de sesión
                val idUsuarioEnvio = if (idUsuario != 0) idUsuario else usuarioSesion
                if (idUsuarioEnvio != 0) {
                    params["id_usuario"] = idUsuarioEnvio.toString()
                    println("📌 Enviando id_usuario: $idUsuarioEnvio")
                } else {
                    println("⚠️ registrarEvento: idUsuario no definido ni en intent ni en session, NO se enviará")
                }

                // id_departamento: preferir idDeptoAdmin (variable local) si es 0 usar sesión
                val idDeptoEnvio = if (idDeptoAdmin != 0) idDeptoAdmin else deptoSesion
                if (idDeptoEnvio != 0) {
                    params["id_departamento"] = idDeptoEnvio.toString()
                } else {
                    println("❌ ERROR CRÍTICO: idDepartamento no definido - el registro fallará")
                }

                params["tipo_evento"] = tipoEvento
                params["resultado"] = resultado

                // Enviar fecha/hora del celular
                val fechaActual = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
                params["fecha_hora"] = fechaActual

                println("📤 registrarEvento - Params finales: $params")
                return params
            }

            // Agregar cabeceras para no-cache
            override fun getHeaders(): MutableMap<String, String> {
                val headers = HashMap<String, String>()
                headers["Cache-Control"] = "no-cache"
                headers["Pragma"] = "no-cache"
                return headers
            }
        }

        // Configurar política de reintentos
        request.retryPolicy = com.android.volley.DefaultRetryPolicy(
            5000, // Tiempo de espera entre reintentos (5 segundos)
            2, // Número de reintentos
            com.android.volley.DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        )

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