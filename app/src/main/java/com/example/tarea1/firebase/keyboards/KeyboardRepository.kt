package com.example.tarea1.firebase.keyboards

import com.example.tarea1.models.Keyboard

// Repositorio de teclados.
// Puente entre ViewModel y datasource de Firestore.
class KeyboardRepository(
    private val keyboardDataSource: KeyboardDataSource
) {

    // Lista completa de teclados del usuario actual.
    suspend fun getKeyboards(): List<Keyboard> = keyboardDataSource.getKeyboards()

    // Lista solo de favoritos.
    suspend fun getFavoriteKeyboards(): List<Keyboard> = keyboardDataSource.getFavoriteKeyboards()

    // Inserta un teclado nuevo y devuelve el id de documento.
    suspend fun addKeyboard(
        title: String,
        description: String,
        fav: Boolean = false
    ): String = keyboardDataSource.addKeyboard(title, description, fav)

    // Cambia estado favorito de un teclado.
    suspend fun setKeyboardFavorite(keyboardId: String, fav: Boolean) {
        keyboardDataSource.updateFav(keyboardId, fav)
    }
}
