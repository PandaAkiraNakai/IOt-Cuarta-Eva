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
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import org.json.JSONArray
import org.json.JSONObject

class depto_usuario_historial : AppCompatActivity() {

    private lateinit var listView: ListView
    private val listaEventos = ArrayList<String>()
    private val listaEventosData = ArrayList<JSONObject>()
    private var idUsuario: Int = 0
    private var idDepartamento: Int = 0

    // Receiver para recargar cuando se registre un evento
    private val eventoReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            try {
                val idUsr = intent?.getIntExtra("id_usuario", 0) ?: 0
                println("🔔 Broadcast recibido en depto_usuario_historial: id_usuario=$idUsr (mi id=$idUsuario)")

                // Si el broadcast corresponde a este usuario, recargar
                // CAMBIO: También recargar si id_usuario del broadcast es 0 o null (eventos sin usuario específico)
                if (idUsr == idUsuario || idUsr == 0) {
                    println("✅ Recargando historial porque el evento me corresponde")
                    android.widget.Toast.makeText(context, "🔄 Nuevo evento detectado, recargando...", android.widget.Toast.LENGTH_SHORT).show()
                    cargarHistorialEventos()
                } else {
                    println("⏭️ Ignorando broadcast, no es para mi usuario (recibido=$idUsr, mio=$idUsuario)")
                }
            } catch (e: Exception) {
                e.printStackTrace()
                println("❌ Error en eventoReceiver: ${e.message}")
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_depto_usuario_historial)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Obtener id_usuario e id_departamento de la sesión
        val prefs = getSharedPreferences("session", MODE_PRIVATE)
        idUsuario = prefs.getInt("id_usuario", 0)
        idDepartamento = prefs.getInt("id_departamento", 0)

        // Referencia al ListView
        listView = findViewById(R.id.historial_accesos_usu)

        // Cargar historial de eventos del usuario
        cargarHistorialEventos()

        // Click en un evento para ver detalles (solo lectura)
        listView.setOnItemClickListener { _, _, position, _ ->
            // Verificar que hay datos y no es el mensaje de "No hay eventos"
            if (listaEventosData.isNotEmpty() && position < listaEventosData.size) {
                val evento = listaEventosData[position]

                val intent = Intent(this, depto_control_listado_acceso_readonly::class.java)
                intent.putExtra("id_evento", evento.optInt("id_evento", 0))
                intent.putExtra("id_sensor", evento.optInt("id_sensor", 0))
                intent.putExtra("codigo_sensor", evento.optString("codigo_sensor", "N/A"))
                intent.putExtra("fecha_hora", evento.optString("fecha_hora", ""))
                intent.putExtra("resultado", evento.optString("resultado", ""))
                intent.putExtra("tipo_evento", evento.optString("tipo_evento", ""))
                intent.putExtra("nombre_usuario", evento.optString("nombre_usuario", "Desconocido"))
                intent.putExtra("id_usuario", evento.optInt("id_usuario", 0))

                startActivity(intent)
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

    private fun cargarHistorialEventos() {
        // Validar que tengamos id de usuario en sesión
        if (idUsuario == 0) {
            listaEventos.clear()
            listaEventosData.clear()
            listaEventos.add("Error: No se pudo identificar tu usuario en la sesión")
            val adapter = ArrayAdapter(
                this,
                android.R.layout.simple_list_item_1,
                listaEventos
            )
            listView.adapter = adapter
            return
        }

        // Endpoint específico para eventos por usuario
        val url = "http://54.89.22.17/listar_eventos_usuario.php?id_usuario=$idUsuario"

        println("🔄 Cargando historial para id_usuario=$idUsuario")
        println("🌐 URL: $url")

        val request = StringRequest(
            Request.Method.GET,
            url,
            { response ->
                try {
                    println("📥 listar_eventos_usuario - respuesta RAW (primeros 500 chars): ${response.take(500)}")

                    val jsonArray = JSONArray(response)
                    val totalEventos = jsonArray.length()

                    println("📊 Total de eventos recibidos: $totalEventos")
                    android.widget.Toast.makeText(this, "Cargando $totalEventos evento(s)...", android.widget.Toast.LENGTH_SHORT).show()

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
                            "📅 $fechaHora\n" +
                            "🔑 Sensor: $codigoSensor\n" +
                            "👤 Usuario: $nombreUsuario"
                        )

                        listaEventosData.add(obj)
                    }

                    if (listaEventos.isEmpty()) {
                        listaEventos.add("No hay eventos registrados para tu usuario")
                    }

                    val adapter = ArrayAdapter(
                        this,
                        android.R.layout.simple_list_item_1,
                        listaEventos
                    )

                    listView.adapter = adapter
                } catch (e: Exception) {
                    e.printStackTrace()
                    println("❌ Error al parsear listar_eventos_usuario: ${e.message}")
                    listaEventos.clear()
                    listaEventosData.clear()
                    listaEventos.add("Error al procesar los datos")
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
                println("❌ Error de conexión listar_eventos_usuario: ${error.message}")
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