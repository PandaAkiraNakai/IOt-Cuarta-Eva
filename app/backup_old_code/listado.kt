package com.example.proyectoindoor

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import org.json.JSONArray

lateinit var list: ListView
lateinit var listados: ArrayList<String>
lateinit var dato: RequestQueue
lateinit var buscador: SearchView
lateinit var adapter: ArrayAdapter<String>

val listaJson = ArrayList<org.json.JSONObject>()

class listado : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_listado)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        list = findViewById(R.id.ListaUsuarios)
        buscador = findViewById(R.id.buscador)   // ← VINCULADO AL XML
        dato = Volley.newRequestQueue(this)

        cargaLista()


        buscador.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                adapter.filter.filter(newText)
                return true
            }
        })
    }

    override fun onResume() {
        super.onResume()
        cargaLista()
    }

    private fun cargaLista() {
        listaJson.clear()
        val listaUsu = ArrayList<String>()
        val url = "http://34.206.129.152/consulta.php"

        val request = StringRequest(
            Request.Method.GET, url,
            Response.Listener<String> { response ->
                try {
                    val json = JSONArray(response)
                    for (i in 0 until json.length()) {
                        val usuarios = json.getJSONObject(i)
                        listaJson.add(usuarios)
                        val linea = "${usuarios.getString("id")} | ${usuarios.getString("nombres")} | ${usuarios.getString("apellidos")}"
                        listaUsu.add(linea)
                    }

                    // ⚠️ IMPORTANTE: Adaptador global para poder filtrar
                    adapter = ArrayAdapter(
                        this,
                        android.R.layout.simple_list_item_1,
                        listaUsu
                    )
                    list.adapter = adapter

                    list.setOnItemClickListener { _, _, position, _ ->
                        val usuario = listaJson[position]
                        val intent = Intent(this, usuarios_CRUD::class.java)
                        intent.putExtra("id", usuario.getString("id"))
                        intent.putExtra("nombres", usuario.getString("nombres"))
                        intent.putExtra("apellidos", usuario.getString("apellidos"))
                        intent.putExtra("correo", usuario.getString("email"))
                        startActivity(intent)
                    }

                } catch (e: Exception) {
                    e.printStackTrace()
                }
            },
            Response.ErrorListener { error ->
                error.printStackTrace()
            })
        dato.add(request)
    }
}
