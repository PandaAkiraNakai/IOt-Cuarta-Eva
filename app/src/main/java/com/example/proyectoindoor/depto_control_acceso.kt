package com.example.proyectoindoor

import android.graphics.Color
import android.os.Bundle
import android.os.CountDownTimer
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class depto_control_acceso : AppCompatActivity() {

    private lateinit var btnAbrir: Button
    private lateinit var btnCerrar: Button
    private lateinit var txtEstado: TextView
    
    private var idUsuario: Int = 0
    private var idDepartamento: Int = 0
    private var estadoActual: String = "Cerrado"
    
    // Timer para el cierre automático
    private var cierreAutomaticoTimer: CountDownTimer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_depto_control_acceso)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Obtener datos de la sesión
        val prefs = getSharedPreferences("session", MODE_PRIVATE)
        idUsuario = prefs.getInt("id_usuario", 0)
        idDepartamento = prefs.getInt("id_departamento", 0)

        // Referencias a vistas
        btnAbrir = findViewById(R.id.abrir_puerta_usu)
        btnCerrar = findViewById(R.id.cerrar_puerta_usu)
        txtEstado = findViewById(R.id.estado_usu_puerta)

        // Actualizar estado inicial
        actualizarEstadoUI()

        // Botón ABRIR
        btnAbrir.setOnClickListener {
            accionarServo("ABRIR")
        }

        // Botón CERRAR
        btnCerrar.setOnClickListener {
            accionarServo("CERRAR")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // Cancelar timer si existe
        cierreAutomaticoTimer?.cancel()
    }

    private fun actualizarEstadoUI() {
        txtEstado.text = estadoActual
        if (estadoActual == "Abierto") {
            txtEstado.setTextColor(Color.parseColor("#4CAF50")) // Verde
        } else {
            txtEstado.setTextColor(Color.parseColor("#F44336")) // Rojo
        }
    }

    private fun iniciarTimerCierreAutomatico() {
        // Cancelar timer anterior si existe
        cierreAutomaticoTimer?.cancel()
        
        // Crear nuevo timer de 10 segundos
        cierreAutomaticoTimer = object : CountDownTimer(10000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                // Actualizar texto con cuenta regresiva
                val segundosRestantes = millisUntilFinished / 1000
                txtEstado.text = "Abierto ($segundosRestantes s)"
            }

            override fun onFinish() {
                // Actualizar estado a cerrado
                estadoActual = "Cerrado"
                actualizarEstadoUI()
            }
        }.start()
    }

    private fun accionarServo(accion: String) {
        val url = "http://54.89.22.17/control_servo.php"

        // Mostrar loading
        val loadingDialog = SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE)
        loadingDialog.titleText = if (accion == "ABRIR") "Abriendo barrera..." else "Cerrando barrera..."
        loadingDialog.setCancelable(false)
        loadingDialog.show()

        val request = object : StringRequest(
            Request.Method.POST,
            url,
            { response ->
                loadingDialog.dismiss()
                
                if (accion == "ABRIR") {
                    estadoActual = "Abierto"
                    actualizarEstadoUI()
                    // Iniciar timer de cierre automático
                    iniciarTimerCierreAutomatico()
                } else {
                    // Cancelar timer si se cierra manualmente
                    cierreAutomaticoTimer?.cancel()
                    estadoActual = "Cerrado"
                    actualizarEstadoUI()
                }

                // Registrar evento
                val tipoEvento = if (accion == "ABRIR") "APERTURA_MANUAL" else "CIERRE_MANUAL"
                val resultado = "PERMITIDO"
                registrarEvento(tipoEvento, resultado)

                // Mostrar mensaje de éxito
                SweetAlertDialog(this, SweetAlertDialog.SUCCESS_TYPE)
                    .setTitleText(if (accion == "ABRIR") "¡Barrera Abierta!" else "¡Barrera Cerrada!")
                    .setContentText(if (accion == "ABRIR") "La barrera se cerrará automáticamente en 10 segundos" else "La barrera ha sido cerrada")
                    .show()
            },
            { error ->
                loadingDialog.dismiss()
                SweetAlertDialog(this, SweetAlertDialog.ERROR_TYPE)
                    .setTitleText("Error")
                    .setContentText("No se pudo conectar con el servidor")
                    .show()
            }
        ) {
            override fun getParams(): MutableMap<String, String> {
                val params = HashMap<String, String>()
                params["accion"] = accion
                return params
            }
        }

        Volley.newRequestQueue(this).add(request)
    }

    private fun registrarEvento(tipoEvento: String, resultado: String) {
        val url = "http://34.206.129.152/registrar_evento_manual.php"

        // Obtener fecha y hora del teléfono
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val fechaHora = sdf.format(Date())

        val request = object : StringRequest(
            Request.Method.POST,
            url,
            { /* Evento registrado exitosamente */ },
            { /* Error al registrar, pero no interrumpir al usuario */ }
        ) {
            override fun getParams(): MutableMap<String, String> {
                val params = HashMap<String, String>()
                params["id_usuario"] = idUsuario.toString()
                params["id_departamento"] = idDepartamento.toString()
                params["tipo_evento"] = tipoEvento
                params["resultado"] = resultado
                params["fecha_hora"] = fechaHora
                return params
            }
        }

        Volley.newRequestQueue(this).add(request)
    }
}