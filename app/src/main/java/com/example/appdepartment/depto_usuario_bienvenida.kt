package com.example.appdepartment

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class depto_usuario_bienvenida : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_depto_usuario_bienvenida)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Botón Llavero Digital
        val btnLlavero = findViewById<Button>(R.id.btn_llavero_usu)
        btnLlavero.setOnClickListener {
            val intent = Intent(this, depto_control_acceso::class.java)
            startActivity(intent)
        }

        // Botón Historial (para implementar después si lo necesitas)
        val btnHistorial = findViewById<Button>(R.id.btn_historial_usu1)
        btnHistorial.setOnClickListener {
            val intent = Intent(this, depto_usuario_historial::class.java)
            startActivity(intent)
        }
    }
}