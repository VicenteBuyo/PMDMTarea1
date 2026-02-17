package com.example.tarea1.viewmodels.users

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tarea1.firebase.auth.AuthRepository
import com.example.tarea1.firebase.users.NewUserUiState
import com.example.tarea1.firebase.users.UserRepository
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class NewUserViewModel(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    // Campos del formulario. Cada cambio revalida botón.
    var username: String = ""
        set(value) {
            field = value
            validateInputData()
        }

    var password: String = ""
        set(value) {
            field = value
            _passwordMismatchError.value = false
            validateInputData()
        }

    var confirmPassword: String = ""
        set(value) {
            field = value
            _passwordMismatchError.value = false
            validateInputData()
        }

    var birthDate: String = ""
        set(value) {
            field = value
            validateInputData()
        }

    // Estado del botón crear cuenta.
    private val _isButtonEnabled = MutableStateFlow(false)
    val isButtonEnabled: StateFlow<Boolean> = _isButtonEnabled.asStateFlow()

    // Error específico de password != confirmPassword.
    private val _passwordMismatchError = MutableStateFlow(false)
    val passwordMismatchError: StateFlow<Boolean> = _passwordMismatchError.asStateFlow()

    // Estado global del flujo de alta.
    private val _uiState = MutableStateFlow<NewUserUiState>(NewUserUiState.Idle)
    val uiState: StateFlow<NewUserUiState> = _uiState.asStateFlow()

    fun validateInputData() {
        val isValid = username.isNotEmpty() &&
                password.length >= 6 &&
                confirmPassword.length >= 6 &&
                birthDate.isNotEmpty()

        _isButtonEnabled.value = isValid
    }

    fun onRegisterClicked() {
        // Si ya estoy en loading, ignoro clicks repetidos.
        if (_uiState.value is NewUserUiState.Loading) return

        _passwordMismatchError.value = false
        _uiState.value = NewUserUiState.Idle

        if (password != confirmPassword) {
            _passwordMismatchError.value = true
            _uiState.value = NewUserUiState.Error(NewUserUiState.Error.Type.PasswordMismatch)
            return
        }

        if (password.length < 6) {
            _uiState.value = NewUserUiState.Error(NewUserUiState.Error.Type.PasswordTooShort)
            return
        }

        // Normalizo email para no comparar valores con espacios/mayúsculas distintas.
        val normalizedEmail = username.trim().lowercase()

        _uiState.value = NewUserUiState.Loading
        _isButtonEnabled.value = false

        viewModelScope.launch {
            try {
                // Comprobación previa en Firestore para cortar antes si ya existe.
                if (userRepository.existsUserWithEmail(normalizedEmail)) {
                    _uiState.value = NewUserUiState.Error(NewUserUiState.Error.Type.EmailAlreadyExists)
                    validateInputData()
                    return@launch
                }

                val createdUser = authRepository.signUp(normalizedEmail, password).getOrElse { error ->
                    _uiState.value = if (error is FirebaseAuthUserCollisionException) {
                        NewUserUiState.Error(NewUserUiState.Error.Type.EmailAlreadyExists)
                    } else {
                        NewUserUiState.Error(NewUserUiState.Error.Type.Generic)
                    }
                    validateInputData()
                    return@launch
                }

                // Id incremental del documento de usuario (1,2,3...).
                val nextUserId = userRepository.getMaxUserId() + 1

                // Creo doc usuario en Firestore con correo + fecha + authUid.
                userRepository.createUserDocument(
                    userDocId = nextUserId,
                    authUid = createdUser.uid,
                    email = normalizedEmail,
                    birthDate = birthDate
                )

                _uiState.value = NewUserUiState.Created(createdUser)
            } catch (e: Exception) {
                _uiState.value = NewUserUiState.Error(NewUserUiState.Error.Type.Generic)
                validateInputData()
            }
        }
    }
}
