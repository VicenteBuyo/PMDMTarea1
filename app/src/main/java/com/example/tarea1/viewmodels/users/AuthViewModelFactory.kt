package com.example.tarea1.viewmodels.users

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.tarea1.firebase.ServiceLocator

// Factory para crear AuthViewModel con su dependencia (AuthRepository).
class AuthViewModelFactory(
    private val locator: ServiceLocator = ServiceLocator
) : ViewModelProvider.Factory {

    // Crea y devuelve el ViewModel solicitado.
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AuthViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AuthViewModel(locator.authRepository) as T
        }

        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
