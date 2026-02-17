package com.example.tarea1.firebase

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

// Punto único para obtener instancias de Firebase.
// Está en object para reutilizar una sola instancia de cada servicio.
object FirebaseProvider {

    // Auth singleton lazy con thread-safe.
    @Volatile private var auth: FirebaseAuth? = null

    // Firestore singleton lazy con  thread-safe.
    @Volatile private var firestore: FirebaseFirestore? = null

    // Devuelve FirebaseAuth. Si no existe, lo crea en ese momento.
    fun provideAuth(): FirebaseAuth =
        auth ?: synchronized(this) {
            auth ?: FirebaseAuth.getInstance().also { auth = it }
        }

    // Devuelve FirebaseFirestore. Si no existe, lo crea en ese momento.
    fun provideFirestore(): FirebaseFirestore =
        firestore ?: synchronized(this) {
            firestore ?: FirebaseFirestore.getInstance().also { firestore = it }
        }
}
