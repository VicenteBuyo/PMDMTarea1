package com.example.tarea1.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.tarea1.databinding.FragmentPreferencesBinding

// Pantalla de preferencias.
class PreferencesFragment : Fragment() {

    private var _binding: FragmentPreferencesBinding? = null
    private val binding get() = _binding!!

    // Infla la vista de preferencias.
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPreferencesBinding.inflate(inflater, container, false)
        return binding.root
    }

    // Limpia binding cuando la vista se destruye.
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
