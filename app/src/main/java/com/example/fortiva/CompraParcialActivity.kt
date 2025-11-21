package com.example.fortiva

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView

class CompraParcialActivity : AppCompatActivity() {

    private lateinit var progresoDisponibilidad: ProgressBar
    private lateinit var tvDisponibilidad: TextView
    private lateinit var etPorcentaje: EditText
    private lateinit var btnComprar: Button
    private lateinit var bottomNav: BottomNavigationView

    private var inmueble: Inmueble? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_compra_parcial)

        // Referencias UI
        progresoDisponibilidad = findViewById(R.id.progresoDisponibilidad)
        tvDisponibilidad = findViewById(R.id.tvDisponibilidad)
        etPorcentaje = findViewById(R.id.etPorcentaje)
        btnComprar = findViewById(R.id.btnComprar)

        // 🔹 Obtener el ID del inmueble
        val idInmueble = intent.getIntExtra("idInmueble", -1)
        Log.d("CompraParcial", "ID recibido: $idInmueble")

        inmueble = FortivaApp.listaInmuebles.find { it.id == idInmueble }

        if (inmueble == null) {
            Toast.makeText(this, "Error: inmueble no encontrado", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        // Mostrar disponibilidad inicial
        inmueble?.let {
            tvDisponibilidad.text = "Disponible: ${it.disponible.toInt()}%"
            progresoDisponibilidad.progress = it.disponible.toInt()
        }

        // 🔹 Actualizar vista previa al cambiar el porcentaje
        etPorcentaje.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) = actualizarVistaPrevia()
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        // 🔹 Acción de compra
        btnComprar.setOnClickListener {
            val valorTexto = etPorcentaje.text.toString()
            if (valorTexto.isEmpty()) {
                Toast.makeText(this, "Ingresa un porcentaje válido", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val porcentaje = valorTexto.toDoubleOrNull() ?: 0.0
            if (porcentaje <= 0) {
                Toast.makeText(this, "El porcentaje debe ser mayor que 0", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            realizarCompraRelativo(porcentaje)
        }
    }

    private fun actualizarVistaPrevia() {
        inmueble?.let {
            val porcentaje = etPorcentaje.text.toString().toDoubleOrNull() ?: 0.0
            val restante = it.disponible - porcentaje
            val nuevoPorcentaje = if (restante < 0) 0.0 else restante

            progresoDisponibilidad.progress = nuevoPorcentaje.toInt()
            tvDisponibilidad.text = "Disponible tras compra: ${nuevoPorcentaje.toInt()}%"
        }
    }

    private fun realizarCompraRelativo(porcentajeRel: Double) {
        inmueble?.let { inmuebleSeleccionado ->
            if (porcentajeRel > inmuebleSeleccionado.disponible) {
                Toast.makeText(this, "No hay tanta disponibilidad", Toast.LENGTH_SHORT).show()
                return
            }

            val agregado = FortivaApp.carritoViewModel.agregarCompraRelativo(
                inmuebleSeleccionado,
                porcentajeRel
            )

            if (agregado) {
                Toast.makeText(this, "Compra añadida al carrito", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, CarritoActivity::class.java))
                finish()
            } else {
                Toast.makeText(this, "No se pudo agregar la compra", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
