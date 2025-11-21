package com.example.fortiva

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth

class ConfiguracionActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_configuracion)

        auth = FirebaseAuth.getInstance()

        val etNombreUsuario = findViewById<EditText>(R.id.etNombreUsuario)
        val btnGuardarNombre = findViewById<Button>(R.id.btnGuardarNombre)
        val btnCerrarSesion = findViewById<Button>(R.id.btnCerrarSesion)
        val tvTitulo = findViewById<TextView>(R.id.tvTituloConfiguracion)

        val user = auth.currentUser
        etNombreUsuario.setText(user?.displayName ?: "")
        tvTitulo.text = "Configuración"

        // Guardar nuevo nombre (a futuro se puede vincular con Firebase)
        btnGuardarNombre.setOnClickListener {
            val nuevoNombre = etNombreUsuario.text.toString().trim()
            if (nuevoNombre.isNotEmpty()) {
                // Aquí podrías actualizar FirebaseAuth.displayName con userProfileChangeRequest
                etNombreUsuario.clearFocus()
            }
        }

        btnCerrarSesion.setOnClickListener {
            // Idealmente, deberías detectar el proveedor de autenticación guardado
            cerrarSesion("GOOGLE") // o null si no usas Google
        }

        // Configurar navegación inferior
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNavigation)
        bottomNav.selectedItemId = R.id.menu_perfil

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.menu_catalogo -> {
                    startActivity(Intent(this, CatalogoActivity::class.java))
                    true
                }
                R.id.menu_carrito -> {
                    startActivity(Intent(this, CarritoActivity::class.java))
                    true
                }
                R.id.menu_perfil -> true // ya estás en Configuración
                else -> false
            }
        }
    }

    // 🔹 Mueve esta función FUERA del onCreate()
    private fun cerrarSesion(provider: String?) {
        // Limpiar preferencias locales
        val prefsEdit = getSharedPreferences(getString(R.string.prefs_file), MODE_PRIVATE).edit()
        prefsEdit.clear()
        prefsEdit.apply()

        // Cerrar sesión de Firebase
        FirebaseAuth.getInstance().signOut()

        // Cerrar sesión de Google si aplica
        if (provider == "GOOGLE") {
            val googleConf = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build()
            val googleClient = GoogleSignIn.getClient(this, googleConf)
            googleClient.signOut()
        }

        // Redirigir al login
        val intent = Intent(this, AuthActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}
