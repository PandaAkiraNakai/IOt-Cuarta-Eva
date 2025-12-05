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
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley

class recuperar_contra : AppCompatActivity() {

    private lateinit var nuevaContra: EditText
    private lateinit var repetirContra: EditText
    private lateinit var btnGuardar: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_recuperar_contra)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        nuevaContra = findViewById(R.id.nueva_contra)
        repetirContra = findViewById(R.id.repetir_nueva_contra)
        btnGuardar = findViewById(R.id.btn_guardar_new_contra)

        btnGuardar.setOnClickListener {
            val pass1 = nuevaContra.text.toString().trim()
            val pass2 = repetirContra.text.toString().trim()

            when {
                pass1.isEmpty() -> alertaConFoco("Contraseña vacía", "Debes ingresar una nueva contraseña.", nuevaContra)
                pass2.isEmpty() -> alertaConFoco("Confirmación vacía", "Debes repetir la contraseña.", repetirContra)
                pass1 != pass2 -> alertaConFoco("No coinciden", "Ambas contraseñas deben ser iguales.", repetirContra)
                !validarPasswordFuerte(pass1) ->
                    alertaConFoco("Contraseña débil", "Debe tener mínimo 8 caracteres, 1 mayúscula, 1 minúscula, 1 número y 1 símbolo.", nuevaContra)
                else -> {
                    cambiarPassword(pass1)
                }
            }
        }
    }

    // ✅ Alerta con foco automático en el campo ingresado
    private fun alertaConFoco(titulo: String, mensaje: String, campo: EditText) {
        SweetAlertDialog(this, SweetAlertDialog.WARNING_TYPE)
            .setTitleText(titulo)
            .setContentText(mensaje)
            .setConfirmClickListener { dialog ->
                dialog.dismissWithAnimation()
                campo.requestFocus()
            }
            .show()
    }

    private fun validarPasswordFuerte(pass: String): Boolean {
        val regex = Regex("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@\$!%*#?&]).{8,}\$")
        return regex.containsMatchIn(pass)
    }

    private fun cambiarPassword(nuevaPassword: String) {
        val email = intent.getStringExtra("email") ?: ""
        val url = "http://54.89.22.17/cambiar_contra.php"

        val request = object : StringRequest(
            Request.Method.POST, url,
            { response ->
                if (response.contains("actualizada")) {
                    SweetAlertDialog(this, SweetAlertDialog.SUCCESS_TYPE)
                        .setTitleText("¡Contraseña actualizada!")
                        .setContentText("Ahora puedes iniciar sesión.")
                        .setConfirmText("Ir al Login")
                        .setConfirmClickListener { dialog ->
                            dialog.dismissWithAnimation()
                            startActivity(Intent(this, MainActivity::class.java))
                            finish()
                        }
                        .show()
                } else {
                    SweetAlertDialog(this, SweetAlertDialog.ERROR_TYPE)
                        .setTitleText("Error")
                        .setContentText("No se pudo cambiar la contraseña.")
                        .show()
                }
            },
            {
                SweetAlertDialog(this, SweetAlertDialog.ERROR_TYPE)
                    .setTitleText("Error de conexión")
                    .setContentText("No se pudo conectar con el servidor.")
                    .show()
            }
        ) {
            override fun getParams(): MutableMap<String, String> {
                val params = HashMap<String, String>()
                params["email"] = email
                params["nueva_contra"] = nuevaPassword
                return params
            }
        }
        Volley.newRequestQueue(this).add(request)
    }
}

