package com.example.tarea1.firebase.keyboards

import com.example.tarea1.models.Keyboard
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class KeyboardDataSource(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) {

    private companion object {
        const val COL_USUARIOS = "usuarios"
        const val COL_KEYBOARDS = "teclados"
        const val INIT_DOC_ID = "init_doc"
        const val LEGACY_INIT_DOC_ID = "__init__"

        const val FIELD_AUTH_UID = "authUid"
        const val FIELD_CORREO = "correo"
        const val FIELD_TITULO = "titulo"
        const val FIELD_DESCRIPCION = "descripcion"
        const val FIELD_FAV = "fav"

        // Catálogo base que quiero para todos los usuarios nuevos.
        // Se guarda en código y se mete en Firestore solo cuando la colección está vacía.
        val DEFAULT_KEYBOARDS = listOf(
            mapOf(
                FIELD_TITULO to "GMMK Pro (75%)",
                FIELD_DESCRIPCION to "Este teclado modular de alta gama tiene layout 75%, montaje Gasket y carcasa de aluminio CNC. Ideal para personalizar switches y keycaps con una escritura suave y sonido profundo.",
                FIELD_FAV to false
            ),
            mapOf(
                FIELD_TITULO to "Keychron Q1 Pro (75%)",
                FIELD_DESCRIPCION to "Teclado inalámbrico 75% con dial giratorio, carcasa de aluminio CNC y conectividad tri-modo (Bluetooth, 2.4Ghz y cable). Compatible con macOS y Windows.",
                FIELD_FAV to false
            ),
            mapOf(
                FIELD_TITULO to "MonsGeek M1W (75%)",
                FIELD_DESCRIPCION to "Muy buena relación calidad-precio. Layout 75%, hot-swappable y keycaps PBT. Monta switches AKKO V3 Pro con tacto rápido y sonido muy agradable.",
                FIELD_FAV to false
            ),
            mapOf(
                FIELD_TITULO to "NuPhy Air75 V2 (75%)",
                FIELD_DESCRIPCION to "Mecánico low profile, fino y portátil, con conectividad triple (Bluetooth 5.0, 2.4Ghz y cable). Compatible con Windows, macOS, Android e iOS.",
                FIELD_FAV to false
            ),
            mapOf(
                FIELD_TITULO to "Akko 3098B (96%)",
                FIELD_DESCRIPCION to "Formato 96% para mantener teclado numérico en menos espacio. Triple conexión (USB-C, Bluetooth 5.0 y 2.4Ghz), switches Akko CS Jelly Pink y keycaps PBT.",
                FIELD_FAV to false
            )
        )
    }

    // Necesito uid sí o sí para saber de qué usuario estoy leyendo/escribiendo.
    private fun requireUid(): String {
        return auth.currentUser?.uid ?: throw IllegalStateException()
    }

    // Como el doc de usuario se guarda con id incremental (1,2,3)
    // aquí resuelvo cuál es el doc real del usuario autenticado.
    private suspend fun resolveCurrentUserDocumentId(): String {
        val authUid = requireUid()

        // Caso principal: buscar por authUid.
        val byAuthUid = firestore.collection(COL_USUARIOS)
            .whereEqualTo(FIELD_AUTH_UID, authUid)
            .limit(1)
            .get()
            .await()
        if (!byAuthUid.isEmpty) return byAuthUid.documents.first().id

        // Fallback por correo (compatibilidad con datos antiguos).
        val currentEmail = auth.currentUser?.email
        if (!currentEmail.isNullOrBlank()) {
            val byEmail = firestore.collection(COL_USUARIOS)
                .whereEqualTo(FIELD_CORREO, currentEmail.trim().lowercase())
                .limit(1)
                .get()
                .await()
            if (!byEmail.isEmpty) return byEmail.documents.first().id
        }

        // Último fallback: usar uid como id de documento.
        return authUid
    }

    // Ruta final: usuarios/{docUsuario}/teclados
    private suspend fun keyboardsCollection() =
        firestore.collection(COL_USUARIOS)
            .document(resolveCurrentUserDocumentId())
            .collection(COL_KEYBOARDS)

    // Si no hay teclados reales, meto catálogo base una sola vez.
    private suspend fun ensureDefaultKeyboardsIfNeeded(collection: CollectionReference) {
        val snapshot = collection.get().await()
        val hasRealKeyboards = snapshot.documents.any { doc ->
            doc.id != INIT_DOC_ID && doc.id != LEGACY_INIT_DOC_ID
        }

        if (hasRealKeyboards) return

        DEFAULT_KEYBOARDS.forEach { keyboardData ->
            collection.add(keyboardData).await()
        }
    }

    suspend fun getKeyboards(): List<Keyboard> {
        return withContext(Dispatchers.IO) {
            val collection = keyboardsCollection()

            // Aquí meto la lista base si todavía no hay datos.
            ensureDefaultKeyboardsIfNeeded(collection)

            val snapshot = collection.get().await()
            snapshot.documents
                .filter { doc -> doc.id != INIT_DOC_ID && doc.id != LEGACY_INIT_DOC_ID }
                .map { doc ->
                    Keyboard(
                        id = doc.id,
                        title = doc.getString(FIELD_TITULO) ?: "",
                        description = doc.getString(FIELD_DESCRIPCION) ?: "",
                        fav = doc.getBoolean(FIELD_FAV) ?: false
                    )
                }
                .sortedBy { it.title.lowercase() }
        }
    }

    suspend fun getFavoriteKeyboards(): List<Keyboard> {
        return withContext(Dispatchers.IO) {
            val snapshot = keyboardsCollection()
                .whereEqualTo(FIELD_FAV, true)
                .get()
                .await()

            snapshot.documents
                .filter { doc -> doc.id != INIT_DOC_ID && doc.id != LEGACY_INIT_DOC_ID }
                .map { doc ->
                    Keyboard(
                        id = doc.id,
                        title = doc.getString(FIELD_TITULO) ?: "",
                        description = doc.getString(FIELD_DESCRIPCION) ?: "",
                        fav = doc.getBoolean(FIELD_FAV) ?: false
                    )
                }
                .sortedBy { it.title.lowercase() }
        }
    }

    suspend fun addKeyboard(
        titulo: String,
        descripcion: String,
        fav: Boolean = false
    ): String {
        return withContext(Dispatchers.IO) {
            val data = mapOf(
                FIELD_TITULO to titulo,
                FIELD_DESCRIPCION to descripcion,
                FIELD_FAV to fav
            )

            val ref = keyboardsCollection().add(data).await()
            ref.id
        }
    }

    suspend fun updateFav(keyboardId: String, fav: Boolean) {
        withContext(Dispatchers.IO) {
            keyboardsCollection()
                .document(keyboardId)
                .update(FIELD_FAV, fav)
                .await()
        }
    }
}
