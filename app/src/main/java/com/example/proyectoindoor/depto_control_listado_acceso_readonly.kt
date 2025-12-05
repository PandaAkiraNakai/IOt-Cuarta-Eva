package com.example.proyectoindoor

import android.os.Bundle
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class depto_control_listado_acceso_readonly : AppCompatActivity() {

    private lateinit var txtEstado: TextView
    private lateinit var txtFecha: TextView
    private lateinit var txtUsuario: TextView
    private lateinit var txtTipoEvento: TextView
    private lateinit var txtSensor: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_depto_control_listado_acceso_readonly)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Referencias a los TextViews
        txtEstado = findViewById(R.id.estado_texto_readonly)
        txtFecha = findViewById(R.id.fecha_texto_readonly)
        txtUsuario = findViewById(R.id.usuario_texto_readonly)
        txtTipoEvento = findViewById(R.id.tipo_evento_texto_readonly)
        txtSensor = findViewById(R.id.sensor_texto_readonly)

        // Obtener datos del intent
        val fechaHora = intent.getStringExtra("fecha_hora") ?: ""
        val resultado = intent.getStringExtra("resultado") ?: ""
        val nombreUsuario = intent.getStringExtra("nombre_usuario") ?: "Desconocido"
        val tipoEvento = intent.getStringExtra("tipo_evento") ?: "N/A"
        val codigoSensor = intent.getStringExtra("codigo_sensor") ?: "N/A"

        // Mostrar datos del evento seleccionado
        txtEstado.text = "Estado: $resultado"
        txtFecha.text = "Fecha: $fechaHora"
        txtUsuario.text = "Usuario: $nombreUsuario"
        txtTipoEvento.text = "Tipo: $tipoEvento"
        txtSensor.text = "Sensor: $codigoSensor"

        // Cambiar color según resultado
        if (resultado == "PERMITIDO") {
            txtEstado.setTextColor(resources.getColor(android.R.color.holo_green_dark))
        } else {
            txtEstado.setTextColor(resources.getColor(android.R.color.holo_red_dark))
        }
    }
}

