package com.example.tarea1.firebase.users

import com.google.firebase.auth.FirebaseUser

// Estados del flujo de autenticación en UI.
sealed interface UserUiState {

    // Pantalla en reposo.
    object Idle : UserUiState

    // Operación en curso (login/registro auth).
    object Loading : UserUiState

    // Usuario autenticado correctamente.
    data class Authenticated(val user: FirebaseUser) : UserUiState

    // Error con mensaje para mostrar en pantalla.
    data class Error(val message: String) : UserUiState
}
