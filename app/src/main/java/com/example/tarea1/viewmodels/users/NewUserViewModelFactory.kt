package com.example.tarea1.viewmodels.users

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.tarea1.firebase.auth.AuthRepository
import com.example.tarea1.firebase.users.UserRepository

// Factory para NewUserViewModel (necesita authRepository y userRepository).
class NewUserViewModelFactory(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository
) : ViewModelProvider.Factory {

    // Crea y devuelve NewUserViewModel con sus dos dependencias.
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(NewUserViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return NewUserViewModel(authRepository, userRepository) as T
        }

        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
