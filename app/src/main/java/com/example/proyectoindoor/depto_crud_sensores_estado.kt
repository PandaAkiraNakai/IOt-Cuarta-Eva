package com.example.proyectoindoor

import android.content.Intent
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

class depto_crud_sensores_estado : AppCompatActivity() {

    private lateinit var listView: ListView
    private val listaSensores = ArrayList<String>()
    private val listaIds = ArrayList<Int>()

    private val itemCompletoJson = ArrayList<JSONObject>()

    // ID del departamento del admin logueado
    private var idDeptoAdmin: Int = 0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_depto_crud_sensores_estado)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Obtener id_departamento de la sesión del admin
        val prefs = getSharedPreferences("session", MODE_PRIVATE)
        idDeptoAdmin = prefs.getInt("id_departamento", 0)

        // REFERENCIA CORRECTA AL NUEVO LISTVIEW
        listView = findViewById(R.id.listado_sensores1)

        cargarSensores()

        listView.setOnItemClickListener { _, _, position, _ ->

            val idSensor = listaIds[position]
            val data = itemCompletoJson[position]

            val intent = Intent(this, depto_crud_sensores_estado_modificar::class.java)

            intent.putExtra("id_sensor", idSensor)
            intent.putExtra("codigo_sensor", data.getString("codigo_sensor"))
            intent.putExtra("estado", data.getString("estado"))
            intent.putExtra("tipo", data.getString("tipo"))
            intent.putExtra("id_usuario", data.getInt("id_usuario"))
            intent.putExtra("id_departamento", data.getInt("id_departamento"))

            startActivityForResult(intent, 500)

        }

    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 500 && resultCode == RESULT_OK) {
            cargarSensores()  //
        }
    }

    private fun cargarSensores() {
        val url = "http://54.89.22.17/listar_sensores.php?id_departamento=$idDeptoAdmin"

        val request = StringRequest(
            Request.Method.GET,
            url,
            { response ->

                val jsonArray = JSONArray(response)

                listaSensores.clear()
                listaIds.clear()
                itemCompletoJson.clear()


                for (i in 0 until jsonArray.length()) {
                    val obj = jsonArray.getJSONObject(i)

                    val id = obj.getInt("id_sensor")
                    val uid = obj.getString("codigo_sensor")
                    val estado = obj.getString("estado")
                    val tipo = obj.getString("tipo")
                    val idUsuario = obj.optInt("id_usuario", 0)
                    val idDepto = obj.optInt("id_departamento", 0)

                    listaSensores.add("\nUID: $uid\nEstado: $estado\nTipo: $tipo \n ")
                    listaIds.add(id)

                    val sensorJson = JSONObject()
                    sensorJson.put("codigo_sensor", uid)
                    sensorJson.put("estado", estado)
                    sensorJson.put("tipo", tipo)
                    sensorJson.put("id_usuario", idUsuario)
                    sensorJson.put("id_departamento", idDepto)

                    itemCompletoJson.add(sensorJson)
                }

                if (listaSensores.isEmpty()) {
                    listaSensores.add("No hay sensores registrados en tu departamento")
                }

                val adapter = ArrayAdapter(
                    this,
                    android.R.layout.simple_list_item_1,
                    listaSensores
                )

                listView.adapter = adapter
            },
            {
                SweetAlertDialog(this, SweetAlertDialog.ERROR_TYPE)
                    .setTitleText("Error")
                    .setContentText("No se pudieron cargar los sensores.")
                    .show()
            }
        )

        Volley.newRequestQueue(this).add(request)
    }

}
