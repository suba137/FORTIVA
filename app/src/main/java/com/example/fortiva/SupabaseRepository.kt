package com.example.fortiva

import com.google.firebase.auth.FirebaseAuth
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object SupabaseRepository {

    private val client = Supabase.client // ✅ corregido
    private val auth = FirebaseAuth.getInstance()

    // 👤 Registrar usuario en Supabase
    suspend fun registrarUsuario(
        nombre: String,
        correo: String,
        telefono: String,
        direccion: String,
        documento: String
    ): Boolean = withContext(Dispatchers.IO) {
        val uid = auth.currentUser?.uid ?: return@withContext false

        try {
            client.from("usuarios").insert(
                mapOf(
                    "uid" to uid,
                    "nombre" to nombre,
                    "correo" to correo,
                    "telefono" to telefono,
                    "direccion" to direccion,
                    "documento" to documento
                )
            )
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    // 🏗 Listar proyectos disponibles
    suspend fun listarProyectos(): List<Map<String, Any?>> = withContext(Dispatchers.IO) {
        try {
            client.from("proyectos")
                .select()
                .decodeList<Map<String, Any?>>()
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    // 💸 Registrar una compra
    suspend fun registrarCompra(proyectoId: Long, porcentaje: Double): Boolean = withContext(Dispatchers.IO) {
        val uid = auth.currentUser?.uid ?: return@withContext false

        try {
            client.from("compras").insert(
                mapOf(
                    "uid" to uid,
                    "proyecto_id" to proyectoId,
                    "porcentaje_comprado" to porcentaje
                )
            )
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    // 📜 Listar compras del usuario actual
    suspend fun listarMisCompras(): List<Map<String, Any?>> = withContext(Dispatchers.IO) {
        val uid = auth.currentUser?.uid ?: return@withContext emptyList()

        try {
            client.from("compras")
                .select {
                    filter { eq("uid", uid) }
                }
                .decodeList<Map<String, Any?>>()
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
}
