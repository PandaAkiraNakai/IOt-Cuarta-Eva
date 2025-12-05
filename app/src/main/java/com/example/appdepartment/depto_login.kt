package com.example.appdepartment

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import cn.pedant.SweetAlert.SweetAlertDialog
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject
import java.util.HashMap

class depto_login : AppCompatActivity() {

    private lateinit var editCorreo: EditText
    private lateinit var editPassword: EditText
    private lateinit var btnLogin: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_depto_login)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        editCorreo = findViewById(R.id.edit_correo)
        editPassword = findViewById(R.id.edit_password)
        btnLogin = findViewById(R.id.btn_login)

        btnLogin.setOnClickListener {
            validarYLogin()
        }
    }

    private fun validarYLogin() {
        val correo = editCorreo.text.toString().trim()
        val pass = editPassword.text.toString().trim()

        if (correo.isEmpty() || pass.isEmpty()) {
            SweetAlertDialog(this, SweetAlertDialog.ERROR_TYPE)
                .setTitleText("Error")
                .setContentText("Ingrese correo y contraseña")
                .show()
            return
        }

        login(correo, pass)
    }

    private fun login(email: String, password: String) {

        val url = "http://54.89.22.17/login.php"

        val request = object : StringRequest(
            Method.POST, url,
            // 1. ETIQUETA listener@ PARA PODER HACER RETURN
            listener@ { response ->
                try {
                    val json = JSONObject(response)

                    // -------------------------------------------
                    // 2. VERIFICAR ERROR DEL SERVIDOR
                    // -------------------------------------------
                    if (json.has("error")) {
                        SweetAlertDialog(this, SweetAlertDialog.ERROR_TYPE)
                            .setTitleText("Error de acceso")
                            .setContentText(json.getString("error"))
                            .show()
                        return@listener
                    }

                    // -------------------------------------------
                    // 3. TOMAR DATOS SEGUROS (Evita el Crash por NULL)
                    // Usamos optInt y optString. Si viene null, usa el valor por defecto.
                    // -------------------------------------------
                    val rol = json.optString("rol", "OPERADOR")
                    val idUsuario = json.optInt("id_usuario", 0)
                    // Si id_departamento es null en BD, aquí valdrá 0
                    val idDepto = json.optInt("id_departamento", 0)
                    val nombre = json.optString("nombre", "Usuario")

                    // Validación extra por seguridad
                    if (idUsuario == 0) {
                        SweetAlertDialog(this, SweetAlertDialog.ERROR_TYPE)
                            .setTitleText("Error")
                            .setContentText("Error al leer el ID del usuario.")
                            .show()
                        return@listener
                    }

                    // -------------------------------------------
                    // 4. GUARDAR SESIÓN
                    // -------------------------------------------
                    guardarSesion(idUsuario, idDepto, rol, nombre)

                    // -------------------------------------------
                    // 5. REDIRIGIR SEGÚN ROL
                    // -------------------------------------------
                    if (rol == "ADMIN") {
                        val intent = Intent(this, depto_gestion_adm::class.java)
                        startActivity(intent)
                        finish()
                    } else {
                        // Usuario OPERADOR - redirigir a pantalla de bienvenida
                        val intent = Intent(this, depto_usuario_bienvenida::class.java)
                        startActivity(intent)
                        finish()
                    }

                } catch (e: Exception) {
                    e.printStackTrace()
                    SweetAlertDialog(this, SweetAlertDialog.ERROR_TYPE)
                        .setTitleText("Error de App")
                        .setContentText("Error procesando la respuesta: ${e.message}")
                        .show()
                }
            },
            { error ->
                SweetAlertDialog(this, SweetAlertDialog.ERROR_TYPE)
                    .setTitleText("Error de conexión")
                    .setContentText(error.message ?: "No se pudo contactar con el servidor.")
                    .show()
            }
        ) {
            override fun getParams(): MutableMap<String, String> {
                val params = HashMap<String, String>()
                params["email"] = email
                params["password"] = password
                return params
            }
        }

        Volley.newRequestQueue(this).add(request)
    }

    private fun guardarSesion(id: Int, idDepto: Int, rol: String, nombre: String) {
        val prefs = getSharedPreferences("session", MODE_PRIVATE)
        val editor = prefs.edit()

        editor.putInt("id_usuario", id)
        editor.putInt("id_departamento", idDepto)
        editor.putString("rol", rol)
        editor.putString("nombre", nombre)

        editor.apply()
    }
}

