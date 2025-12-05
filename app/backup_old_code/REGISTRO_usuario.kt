package com.example.proyectoindoor

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import cn.pedant.SweetAlert.SweetAlertDialog
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley

class REGISTRO_usuario : AppCompatActivity() {

    private lateinit var nombres: EditText
    private lateinit var apellidos: EditText
    private lateinit var correo: EditText
    private lateinit var password: EditText
    private lateinit var repetirPassword: EditText
    private lateinit var btnRegistrar: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registro_usuario)

        // Referencias a los campos
        nombres = findViewById(R.id.txtnombresregistro)
        apellidos = findViewById(R.id.txtapellidosregistro)
        correo = findViewById(R.id.txtcorreoregistro)
        password = findViewById(R.id.txtcontraseñaregistro)
        repetirPassword = findViewById(R.id.txtrepitocontraseñaregistro)
        btnRegistrar = findViewById(R.id.btn_registrar)

        btnRegistrar.setOnClickListener {
            val nom = nombres.text.toString().trim()
            val ape = apellidos.text.toString().trim()
            val email = correo.text.toString().trim()
            val pass1 = password.text.toString().trim()
            val pass2 = repetirPassword.text.toString().trim()

            // Validaciones con SweetAlert + Focus
            when {
                nom.isEmpty() -> alertaCampo("Nombre vacío", "Debes ingresar tu nombre.", nombres)
                ape.isEmpty() -> alertaCampo("Apellido vacío", "Debes ingresar tu apellido.", apellidos)
                email.isEmpty() -> alertaCampo("Correo vacío", "Debes ingresar un correo.", correo)
                !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() ->
                    alertaCampo("Correo inválido", "El formato del correo no es correcto.", correo)
                pass1.isEmpty() -> alertaCampo("Contraseña vacía", "Debes ingresar una contraseña.", password)
                pass2.isEmpty() -> alertaCampo("Confirmación vacía", "Debes repetir la contraseña.", repetirPassword)
                pass1 != pass2 -> alertaCampo("Contraseñas no coinciden", "Ambas contraseñas deben ser iguales.", repetirPassword)
                !validarPasswordFuerte(pass1) ->
                    alertaCampo("Contraseña débil", "Debe tener mínimo 8 caracteres, 1 mayúscula, 1 minúscula, 1 número y 1 símbolo.", password)
                else -> registrarUsuario(nom, ape, email, pass1)
            }
        }
    }

    // SweetAlert con enfoque automático en el campo que falló
    private fun alertaCampo(titulo: String, mensaje: String, campo: EditText) {
        SweetAlertDialog(this, SweetAlertDialog.WARNING_TYPE)
            .setTitleText(titulo)
            .setContentText(mensaje)
            .setConfirmClickListener {
                it.dismissWithAnimation()
                campo.requestFocus()
            }
            .show()
    }

    // Validar contraseña fuerte
    private fun validarPasswordFuerte(pass: String): Boolean {
        val regex = Regex("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@\$!%*#?&]).{8,}\$")
        return regex.containsMatchIn(pass)
    }

    // Enviar datos al servidor
    private fun registrarUsuario(nombre: String, apellido: String, email: String, pass: String) {
        val url = "http://54.89.22.17/registrar_usuario.php"

        val request = object : StringRequest(
            Method.POST, url,
            { response ->
                when {
                    response.contains("registrado") -> {
                        SweetAlertDialog(this, SweetAlertDialog.SUCCESS_TYPE)
                            .setTitleText("Registro exitoso")
                            .setContentText("Tu cuenta fue creada.")
                            .setConfirmText("Ir al Login")
                            .setConfirmClickListener { dialog ->
                                dialog.dismissWithAnimation()
                                startActivity(Intent(this, MainActivity::class.java))
                                finish()
                            }
                            .show()
                    }
                    response.contains("existe") -> alertaCampo("Correo ya registrado", "Ese correo ya está en uso.", correo)
                    else -> SweetAlertDialog(this, SweetAlertDialog.ERROR_TYPE)
                        .setTitleText("Error")
                        .setContentText("No se pudo registrar. Intenta más tarde.")
                        .show()
                }
            },
            {
                SweetAlertDialog(this, SweetAlertDialog.ERROR_TYPE)
                    .setTitleText("Error de conexión")
                    .setContentText("No se pudo conectar al servidor.")
                    .show()
            }
        ) {
            override fun getParams(): MutableMap<String, String> {
                val params = HashMap<String, String>()
                params["nombre"] = nombre
                params["apellido"] = apellido
                params["email"] = email
                params["password"] = pass
                return params
            }
        }

        Volley.newRequestQueue(this).add(request)
    }
}
