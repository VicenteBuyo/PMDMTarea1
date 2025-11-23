package com.example.tarea1.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

//ViewModel del LoginFragment
//Mantiene el estado del formulario y gestiona la lógica de validación y autenticación
//Comunica el resultado al LiveData del LoginFragment
class AuthViewModel: ViewModel (){
    // Variables que usamos para notificar a la View de los cambios de estado
    private val _isLoginButtonEnabled = MutableLiveData<Boolean>() //Mutable, puede cambiar su valor
    val isLoginButtonEnabled: LiveData<Boolean> = _isLoginButtonEnabled //Inmutable, LoginFragment lo observa

    private val _loginResult = MutableLiveData<Boolean>() //Mutable, puede cambiar su valor
    val loginResult: LiveData<Boolean> = _loginResult //Inmutable, LoginFragment lo observa

    //Variables username y password
    //Cada vez que el LoginFragment actualiza las variables se ejecuta el set y
    // checkCredenTialsValidity() comprueba su validez para activar el botón de Login
    var username: String = ""
        set(value){
            field = value
            checkCredentialsValidity()
        }
    var password: String = ""
        set(value){
            field = value
            checkCredentialsValidity()
        }

    // Función para checkear longitud de usuario y contraseña
    fun checkCredentialsValidity(){
        val isValid = username.length >= 1 && password.length >= 4

        _isLoginButtonEnabled.value = isValid
    }

    // Función para checkear si las credenciales son correctas
    fun performLogin(){
        val success = username == "admin" && password == "1234"

        _loginResult.value = success
    }
}