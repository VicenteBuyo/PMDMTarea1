package com.example.tarea1.recycler

// Clase data class que va a albergar los teclados que mostraremos
data class Keyboard (
    val title:String, // Nombre del teclado
    val description:String, // Descripción
    val imagenId: Int, // ID de la imagen asociada
    var fav: Boolean){ // Boolean que indica si lo hemos marcado como favorito
}