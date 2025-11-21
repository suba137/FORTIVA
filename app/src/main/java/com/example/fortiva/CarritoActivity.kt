package com.example.fortiva

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import com.google.android.material.bottomnavigation.BottomNavigationView
import java.text.NumberFormat
import java.util.Locale
import java.util.concurrent.Executor

class CarritoActivity : AppCompatActivity() {

    private lateinit var recycler: RecyclerView
    private lateinit var tvTotal: TextView
    private lateinit var btnFinalizar: Button
    private lateinit var adapter: CarritoAdapter
    private lateinit var executor: Executor
    private lateinit var bottomNav: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_carrito)

        // --- Inicialización de vistas ---
        recycler = findViewById(R.id.recyclerCarrito)
        tvTotal = findViewById(R.id.tvTotal)
        btnFinalizar = findViewById(R.id.btnFinalizar)
        bottomNav = findViewById(R.id.bottomNavigation)
        executor = ContextCompat.getMainExecutor(this)

        // --- Configuración del RecyclerView ---
        recycler.layoutManager = LinearLayoutManager(this)
        val viewModel = FortivaApp.carritoViewModel

        adapter = CarritoAdapter(viewModel.carrito.value ?: mutableListOf()) { compra ->
            viewModel.eliminarCompra(compra)
        }
        recycler.adapter = adapter

        // --- Observadores del ViewModel ---
        viewModel.carrito.observe(this) {
            adapter.notifyDataSetChanged()
        }

        viewModel.totalCompra.observe(this) { total ->
            val formato = NumberFormat.getCurrencyInstance(Locale("es", "CO"))
            tvTotal.text = "Total: ${formato.format(total)}"
        }

        // --- Botón de finalizar compra ---
        btnFinalizar.text = "Finalizar compra"
        btnFinalizar.setOnClickListener {
            val total = viewModel.totalCompra.value ?: 0.0
            if (total > 0) {
                mostrarConfirmacionCompra(total)
            } else {
                irAlCatalogo()
            }
        }

        // --- Configuración de la barra de navegación ---
        configurarBottomNavigation()
    }

    // --- Funciones auxiliares ---

    private fun configurarBottomNavigation() {
        bottomNav.selectedItemId = R.id.menu_carrito

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.menu_catalogo -> {
                    startActivity(Intent(this, CatalogoActivity::class.java))
                    overridePendingTransition(0, 0)
                    true
                }
                R.id.menu_carrito -> true // ya estamos aquí
                R.id.menu_perfil -> {
                    startActivity(Intent(this, ConfiguracionActivity::class.java))
                    overridePendingTransition(0, 0)
                    true
                }
                else -> false
            }
        }
    }

    private fun mostrarConfirmacionCompra(total: Double) {
        val formato = NumberFormat.getCurrencyInstance(Locale("es", "CO"))
        val mensaje = "¿Deseas confirmar tu compra por ${formato.format(total)}?"

        AlertDialog.Builder(this)
            .setTitle("Confirmar compra")
            .setMessage(mensaje)
            .setPositiveButton("Confirmar") { _, _ ->
                mostrarBiometria()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun mostrarBiometria() {
        val biometricPrompt = BiometricPrompt(
            this,
            executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    finalizarCompra()
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    mostrarDialogo("Autenticación cancelada o fallida.")
                }
            }
        )

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Confirmar compra")
            .setSubtitle("Usa tu huella o autenticación biométrica")
            .setNegativeButtonText("Cancelar")
            .build()

        biometricPrompt.authenticate(promptInfo)
    }

    private fun finalizarCompra() {
        FortivaApp.carritoViewModel.finalizarCompra()
        mostrarDialogo("Compra completada exitosamente.")
        irAlCatalogo()
    }

    private fun irAlCatalogo() {
        val intent = Intent(this, CatalogoActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
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
