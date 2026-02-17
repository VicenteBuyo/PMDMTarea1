package com.example.tarea1.firebase.auth

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

// Capa que llama directamente a FirebaseAuth.
// Aquí dejo login/registro/logout/reset  en un solo sitio.
class AuthDataSource(
    private val auth: FirebaseAuth
) {

    // Login por email y contraseña.
    // Suspend porque espera una llamada de red (Firebase).
    suspend fun signInWithEmail(email: String, password: String): Result<FirebaseUser> =
        withContext(Dispatchers.IO) {
            runCatching {
                auth.signInWithEmailAndPassword(email, password).await()
                auth.currentUser ?: throw IllegalStateException()
            }
        }

    // Registro por email y contraseña.
    // Valida básico y devuelve el usuario creado en auth.
    suspend fun signUpWithEmail(email: String, password: String): Result<FirebaseUser> =
        withContext(Dispatchers.IO) {
            runCatching {
                require(email.isNotBlank())
                require(password.length >= 6)
                auth.createUserWithEmailAndPassword(email, password).await()
                auth.currentUser ?: throw IllegalStateException()
            }
        }

    // Envía correo de recuperación de contraseña.
    suspend fun sendPasswordReset(email: String): Result<Unit> =
        withContext(Dispatchers.IO) {
            runCatching {
                auth.sendPasswordResetEmail(email).await()
                Unit
            }
        }

    // Cierra sesión local del usuario actual.
    fun signOut() = auth.signOut()

    // Devuelve usuario actual si hay sesión iniciada.
    fun currentUser(): FirebaseUser? = auth.currentUser
}
