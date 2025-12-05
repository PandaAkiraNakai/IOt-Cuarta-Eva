package com.example.appdepartment

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ListView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import org.json.JSONArray

class depto_crud_usuarios : AppCompatActivity() {

    private lateinit var listView: ListView
    private val listaUsuariosIds = ArrayList<Int>()
    private val listaUsuariosNombres = ArrayList<String>()
    private val listaUsuariosCompletos = ArrayList<Map<String, Any>>()
    private var idDepartamento: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_depto_crud_usuarios)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Obtener ID del departamento del admin
        val prefs = getSharedPreferences("session", MODE_PRIVATE)
        idDepartamento = prefs.getInt("id_departamento", 0)

        // === REFERENCIA AL BOTÓN Y LISTVIEW ===
        val btnRegistrar = findViewById<Button>(R.id.btn_RegistrarUsuario)
        listView = findViewById(R.id.listview_usuarios)

        // === AL HACER CLICK, IR A CREAR USUARIO ===
        btnRegistrar.setOnClickListener {
            val intent = Intent(this, depto_crud_usuarios_crear::class.java)
            startActivity(intent)
        }

        // === CARGAR USUARIOS DEL DEPARTAMENTO ===
        cargarUsuarios()

        // === AL HACER CLICK EN UN USUARIO, IR A MODIFICAR ===
        listView.setOnItemClickListener { _, _, position, _ ->
            // Verificar que no sea el mensaje de "No hay usuarios"
            if (listaUsuariosCompletos.isEmpty()) {
                return@setOnItemClickListener
            }

            val usuario = listaUsuariosCompletos[position]

            val intent = Intent(this, depto_crud_usuarios_modificar::class.java)
            intent.putExtra("id_usuario", usuario["id_usuario"] as Int)
            intent.putExtra("nombre", usuario["nombre"] as String)
            intent.putExtra("email", usuario["email"] as String)
            intent.putExtra("rol", usuario["rol"] as String)
            intent.putExtra("estado", usuario["estado"] as String)
            intent.putExtra("rut", usuario["rut"] as String)
            intent.putExtra("telefono", usuario["telefono"] as String)
            intent.putExtra("id_departamento", idDepartamento)
            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        // Recargar lista al volver de modificar
        cargarUsuarios()
    }

    private fun cargarUsuarios() {
        // Agregar parámetro para solicitar TODOS los usuarios, sin filtrar por estado
        val url = "http://54.89.22.17/listar_usuarios_depto.php?id_departamento=$idDepartamento&todos=1"

        // Log para depuración
        println("📡 Cargando usuarios desde: $url")

        val request = StringRequest(
            Request.Method.GET,
            url,
            { response ->
                try {
                    println("📥 Respuesta del servidor: $response")

                    val jsonArray = JSONArray(response)

                    listaUsuariosIds.clear()
                    listaUsuariosNombres.clear()
                    listaUsuariosCompletos.clear()

                    for (i in 0 until jsonArray.length()) {
                        val obj = jsonArray.getJSONObject(i)
                        val idUsuario = obj.getInt("id_usuario")
                        val nombre = obj.getString("nombre")
                        val email = obj.getString("email")
                        val rol = obj.getString("rol")
                        val estado = obj.optString("estado", "ACTIVO")
                        val rut = obj.optString("rut", "")
                        val telefono = obj.optString("telefono", "")

                        listaUsuariosIds.add(idUsuario)

                        // Mostrar estado con color/emoji para identificar rápido
                        val estadoVisual = when(estado) {
                            "ACTIVO" -> "✓ $nombre ($rol)"
                            "INACTIVO" -> "⊘ $nombre ($rol) - INACTIVO"
                            "BLOQUEADO" -> "🔒 $nombre ($rol) - BLOQUEADO"
                            else -> "$nombre ($rol) - $estado"
                        }
                        listaUsuariosNombres.add(estadoVisual)

                        val usuarioMap = mapOf(
                            "id_usuario" to idUsuario,
                            "nombre" to nombre,
                            "email" to email,
                            "rol" to rol,
                            "estado" to estado,
                            "rut" to rut,
                            "telefono" to telefono
                        )
                        listaUsuariosCompletos.add(usuarioMap)
                    }

                    if (listaUsuariosNombres.isEmpty()) {
                        listaUsuariosNombres.add("No hay usuarios en este departamento")
                    }

                    println("✅ Usuarios cargados: ${listaUsuariosCompletos.size}")

                    val adapter = ArrayAdapter(
                        this,
                        android.R.layout.simple_list_item_1,
                        listaUsuariosNombres
                    )
                    listView.adapter = adapter

                } catch (e: Exception) {
                    e.printStackTrace()
                    println("❌ Error al procesar usuarios: ${e.message}")
                    Toast.makeText(this, "Error al cargar usuarios: ${e.message}", Toast.LENGTH_LONG).show()
                }
            },
            { error ->
                println("❌ Error de conexión: ${error.message}")
                Toast.makeText(this, "Error de conexión: ${error.message}", Toast.LENGTH_LONG).show()
            }
        )

        Volley.newRequestQueue(this).add(request)
    }
}
