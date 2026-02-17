package com.example.tarea1.viewmodels.users

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tarea1.firebase.auth.AuthRepository
import com.example.tarea1.firebase.users.UserUiState
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuthViewModel(
    private val repo: AuthRepository
) : ViewModel() {

    // Estado del botón login.
    private val _isLoginButtonEnabled = MutableStateFlow(false)
    val isLoginButtonEnabled: StateFlow<Boolean> = _isLoginButtonEnabled.asStateFlow()

    // Estado global de autenticación (idle, loading, ok, error).
    private val _userUiState = MutableStateFlow<UserUiState>(UserUiState.Idle)
    val userUiState: StateFlow<UserUiState> = _userUiState.asStateFlow()

    // Resultado puntual de recuperación de contraseña.
    private val _passwordResetResult = MutableSharedFlow<PasswordResetResult>(extraBufferCapacity = 1)
    val passwordResetResult: SharedFlow<PasswordResetResult> = _passwordResetResult.asSharedFlow()

    // Valores del formulario. Cada cambio vuelve a validar.
    var username: String = ""
        set(value) {
            field = value
            checkCredentialsValidity()
        }

    var password: String = ""
        set(value) {
            field = value
            checkCredentialsValidity()
        }

    fun checkCredentialsValidity() {
        // Login habilitado solo con usuario no vacío y pass de 6+.
        val isValid = username.isNotBlank() && password.length >= 6
        _isLoginButtonEnabled.value = isValid
    }

    fun performLogin() {
        signIn(username, password)
    }

    fun signIn(email: String, password: String) {
        viewModelScope.launch {
            _userUiState.value = UserUiState.Loading

            val result = repo.signIn(email.trim(), password.trim())
            if (result.isSuccess) {
                val user = result.getOrNull()
                if (user != null) {
                    _userUiState.value = UserUiState.Authenticated(user = user)
                } else {
                    _userUiState.value = UserUiState.Error(message = "")
                }
            } else {
                _userUiState.value = UserUiState.Error(message = mensajes(result.exceptionOrNull()))
            }
        }
    }

    fun signUp(email: String, password: String) {
        viewModelScope.launch {
            _userUiState.value = UserUiState.Loading

            val result = repo.signUp(email.trim(), password.trim())
            if (result.isSuccess) {
                val user = result.getOrNull()
                if (user != null) {
                    _userUiState.value = UserUiState.Authenticated(user = user)
                } else {
                    _userUiState.value = UserUiState.Error(message = "")
                }
            } else {
                _userUiState.value = UserUiState.Error(message = mensajes(result.exceptionOrNull()))
            }
        }
    }

    fun resetPassword(email: String = username) {
        if (email.isBlank()) {
            _passwordResetResult.tryEmit(PasswordResetResult.EmptyEmail)
            return
        }

        viewModelScope.launch {
            val result = repo.resetPassword(email.trim())
            if (result.isSuccess) {
                _passwordResetResult.tryEmit(PasswordResetResult.Sent)
            } else {
                _passwordResetResult.tryEmit(PasswordResetResult.Failed)
            }
        }
    }

    fun signOut() {
        repo.signOut()
        _userUiState.value = UserUiState.Idle
    }

    fun currentUser(): FirebaseUser? = repo.currentUser()

    private fun mensajes(t: Throwable?): String {
        return t?.message ?: ""
    }

    enum class PasswordResetResult {
        Sent,
        EmptyEmail,
        Failed
    }
}
