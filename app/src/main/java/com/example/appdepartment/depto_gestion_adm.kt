package com.example.appdepartment

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class depto_gestion_adm : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_depto_gestion_adm)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // === REFERENCIAS A BOTONES ===
        val btnCrudSensores = findViewById<Button>(R.id.btn_Crudrfid)
        val btnCrudUsuarios = findViewById<Button>(R.id.btn_CrudUsuariosiot)
        val btnAccesoManual = findViewById<Button>(R.id.btn_historial_accesos) // Agregué la referencia al 3er botón por si lo usas


        btnCrudSensores.setOnClickListener {

            val intent = Intent(this, depto_crud_sensores::class.java)
            startActivity(intent)
        }

        // === EVENTO: CRUD USUARIOS ===
        btnCrudUsuarios.setOnClickListener {
            val intent = Intent(this, depto_crud_usuarios::class.java)
            startActivity(intent)
        }

        // === EVENTO: ACCESO MANUAL (Opcional, agrégalo cuando tengas la activity creada) ===
        btnAccesoManual.setOnClickListener {
            val intent = Intent(this, depto_control_listado::class.java)
            startActivity(intent)
        }
    }
}
