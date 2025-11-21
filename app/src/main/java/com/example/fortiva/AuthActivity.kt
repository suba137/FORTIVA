package com.example.fortiva

import android.content.Intent
import android.os.Bundle
import android.view.WindowManager
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import com.example.fortiva.databinding.ActivityAuthBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import java.util.concurrent.Executor
import androidx.appcompat.app.AppCompatDelegate

class AuthActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAuthBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var executor: Executor
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var biometricPrompt: BiometricPrompt
    private lateinit var promptInfo: BiometricPrompt.PromptInfo
    private var failedAttempts = 0

    companion object {
        private const val GOOGLE_SIGN_IN = 100
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 🔓 Permitir captura de pantalla, etc.
        try {
            window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
        } catch (_: Exception) {}

        // ✅ Forzar modo claro
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        binding = ActivityAuthBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        executor = ContextCompat.getMainExecutor(this)

        configurarBiometria()
        configurarGoogleSignIn()
        setupListeners()
        verificarSesionGuardada()
    }

    // -----------------------------
    // 🔹 LISTENERS
    // -----------------------------
    private fun setupListeners() {
        binding.btnContinuar.setOnClickListener {
            val email = binding.etCorreo.text.toString().trim()
            val password = binding.etContrasena.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                mostrarDialogo("Por favor completa todos los campos.")
                return@setOnClickListener
            }

            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        // ✅ Guardar credenciales y forzar autenticación biométrica
                        guardarSesion(email, "EMAIL")
                        mostrarBiometria()
                    } else {
                        mostrarDialogo("Error al iniciar sesión. Verifica tus credenciales.")
                    }
                }
        }

        binding.tvCrearCuenta.setOnClickListener {
            startActivity(Intent(this, RegistroActivity::class.java))
        }

        binding.tvOlvidarContrasena.setOnClickListener {
            startActivity(Intent(this, RecordarActivity::class.java))
        }

        binding.btnGoogle.setOnClickListener {
            iniciarSesionGoogle()
        }
    }

    // -----------------------------
    // 🔹 GOOGLE SIGN-IN
    // -----------------------------
    private fun configurarGoogleSignIn() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)
    }

    private fun iniciarSesionGoogle() {
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, GOOGLE_SIGN_IN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == GOOGLE_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)!!
                firebaseAuthConGoogle(account)
            } catch (e: ApiException) {
                mostrarDialogo("Error al iniciar sesión con Google.")
            }
        }
    }

    private fun firebaseAuthConGoogle(account: GoogleSignInAccount) {
        val credential = GoogleAuthProvider.getCredential(account.idToken, null)
        auth.signInWithCredential(credential).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                // ✅ Guardar sesión y pedir huella
                guardarSesion(account.email ?: "", "GOOGLE")
                mostrarBiometria()
            } else {
                mostrarDialogo("Error de autenticación con Google.")
            }
        }
    }

    // -----------------------------
    // 🔹 BIOMETRÍA (2FA)
    // -----------------------------
    private fun configurarBiometria() {
        biometricPrompt = BiometricPrompt(this, executor,
            object : BiometricPrompt.AuthenticationCallback() {

                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    try {
                        window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
                    } catch (_: Exception) {}

                    val prefs = getSharedPreferences(getString(R.string.prefs_file), MODE_PRIVATE)
                    val email = prefs.getString("email", null)
                    val provider = prefs.getString("provider", null)

                    if (email != null && provider != null) {
                        navegarHome(email, provider)
                    } else {
                        mostrarDialogo("Error: no se encontró la sesión guardada.")
                    }
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    failedAttempts++
                    if (failedAttempts >= 3) {
                        cerrarSesion()
                    } else {
                        mostrarDialogo("Huella incorrecta ($failedAttempts/3)")
                    }
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    mostrarDialogo("Error: $errString")
                }
            })

        promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Verificación biométrica requerida")
            .setSubtitle("Confirma tu identidad con tu huella digital para continuar")
            .setNegativeButtonText("Cancelar")
            .build()
    }

    private fun mostrarBiometria() {
        failedAttempts = 0
        biometricPrompt.authenticate(promptInfo)
    }

    private fun guardarSesion(email: String, provider: String) {
        val prefs = getSharedPreferences(getString(R.string.prefs_file), MODE_PRIVATE).edit()
        prefs.putString("email", email)
        prefs.putString("provider", provider)
        prefs.apply()
    }

    private fun verificarSesionGuardada() {
        val prefs = getSharedPreferences(getString(R.string.prefs_file), MODE_PRIVATE)
        val email = prefs.getString("email", null)
        val provider = prefs.getString("provider", null)

        // Si hay sesión guardada, pedir biometría antes de pasar a la app
        if (email != null && provider != null) {
            mostrarBiometria()
        }
    }

    private fun cerrarSesion() {
        auth.signOut()
        val prefs = getSharedPreferences(getString(R.string.prefs_file), MODE_PRIVATE).edit()
        prefs.clear().apply()
        mostrarDialogo("Demasiados intentos fallidos. Cerrando sesión...")
        startActivity(Intent(this, AuthActivity::class.java))
        finish()
    }

    // -----------------------------
    // 🔹 NAVEGACIÓN Y DIÁLOGOS
    // -----------------------------
    private fun navegarHome(email: String, provider: String) {
        val intent = Intent(this, CatalogoActivity::class.java).apply {
            putExtra("email", email)
            putExtra("provider", provider)
        }
        startActivity(intent)
        finish()
    }

    private fun mostrarDialogo(mensaje: String) {
        AlertDialog.Builder(this)
            .setMessage(mensaje)
            .setPositiveButton("OK", null)
            .show()
    }
}
