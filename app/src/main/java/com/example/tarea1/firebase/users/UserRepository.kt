package com.example.tarea1.firebase.users

// Repositorio de usuario.
// Delega en datasource y lo expone limpio al ViewModel.
class UserRepository(
    private val userDataSource: UserDataSource
) {

    // Crea el documento usuario en Firestore.
    suspend fun createUserDocument(userDocId: Int, authUid: String, email: String, birthDate: String) {
        userDataSource.createUserDocument(
            userDocId = userDocId,
            authUid = authUid,
            email = email,
            birthDate = birthDate
        )
    }

    // True si ya existe ese correo.
    suspend fun existsUserWithEmail(email: String): Boolean {
        return userDataSource.existsUserWithEmail(email)
    }

    // Devuelve id máximo actual.
    suspend fun getMaxUserId(): Int {
        return userDataSource.getMaxUserId()
    }
}
