package com.example.tarea1.firebase.users

import com.google.firebase.auth.FirebaseUser

// Estados del flujo de creación de cuenta.
sealed class NewUserUiState {

    // Estado inicial.
    object Idle : NewUserUiState()

    // Alta en proceso.
    object Loading : NewUserUiState()

    // Alta completada.
    data class Created(val user: FirebaseUser) : NewUserUiState()

    // Error durante alta, para poder mapear mensaje en la vista.
    data class Error(val type: Type) : NewUserUiState() {
        enum class Type {
            PasswordMismatch,
            PasswordTooShort,
            EmailAlreadyExists,
            Generic
        }
    }
}
