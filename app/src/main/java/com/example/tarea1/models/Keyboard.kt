package com.example.tarea1.models

// Modelo base de cada teclado que mostramos en lista/favoritos.
data class Keyboard(
    val id: String = "",          // id del documento en Firestore
    val title: String = "",       // nombre del teclado
    val description: String = "", // descripción
    val fav: Boolean = false       // marcado como favorito o no
)
