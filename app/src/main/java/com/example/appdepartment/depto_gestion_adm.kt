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
        val btnAccesoManual = findViewById<Button>(R.id.btn_historial_accesos)
        val btnCerrarSesion = findViewById<Button>(R.id.btn_cerrar_sesion)


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

        // === EVENTO: CERRAR SESIÓN ===
        btnCerrarSesion.setOnClickListener {
            cerrarSesion()
        }
    }

    private fun cerrarSesion() {
        // Limpiar SharedPreferences
        val prefs = getSharedPreferences("session", MODE_PRIVATE)
        val editor = prefs.edit()
        editor.clear()
        editor.apply()

        // Redirigir al login
        val intent = Intent(this, depto_login::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}
