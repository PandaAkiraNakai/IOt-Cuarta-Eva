package com.example.proyectoindoor

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.camera2.CameraManager
import android.os.Build
import android.icu.text.SimpleDateFormat
import android.icu.util.Calendar
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.ImageView
import android.widget.TextView
import android.widget.Switch
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.VolleyError
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import cn.pedant.SweetAlert.SweetAlertDialog
import org.json.JSONException
import org.json.JSONObject

// =================== VARIABLES GLOBALES ===================
lateinit var datos: RequestQueue
val mHandler = Handler(Looper.getMainLooper())

lateinit var fecha: TextView
lateinit var temp: TextView
lateinit var hum: TextView
lateinit var imagenTemp: ImageView
lateinit var imagenBombilla: ImageView

private lateinit var flashSwitch: Switch
private lateinit var ampolletaSwitch: Switch
private lateinit var flashIcon: ImageView

private lateinit var cameraManager: CameraManager
private var cameraId: String? = null

class switches : AppCompatActivity() {

    private val PREFS_NAME = "preferenciasAmpolleta"
    private val KEY_AMPOLLETA = "estadoAmpolleta"

    // Refrescar cada 1 segundo
    private val refrescar = object : Runnable {
        override fun run() {
            obtenerDatos()
            mHandler.postDelayed(this, 1000)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_switches)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Referencias UI
        temp = findViewById(R.id.txt_temp)
        hum = findViewById(R.id.txt_humedad)
        imagenTemp = findViewById(R.id.imagen_temp)
        imagenBombilla = findViewById(R.id.bombilla)
        datos = Volley.newRequestQueue(this)

        flashSwitch = findViewById(R.id.switch_flash)
        flashIcon = findViewById(R.id.flashlight)
        ampolletaSwitch = findViewById(R.id.switch_ampolleta)

        // ========== FLASH ==============
        cameraManager = getSystemService(CAMERA_SERVICE) as CameraManager
        cameraId = cameraManager.cameraIdList[0]

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), 123)
        }

        flashSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                encenderFlash()
                SweetAlertDialog(this, SweetAlertDialog.SUCCESS_TYPE)
                    .setTitleText("Linterna encendida")
                    .setContentText("La linterna del dispositivo está activada.")
                    .show()
            } else {
                apagarFlash()
                SweetAlertDialog(this, SweetAlertDialog.WARNING_TYPE)
                    .setTitleText("Linterna apagada")
                    .setContentText("La linterna del dispositivo está desactivada.")
                    .show()
            }
        }

        // ========== AMPOLLETA PERSISTENTE ==============
        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val estadoGuardado = prefs.getBoolean(KEY_AMPOLLETA, false)
        ampolletaSwitch.isChecked = estadoGuardado
        cambiarImagenAmpolleta()

        ampolletaSwitch.setOnCheckedChangeListener { _, isChecked ->
            guardarEstadoAmpolleta(isChecked)
            cambiarImagenAmpolleta()
        }
    }

    private fun guardarEstadoAmpolleta(estado: Boolean) {
        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_AMPOLLETA, estado).apply()
    }

    private fun cambiarImagenAmpolleta() {
        if (ampolletaSwitch.isChecked) {
            imagenBombilla.setImageResource(R.drawable.bombilla_on)
        } else {
            imagenBombilla.setImageResource(R.drawable.bombilla_off)
        }
    }

    // ========== DATOS DEL SERVIDOR (TEMP/HUM) ==========
    private fun obtenerDatos() {
        val url = "https://www.pnk.cl/muestra_datos.php"
        val request = JsonObjectRequest(
            Request.Method.GET, url, null,
            { response: JSONObject ->
                try {
                    temp.text = "${response.getString("temperatura")} C"
                    hum.text = "${response.getString("humedad")} %"
                    cambiarImagen(response.getString("temperatura").toFloat())
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            },
            { error: VolleyError ->
                error.printStackTrace()
            }
        )
        datos.add(request)
    }

    private fun cambiarImagen(valor: Float) {
        if (valor >= 20) {
            imagenTemp.setImageResource(R.drawable.tempalta)
        } else {
            imagenTemp.setImageResource(R.drawable.tempbaja)
        }
    }

    override fun onResume() {
        super.onResume()
        mHandler.post(refrescar)
    }

    override fun onPause() {
        super.onPause()
        mHandler.removeCallbacks(refrescar)
        apagarFlash()
    }

    override fun onDestroy() {
        super.onDestroy()
        mHandler.removeCallbacks(refrescar)
        apagarFlash()
    }

    // ========== FLASH ==============
    private fun encenderFlash() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && cameraId != null) {
            cameraManager.setTorchMode(cameraId!!, true)
            flashIcon.setImageResource(R.drawable.flash_on)
        }
    }

    private fun apagarFlash() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && cameraId != null) {
            cameraManager.setTorchMode(cameraId!!, false)
            flashIcon.setImageResource(R.drawable.flash_off)
            flashSwitch.isChecked = false
        }
    }
}
