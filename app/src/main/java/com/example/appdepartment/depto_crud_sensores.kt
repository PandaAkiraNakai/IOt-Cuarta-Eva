package com.example.appdepartment

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class depto_crud_sensores : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_depto_crud_sensores)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // === BOTÓN 1: REGISTRAR SENSOR ===
        val btnRegistrar = findViewById<Button>(R.id.btn_RegistrarRfid)
        btnRegistrar.setOnClickListener {
            val intent = Intent(this, depto_crud_sensores_registro::class.java)
            startActivity(intent)
        }

        // === BOTÓN 2: MODIFICAR SENSOR (AGREGADO) ===
        // Según tu XML el ID es "btn_cambiar_estados"
        val btnModificar = findViewById<Button>(R.id.btn_cambiar_estados)
        btnModificar.setOnClickListener {
            val intent = Intent(this, depto_crud_sensores_estado::class.java)
            startActivity(intent)
        }
    }
}