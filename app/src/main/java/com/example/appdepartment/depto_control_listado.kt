package com.example.appdepartment

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.ListView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import cn.pedant.SweetAlert.SweetAlertDialog
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import org.json.JSONArray
import org.json.JSONObject

class depto_control_listado : AppCompatActivity() {

    private lateinit var listView: ListView
    private val listaEventos = ArrayList<String>()
    private val listaEventosData = ArrayList<JSONObject>()
    private var idDeptoAdmin: Int = 0

    // Receiver para recargar cuando se registre un evento en el departamento
    private val eventoReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            try {
                val idDepto = intent?.getIntExtra("id_departamento", 0) ?: 0
                println("🔔 Broadcast recibido en depto_control_listado: id_departamento=$idDepto")
                if (idDepto != 0 && idDepto == idDeptoAdmin) {
                    cargarHistorialEventos()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_depto_control_listado)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Obtener id_departamento y rol de la sesión
        val prefs = getSharedPreferences("session", MODE_PRIVATE)
        idDeptoAdmin = prefs.getInt("id_departamento", 0)
        val rol = prefs.getString("rol", "") ?: ""

        // Si el usuario NO es admin, redirigir al historial del usuario (solo su departamento)
        if (rol != "ADMIN") {
            // Evitar que un operador vea el listado admin de todos los departamentos
            val intentRedirect = Intent(this, depto_usuario_historial::class.java)
            startActivity(intentRedirect)
            finish()
            return
        }

        // Referencia al ListView
        listView = findViewById(R.id.lsitado_historial_accesos)

        // Cargar historial de eventos
        cargarHistorialEventos()

        // Click en un evento para ver detalles y controlar acceso
        listView.setOnItemClickListener { _, _, position, _ ->
            // Verificar que hay datos y no es el mensaje de "No hay eventos"
            if (listaEventosData.isNotEmpty() && position < listaEventosData.size) {
                val evento = listaEventosData[position]

                val intent = Intent(this, depto_control_listado_acceso::class.java)
                intent.putExtra("id_evento", evento.optInt("id_evento", 0))
                intent.putExtra("id_sensor", evento.optInt("id_sensor", 0))
                intent.putExtra("codigo_sensor", evento.optString("codigo_sensor", "N/A"))
                intent.putExtra("fecha_hora", evento.optString("fecha_hora", ""))
                intent.putExtra("resultado", evento.optString("resultado", ""))
                intent.putExtra("tipo_evento", evento.optString("tipo_evento", ""))
                intent.putExtra("nombre_usuario", evento.optString("nombre_usuario", "Desconocido"))
                intent.putExtra("id_usuario", evento.optInt("id_usuario", 0))

                startActivityForResult(intent, 600)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Registrar receiver para actualizaciones en tiempo real
        try {
            registerReceiver(eventoReceiver, IntentFilter("com.example.appdepartment.EVENTO_REGISTRADO"))
        } catch (e: Exception) { }
        // Recargar eventos cada vez que la pantalla vuelve a ser visible
        cargarHistorialEventos()
    }

    override fun onPause() {
        super.onPause()
        try {
            unregisterReceiver(eventoReceiver)
        } catch (e: Exception) { }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 600 && resultCode == RESULT_OK) {
            cargarHistorialEventos()
        }
    }

    private fun cargarHistorialEventos() {
        val url = "http://54.89.22.17/listar_eventos.php?id_departamento=$idDeptoAdmin"

        val request = StringRequest(
            Request.Method.GET,
            url,
            { response ->
                try {
                    val jsonArray = JSONArray(response)

                    listaEventos.clear()
                    listaEventosData.clear()

                    for (i in 0 until jsonArray.length()) {
                        val obj = jsonArray.getJSONObject(i)

                        val tipoEvento = obj.optString("tipo_evento", "N/A")
                        val fechaHora = obj.optString("fecha_hora", "N/A")
                        val resultado = obj.optString("resultado", "N/A")
                        val codigoSensor = if (obj.isNull("codigo_sensor")) "N/A" else obj.optString("codigo_sensor", "N/A")
                        val nombreUsuario = if (obj.isNull("nombre_usuario")) "Desconocido" else obj.optString("nombre_usuario", "Desconocido")

                        // Formatear el resultado with emoji
                        val resultadoIcon = if (resultado == "PERMITIDO") "✅" else "❌"

                        listaEventos.add(
                            "$resultadoIcon $tipoEvento\n" +
                            " $fechaHora\n" +
                            " Sensor: $codigoSensor\n" +
                            " Usuario: $nombreUsuario"
                        )

                        listaEventosData.add(obj)
                    }

                    if (listaEventos.isEmpty()) {
                        listaEventos.add("No hay eventos registrados en tu departamento")
                    }

                    val adapter = ArrayAdapter(
                        this,
                        android.R.layout.simple_list_item_1,
                        listaEventos
                    )

                    listView.adapter = adapter
                } catch (e: Exception) {
                    e.printStackTrace()
                    listaEventos.clear()
                    listaEventosData.clear()
                    listaEventos.add("Error al cargar eventos")
                    val adapter = ArrayAdapter(
                        this,
                        android.R.layout.simple_list_item_1,
                        listaEventos
                    )
                    listView.adapter = adapter
                }
            },
            { error ->
                error.printStackTrace()
                listaEventos.clear()
                listaEventosData.clear()
                listaEventos.add("Error de conexión con el servidor")
                val adapter = ArrayAdapter(
                    this,
                    android.R.layout.simple_list_item_1,
                    listaEventos
                )
                listView.adapter = adapter
            }
        )

        // Deshabilitar caché para asegurar datos frescos
        request.setShouldCache(false)

        Volley.newRequestQueue(this).add(request)
    }
}

