package com.example.proyectoindoor

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import cn.pedant.SweetAlert.SweetAlertDialog
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import java.util.regex.Pattern

class usuarios_CRUD : AppCompatActivity() {

    private lateinit var txtNombre: EditText
    private lateinit var txtApellido: EditText
    private lateinit var txtEmail: EditText
    private lateinit var btnModificar: Button
    private lateinit var btnEliminar: Button

    private var userId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_usuarios_crud)

        txtNombre = findViewById(R.id.txtnombresmod)
        txtApellido = findViewById(R.id.txtapellidosmod)
        txtEmail = findViewById(R.id.txtcorreomod)
        btnModificar = findViewById(R.id.btn_modificar)
        btnEliminar = findViewById(R.id.btn_eliminar)

        // Rellenar datos enviados desde el intent
        userId = intent.getStringExtra("id")
        txtNombre.setText(intent.getStringExtra("nombres"))
        txtApellido.setText(intent.getStringExtra("apellidos"))
        txtEmail.setText(intent.getStringExtra("correo"))

        btnModificar.setOnClickListener {
            if (validarCampos()) {
                confirmarModificacion()
            }
        }

        btnEliminar.setOnClickListener {
            confirmarEliminacion()
        }
    }

    private fun validarCampos(): Boolean {
        val nombre = txtNombre.text.toString().trim()
        val apellido = txtApellido.text.toString().trim()
        val email = txtEmail.text.toString().trim()

        if (nombre.isEmpty()) {
            SweetAlertDialog(this, SweetAlertDialog.WARNING_TYPE)
                .setTitleText("Campo vacío")
                .setContentText("El campo Nombres no puede estar vacío.")
                .setConfirmClickListener {
                    it.dismissWithAnimation()
                    txtNombre.requestFocus()  // ✅ Foco aquí
                }
                .show()
            return false
        }

        if (apellido.isEmpty()) {
            SweetAlertDialog(this, SweetAlertDialog.WARNING_TYPE)
                .setTitleText("Campo vacío")
                .setContentText("El campo Apellidos no puede estar vacío.")
                .setConfirmClickListener {
                    it.dismissWithAnimation()
                    txtApellido.requestFocus()
                }
                .show()
            return false
        }

        if (email.isEmpty()) {
            SweetAlertDialog(this, SweetAlertDialog.WARNING_TYPE)
                .setTitleText("Campo vacío")
                .setContentText("El campo Correo no puede estar vacío.")
                .setConfirmClickListener {
                    it.dismissWithAnimation()
                    txtEmail.requestFocus()
                }
                .show()
            return false
        }

        if (!Pattern.matches("^[a-zA-ZáéíóúÁÉÍÓÚñÑ ]+$", nombre)) {
            SweetAlertDialog(this, SweetAlertDialog.ERROR_TYPE)
                .setTitleText("Nombre inválido")
                .setContentText("El campo Nombres solo puede contener letras y espacios.")
                .setConfirmClickListener {
                    it.dismissWithAnimation()
                    txtNombre.requestFocus()
                }
                .show()
            return false
        }

        if (!Pattern.matches("^[a-zA-ZáéíóúÁÉÍÓÚñÑ ]+$", apellido)) {
            SweetAlertDialog(this, SweetAlertDialog.ERROR_TYPE)
                .setTitleText("Apellido inválido")
                .setContentText("El campo Apellidos solo puede contener letras y espacios.")
                .setConfirmClickListener {
                    it.dismissWithAnimation()
                    txtApellido.requestFocus()
                }
                .show()
            return false
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            SweetAlertDialog(this, SweetAlertDialog.ERROR_TYPE)
                .setTitleText("Correo inválido")
                .setContentText("El formato del correo no es válido.")
                .setConfirmClickListener {
                    it.dismissWithAnimation()
                    txtEmail.requestFocus()
                }
                .show()
            return false
        }

        return true
    }

    private fun confirmarModificacion() {
        SweetAlertDialog(this, SweetAlertDialog.WARNING_TYPE)
            .setTitleText("¿Guardar cambios?")
            .setContentText("¿Estás seguro de Modificar este usuario?")
            .setConfirmText("Sí, Modificar")
            .setCancelText("Cancelar")
            .setConfirmClickListener {
                it.dismissWithAnimation()
                modificarUsuario()
            }
            .show()
    }

    private fun confirmarEliminacion() {
        SweetAlertDialog(this, SweetAlertDialog.WARNING_TYPE)
            .setTitleText("¿Eliminar usuario?")
            .setContentText("Esta acción no se puede deshacer.")
            .setConfirmText("Sí, eliminar")
            .setCancelText("Cancelar")
            .setConfirmClickListener { dialog ->
                dialog.dismissWithAnimation()
                eliminarUsuario()
            }
            .show()
    }

    private fun eliminarUsuario() {
        val url = "http://54.89.22.17/eliminar_usuario.php"
        val request = object : StringRequest(
            Request.Method.POST, url,
            { response ->
                if (response.contains("success")) {
                    SweetAlertDialog(this, SweetAlertDialog.SUCCESS_TYPE)
                        .setTitleText("Eliminado")
                        .setContentText("El usuario fue eliminado correctamente.")
                        .setConfirmText("OK")
                        .setConfirmClickListener { dialog ->
                            dialog.dismissWithAnimation()
                            finish()
                        }
                        .show()
                } else {
                    SweetAlertDialog(this, SweetAlertDialog.ERROR_TYPE)
                        .setTitleText("Error")
                        .setContentText("No se pudo eliminar el usuario.")
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
                params["id"] = userId ?: ""
                return params
            }
        }
        Volley.newRequestQueue(this).add(request)
    }

    private fun modificarUsuario() {
        val url = "http://54.89.22.17/modificar_usuario.php"

        val request = object : StringRequest(Request.Method.POST, url,
            { response ->
                when {
                    response.contains("success") -> {
                        SweetAlertDialog(this, SweetAlertDialog.SUCCESS_TYPE)
                            .setTitleText("Actualizado")
                            .setContentText("El usuario fue modificado correctamente.")
                            .setConfirmText("OK")
                            .setConfirmClickListener { dialog ->
                                dialog.dismissWithAnimation()
                                finish()
                            }
                            .show()
                    }
                    response.contains("email_exists") -> {
                        SweetAlertDialog(this, SweetAlertDialog.ERROR_TYPE)
                            .setTitleText("Correo duplicado")
                            .setContentText("Ya existe otro usuario con ese correo.")
                            .setConfirmClickListener {
                                it.dismissWithAnimation()
                                txtEmail.requestFocus()
                            }
                            .show()
                    }
                    else -> {
                        SweetAlertDialog(this, SweetAlertDialog.ERROR_TYPE)
                            .setTitleText("Error")
                            .setContentText("No se pudo actualizar el usuario.")
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
                params["id"] = userId ?: ""
                params["nombres"] = txtNombre.text.toString()
                params["apellidos"] = txtApellido.text.toString()
                params["email"] = txtEmail.text.toString()
                return params
            }
        }

        Volley.newRequestQueue(this).add(request)
    }
}

