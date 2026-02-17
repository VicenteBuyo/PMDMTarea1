package com.example.tarea1.firebase

import com.example.tarea1.firebase.auth.AuthDataSource
import com.example.tarea1.firebase.auth.AuthRepository
import com.example.tarea1.firebase.keyboards.KeyboardDataSource
import com.example.tarea1.firebase.keyboards.KeyboardRepository
import com.example.tarea1.firebase.users.UserDataSource
import com.example.tarea1.firebase.users.UserRepository

// Inyección de dependencias
// Aquí monto datasources/repos una sola vez y luego los reutilizo.
object ServiceLocator {

    // Capa auth: datasource (Firebase) + repository (interfaz para VMs).
    val authDataSource: AuthDataSource by lazy {
        AuthDataSource(FirebaseProvider.provideAuth())
    }

    val authRepository: AuthRepository by lazy {
        AuthRepository(authDataSource)
    }

    // Capa users: datos del documento usuario en Firestore.
    val userDataSource: UserDataSource by lazy {
        UserDataSource(FirebaseProvider.provideFirestore())
    }

    val userRepository: UserRepository by lazy {
        UserRepository(userDataSource)
    }

    // Capa keyboards: subcolección de teclados del usuario.
    val keyboardDataSource: KeyboardDataSource by lazy {
        KeyboardDataSource(
            auth = FirebaseProvider.provideAuth(),
            firestore = FirebaseProvider.provideFirestore()
        )
    }

    val keyboardRepository: KeyboardRepository by lazy {
        KeyboardRepository(keyboardDataSource)
    }
}
