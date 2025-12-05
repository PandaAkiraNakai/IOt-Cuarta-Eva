package com.example.proyectoindoor

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
private lateinit var btnregistrarusuario: Button
private lateinit var btnlistarusuario: Button
class Botones_CRUD : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_botones_crud)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        btnregistrarusuario=findViewById(R.id.btn_Registrarusuario)
        btnregistrarusuario.setOnClickListener {
            val intent = Intent(this, REGISTRO_usuario::class.java)
            startActivity(intent)
        }
        btnlistarusuario=findViewById(R.id.btn_Listarusuario)
        btnlistarusuario.setOnClickListener {
            val intent = Intent(this, listado::class.java)
            startActivity(intent)
        }
    }
}