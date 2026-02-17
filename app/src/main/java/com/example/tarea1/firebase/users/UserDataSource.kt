package com.example.tarea1.firebase.users

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

// Capa que toca directamente colección usuarios en Firestore.
class UserDataSource(
    private val firestore: FirebaseFirestore
) {

    private val USERS_COLLECTION = "usuarios"
    private val KEYBOARDS_COLLECTION = "teclados"
    private val FIELD_AUTH_UID = "authUid"
    private val INIT_DOC_ID = "init_doc"

    private val usersCollection = firestore.collection(USERS_COLLECTION)

    // Comprueba si ya existe un documento con ese correo.
    suspend fun existsUserWithEmail(email: String): Boolean {
        return withContext(Dispatchers.IO) {
            val query = usersCollection
                .whereEqualTo("correo", email)
                .limit(1)
                .get()
                .await()

            !query.isEmpty
        }
    }

    // Saca el id numérico mayor de la colección para seguir con 1, 2, 3...
    suspend fun getMaxUserId(): Int {
        return withContext(Dispatchers.IO) {
            val snapshot = usersCollection.get().await()
            snapshot.documents
                .mapNotNull { it.id.toIntOrNull() }
                .maxOrNull() ?: 0
        }
    }

    // Crea doc usuario y deja creada su subcolección "teclados".
    // Firestore no guarda colecciones vacías reales, así que metemos un doc técnico init_doc.
    // Luego ese doc se filtra en la app para que funcionalmente sea una colección vacía.
    suspend fun createUserDocument(userDocId: Int, authUid: String, email: String, birthDate: String) {
        withContext(Dispatchers.IO) {
            val userDoc = hashMapOf(
                FIELD_AUTH_UID to authUid,
                "correo" to email,
                "fechaNacimiento" to birthDate
            )

            val userRef = usersCollection.document(userDocId.toString())
            userRef.set(userDoc).await()

            val initDoc = mapOf("init" to true)
            userRef.collection(KEYBOARDS_COLLECTION)
                .document(INIT_DOC_ID)
                .set(initDoc)
                .await()
        }
    }
}
