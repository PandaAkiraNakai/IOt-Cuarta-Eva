package com.example.proyectoindoor

import android.annotation.SuppressLint
import android.content.Intent
import android.icu.text.SimpleDateFormat
import android.icu.util.Calendar
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat


class Indoor_CRUDS : AppCompatActivity() {
    private lateinit var fecha: TextView
    private lateinit var btncrudusuario: Button
    private lateinit var btndatosensor: Button
    private lateinit var btndesarrollador: Button
    private val mHandler = Handler(Looper.getMainLooper())
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_indoor_cruds)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        btncrudusuario=findViewById(R.id.btn_Crudusuario)
        btncrudusuario.setOnClickListener {
            val intent = Intent(this, Botones_CRUD::class.java)
            startActivity(intent)
        }
        btndatosensor=findViewById(R.id.btn_Datossensor)
        btndatosensor.setOnClickListener {
            val intent = Intent(this, switches::class.java)
            startActivity(intent)
        }
        btndesarrollador=findViewById(R.id.btn_desarrollador)
        btndesarrollador.setOnClickListener {
            val intent = Intent(this, desarrolladores::class.java)
            startActivity(intent)
        }
        fecha=findViewById(R.id.txt_fecha)
        mHandler.post(refrescar)
    }
    fun fechahora(): String {
        val c: Calendar = Calendar.getInstance()
        val sdf: SimpleDateFormat = SimpleDateFormat("dd MMMM YYYY, hh:mm:ss a")
            val strDate: String = sdf.format(c.getTime())
        return strDate
    }
    private val refrescar = object : Runnable {
        override fun run() {
            fecha.text = fechahora() // ✅ (AGREGADO) Ahora sí se actualiza el TextView en pantalla
            mHandler.postDelayed(this, 1000) // ✅ Tu código estaba bien, se deja igual
            }
        }

    override fun onDestroy() {
        super.onDestroy()
        mHandler.removeCallbacks(refrescar)
    }

    override fun onPause() {
        super.onPause()
        mHandler.removeCallbacks(refrescar)
    }

    override fun onResume() {
        super.onResume()
        mHandler.post(refrescar)
    }
}
