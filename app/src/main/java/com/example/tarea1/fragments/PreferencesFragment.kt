package com.example.tarea1.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.tarea1.databinding.FragmentPreferencesBinding

// Fragment encargado de mostrar la pantalla de Preferencias
class PreferencesFragment : Fragment() {

    // Binding del layout. Se declara nullable porque solo existe
    // entre onCreateView() y onDestroyView()
    private var _binding: FragmentPreferencesBinding? = null

    // Getter seguro para acceder al binding sin escribir !! tod o el rato
    private val binding get() = _binding!!

    // ------------------------------------------------------------------
    // 1. Creación de la vista del Fragment
    // ------------------------------------------------------------------
    override fun onCreateView(
        inflater: LayoutInflater,          // Se encarga de "inflar" el XML
        container: ViewGroup?,              // Vista padre (la activity)
        savedInstanceState: Bundle?
    ): View {

        // Inflamos el layout fragment_preferences.xml usando ViewBinding
        _binding = FragmentPreferencesBinding.inflate(inflater, container, false)

        // Devolvemos la vista raíz para que Android la pinte en pantalla
        return binding.root
    }

    // ------------------------------------------------------------------
    // 2. Limpieza de memoria
    // ------------------------------------------------------------------
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
