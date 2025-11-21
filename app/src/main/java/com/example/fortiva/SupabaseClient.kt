package com.example.fortiva

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.storage.Storage

object Supabase {

    val client: SupabaseClient = createSupabaseClient(
        supabaseUrl = "https://fhevcuocjxrrogocwhvo.supabase.co",
        supabaseKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImZoZXZjdW9janhycm9nb2N3aHZvIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NjIyOTU2NzQsImV4cCI6MjA3Nzg3MTY3NH0.iNUtNN5A-nLSivfcXapfC2F2Hbv4iodenQYzHXxJQEE"
    ) {
        install(Postgrest)
        install(Storage)
    }

    // Atajos opcionales para acceder fácilmente
    val db get() = client.postgrest
    val storage get() = client.storage
}
