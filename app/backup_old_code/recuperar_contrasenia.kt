package com.example.proyectoindoor

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.text.Editable
import android.text.TextWatcher
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import cn.pedant.SweetAlert.SweetAlertDialog
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject

class recuperar_contrasenia : AppCompatActivity() {

    // Campos
    lateinit var correo: EditText
    lateinit var btnRecuperar: Button
    lateinit var c1: EditText
    lateinit var c2: EditText
    lateinit var c3: EditText
    lateinit var c4: EditText
    lateinit var c5: EditText
    lateinit var contador: TextView

    var codigoVigente = false   //  controla si el código aún es válido

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recuperar_contrasenia)

        // Vincular elementos
        correo = findViewById(R.id.correo)
        btnRecuperar = findViewById(R.id.recuperar)
        c1 = findViewById(R.id.codigo_1)
        c2 = findViewById(R.id.codigo_2)
        c3 = findViewById(R.id.codigo_3)
        c4 = findViewById(R.id.codigo_4)
        c5 = findViewById(R.id.codigo_5)
        contador = findViewById(R.id.contador)

        //  PRESIONAR BOTÓN RECUPERAR
        btnRecuperar.setOnClickListener {
            val email = correo.text.toString().trim()

            if (email.isEmpty()) {
                SweetAlertDialog(this, SweetAlertDialog.WARNING_TYPE)
                    .setTitleText("Campo vacío")
                    .setContentText("Ingresa un correo.")
                    .show()
            } else {
                enviarCodigoAlPHP(email)
            }
        }

        //  Mover cursor automáticamente entre campos
        configurarTextWatcher(c1, c2)
        configurarTextWatcher(c2, c3)
        configurarTextWatcher(c3, c4)
        configurarTextWatcher(c4, c5)

        //  Verificar cuando escribe el último dígito
        c5.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if (c1.text.length == 1 && c2.text.length == 1 &&
                    c3.text.length == 1 && c4.text.length == 1 &&
                    c5.text.length == 1) {

                    val email = correo.text.toString().trim()
                    val codigo = c1.text.toString() + c2.text.toString() +
                            c3.text.toString() + c4.text.toString() + c5.text.toString()

                    validarCodigoPHP(email, codigo)
                }
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    // Cambia el foco al siguiente campo cuando escribes 1 número
    private fun configurarTextWatcher(actual: EditText, siguiente: EditText) {
        actual.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if (actual.text.length == 1) {
                    siguiente.requestFocus()
                }
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    //  Enviar correo + iniciar contador
    private fun enviarCodigoAlPHP(email: String) {
        val url = "http://34.206.129.152/enviar_codigo.php"

        val request = object : StringRequest(Method.POST, url,
            { response ->
                val json = JSONObject(response)
                val estado = json.optString("estado", "")

                when (estado) {
                    "codigo_enviado" -> {
                        SweetAlertDialog(this, SweetAlertDialog.SUCCESS_TYPE)
                            .setTitleText("Código enviado")
                            .setContentText("Revisa tu correo. Válido por 60 segundos.")
                            .show()
                        iniciarContador()
                    }
                    "no_existe" -> {
                        SweetAlertDialog(this, SweetAlertDialog.ERROR_TYPE)
                            .setTitleText("Correo no registrado")
                            .setContentText("Ese correo no existe en la base de datos.")
                            .show()
                    }
                    else -> {
                        SweetAlertDialog(this, SweetAlertDialog.ERROR_TYPE)
                            .setTitleText("Error inesperado")
                            .setContentText("No se pudo enviar el código.")
                            .show()
                    }
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
                params["email"] = email
                return params
            }
        }

        Volley.newRequestQueue(this).add(request)
    }

    //  Validar código en PHP
    private fun validarCodigoPHP(email: String, codigo: String) {
        val url = "http://34.206.129.152/validar_codigo.php"

        val request = object : StringRequest(Method.POST, url,
            { response ->
                val json = JSONObject(response)
                val estado = json.optString("estado", "")

                when (estado) {
                    "valido" -> {
                        SweetAlertDialog(this, SweetAlertDialog.SUCCESS_TYPE)
                            .setTitleText("Código correcto")
                            .setContentText("Ahora crea tu nueva contraseña.")
                            .setConfirmClickListener {
                                it.dismissWithAnimation()
                                val intent = Intent(this, recuperar_contra::class.java)
                                intent.putExtra("email", email)
                                startActivity(intent)
                            }
                            .show()
                    }
                    "incorrecto" -> {
                        SweetAlertDialog(this, SweetAlertDialog.ERROR_TYPE)
                            .setTitleText("Código incorrecto")
                            .setContentText("El código no coincide.")
                            .show()
                    }
                    "expirado" -> {
                        SweetAlertDialog(this, SweetAlertDialog.WARNING_TYPE)
                            .setTitleText("Código expirado")
                            .setContentText("Han pasado más de 60 segundos, solicita uno nuevo.")
                            .show()
                    }
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
                params["codigo"] = codigo
                return params
            }
        }

        Volley.newRequestQueue(this).add(request)
    }

    //  Contador de 60s
    private fun iniciarContador() {
        codigoVigente = true

        object : CountDownTimer(60000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val seg = millisUntilFinished / 1000
                contador.setText("00:$seg")
            }

            override fun onFinish() {
                codigoVigente = false
                contador.setText("00:00")
            }
        }.start()
    }
}
