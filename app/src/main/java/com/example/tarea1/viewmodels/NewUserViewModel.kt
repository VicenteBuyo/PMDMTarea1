package com.example.tarea1.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

//ViewModel del SignupFragment
//Mantiene el estado del formulario y gestiona la lógica de validación y registro
//Comunica el resultado al LiveData del SignupFragment

class NewUserViewModel : ViewModel() {

    // VARIABLES QUE ALMACENAN LOS DATOS DE LA VISTA
    var username = ""
    var password = ""
    var confirmPassword = ""
    var birthDate = ""


    // LIVEDATA 1: Controla si el botón "Crear cuenta" está habilitado o gris
    private val _isButtonEnabled = MutableLiveData(false)
    val isButtonEnabled: LiveData<Boolean> get() = _isButtonEnabled

    // LIVEDATA 2: Notifica si hubo un error de contraseñas que no coinciden
    private val _passwordMismatchError = MutableLiveData(false)
    val passwordMismatchError: LiveData<Boolean> get() = _passwordMismatchError

    // LIVEDATA 3: Notifica si el registro fue exitoso para navegar al Login
    private val _registrationSuccess = MutableLiveData(false)
    val registrationSuccess: LiveData<Boolean> get() = _registrationSuccess

    /**
     * Función llamada cada vez que el usuario escribe algo.
     * Verifica las longitudes mínimas según el enunciado.
     */
    fun validateInputData() {
        // Requisito: Usuario al menos 1 carácter, Contraseñas al menos 4 caracteres
        val isValid = username.isNotEmpty() &&
                password.length >= 4 &&
                confirmPassword.length >= 4

        _isButtonEnabled.value = isValid
    }

    /**
     * Función llamada al pulsar el botón "Crear Cuenta".
     * Verifica si las contraseñas coinciden.
     */
    fun onRegisterClicked() {
        // Requisito: Comprobar si ambas contraseñas coinciden
        if (password == confirmPassword) {
            // SI COINCIDEN: Éxito, navegamos al Login
            _passwordMismatchError.value = false
            _registrationSuccess.value = true
        } else {
            // NO COINCIDEN: Error, marcamos los campos en rojo
            _passwordMismatchError.value = true
        }
    }
}