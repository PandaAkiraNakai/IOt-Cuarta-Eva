package com.example.proyectoindoor

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import cn.pedant.SweetAlert.SweetAlertDialog
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import org.json.JSONException

class MainActivity : AppCompatActivity() {

    private lateinit var usu: EditText
    private lateinit var clave: EditText
    private lateinit var btn: Button
    private lateinit var btnregistrarme: Button
    private lateinit var btnolvidocontra: Button
    private lateinit var datos: RequestQueue

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Referencias UI
        usu = findViewById(R.id.usu)
        clave = findViewById(R.id.pass)
        btn = findViewById(R.id.btn_ingresar)
        btnregistrarme = findViewById(R.id.btn_registrarme)
        btnolvidocontra = findViewById(R.id.btn_olvidocontra)
        datos = Volley.newRequestQueue(this)

        // Botón ingresar
        btn.setOnClickListener {
            val usuario = usu.text.toString().trim()
            val password = clave.text.toString().trim()

            when {
                usuario.isEmpty() -> alertaConFoco("Correo vacío", "Debes ingresar tu correo.", usu)
                password.isEmpty() -> alertaConFoco("Contraseña vacía", "Debes ingresar tu contraseña.", clave)
                else -> consultarDatos(usuario, password)
            }
        }

        // Botón Registrarse
        btnregistrarme.setOnClickListener {
            startActivity(Intent(this, REGISTRO_usuario::class.java))
        }

        // Botón Olvidó su contraseña
        btnolvidocontra.setOnClickListener {
            startActivity(Intent(this, recuperar_contrasenia::class.java))
        }
    }


    private fun alertaConFoco(titulo: String, mensaje: String, campo: EditText) {
        SweetAlertDialog(this, SweetAlertDialog.WARNING_TYPE)
            .setTitleText(titulo)
            .setContentText(mensaje)
            .setConfirmText("Entendido")
            .setConfirmClickListener { dialog ->
                dialog.dismissWithAnimation()
                campo.requestFocus()
            }
            .show()
    }


    fun consultarDatos(usu: String, pass: String) {
        val url = "http://54.89.22.17/apiconsultausu.php?usu=$usu&pass=$pass"

        val request = JsonObjectRequest(
            Request.Method.GET, url, null,
            { response ->
                try {
                    when (response.getString("estado")) {
                        "0" -> alertaConFoco("Usuario no encontrado", "Ese correo no existe.", this.usu)
                        "2" -> alertaConFoco("Contraseña incorrecta", "Verifica tu contraseña.", this.clave)
                        "3" -> SweetAlertDialog(this, SweetAlertDialog.ERROR_TYPE)
                            .setTitleText("Usuario bloqueado")
                            .setContentText("Tu cuenta ha sido desactivada. Contacta al administrador.")
                            .show()
                        "1" -> alertaExito("¡Bienvenido!", "Inicio de sesión exitoso.")
                        else -> alertaError("Error", "Respuesta desconocida del servidor.")
                    }
                } catch (e: JSONException) {
                    alertaError("Error interno", "Falló el procesamiento de la respuesta.")
                }
            },
            {
                alertaError("Error de conexión", "No se pudo conectar con el servidor.")
            }
        )
        datos.add(request)
    }


    fun alertaExito(titulo: String, mensaje: String) {
        SweetAlertDialog(this, SweetAlertDialog.SUCCESS_TYPE)
            .setTitleText(titulo)
            .setContentText(mensaje)
            .setConfirmText("Continuar")
            .setConfirmClickListener {
                it.dismissWithAnimation()
                startActivity(Intent(this@MainActivity, Indoor_CRUDS::class.java))
            }
            .show()
    }


    fun alertaError(titulo: String, mensaje: String) {
        SweetAlertDialog(this, SweetAlertDialog.ERROR_TYPE)
            .setTitleText(titulo)
            .setContentText(mensaje)
            .setConfirmText("Entendido")
            .show()
    }
}
