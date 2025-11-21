package com.example.fortiva

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView

open class BaseActivity(private val layoutResId: Int) : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(layoutResId)
        configurarBottomNavigation()
    }

    private fun configurarBottomNavigation() {
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNavigation)
        if (bottomNav == null) {
            Log.e("BaseActivity", "❌ BottomNavigationView no encontrado en el layout")
            return
        }

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.menu_catalogo -> {
                    if (this !is CatalogoActivity) {
                        startActivity(Intent(this, CatalogoActivity::class.java))
                        overridePendingTransition(0, 0)
                    }
                    true
                }
                R.id.menu_carrito -> {
                    if (this !is CarritoActivity) {
                        startActivity(Intent(this, CarritoActivity::class.java))
                        overridePendingTransition(0, 0)
                    }
                    true
                }
                R.id.menu_perfil -> {
                    if (this !is ConfiguracionActivity) {
                        startActivity(Intent(this, ConfiguracionActivity::class.java))
                        overridePendingTransition(0, 0)
                    }
                    true
                }
                else -> false
            }
        }

        // Marcar item seleccionado según Activity actual
        when (this) {
            is CatalogoActivity -> bottomNav.selectedItemId = R.id.menu_catalogo
            is CarritoActivity -> bottomNav.selectedItemId = R.id.menu_carrito
            is ConfiguracionActivity -> bottomNav.selectedItemId = R.id.menu_catalogo
        }
    }
}
