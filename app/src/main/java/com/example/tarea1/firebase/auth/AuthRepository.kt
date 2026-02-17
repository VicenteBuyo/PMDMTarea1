package com.example.tarea1.firebase.auth

import com.google.firebase.auth.FirebaseUser

// Repositorio de auth.
// delega en datasource para desacoplar el VM de Firebase.
class AuthRepository(
    private val ds: AuthDataSource
) {

    // Login.
    suspend fun signIn(email: String, password: String): Result<FirebaseUser> =
        ds.signInWithEmail(email, password)

    // Registro.
    suspend fun signUp(email: String, password: String): Result<FirebaseUser> =
        ds.signUpWithEmail(email, password)

    // Logout.
    fun signOut() = ds.signOut()

    // Reset password.
    suspend fun resetPassword(email: String): Result<Unit> =
        ds.sendPasswordReset(email)

    // Usuario actual.
    fun currentUser(): FirebaseUser? = ds.currentUser()
}
