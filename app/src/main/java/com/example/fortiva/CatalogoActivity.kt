package com.example.fortiva

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class CatalogoActivity : BaseActivity(R.layout.activity_catalogo) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerViewCatalogo)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = InmuebleAdapter(FortivaApp.listaInmuebles) { inmueble ->
            if (inmueble.id != null) {
                val intent = Intent(this, CompraParcialActivity::class.java)
                intent.putExtra("idInmueble", inmueble.id)
                startActivity(intent)
            } else {
                Log.e("CatalogoActivity", "❌ Error: inmueble.id es nulo")
            }
        }
    }

    override fun onResume() {
        super.onResume()
        findViewById<RecyclerView>(R.id.recyclerViewCatalogo).adapter?.notifyDataSetChanged()
    }
}
