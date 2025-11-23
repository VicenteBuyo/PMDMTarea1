package com.example.tarea1

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.tarea1.databinding.ActivityMainBinding


// Punto de entrada de la aplicación (Single Activity Architecture)
// Inicializa el entorno y carga el conenedor que gestiona la navegación (NavHostFragment)
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1. Primero inflamos el diseño (creamos la vista en memoria)
        binding = ActivityMainBinding.inflate(layoutInflater)

        // 2. Establecemos la vista en la pantalla
        setContentView(binding.root)

        // 3. Activamos el diseño borde a borde (opcional, pero viene por defecto)
        enableEdgeToEdge()

        // 4. Ajustamos los márgenes para las barras del sistema usando 'binding.root'
        // binding.root es el contenedor principal (ConstraintLayout)
        // así no nos importa qué ID tenga puesto en el XML.
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
}