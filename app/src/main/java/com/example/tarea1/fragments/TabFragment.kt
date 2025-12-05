package com.example.tarea1.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.android.material.tabs.TabLayoutMediator // ⚠️ CLAVE: Conecta las pestañas con el contenido deslizable.
import com.example.tarea1.R // Para acceder a los strings de los títulos de las pestañas.
import com.example.tarea1.databinding.FragmentTabBinding // El binding para el layout que contiene el TabLayout y el ViewPager2.
import com.example.tarea1.viewpager.ViewPagerAdapter // El adaptador que sabe qué Fragments mostrar.

// Este Fragment actúa como el CONTENEDOR principal que aloja las dos pestañas (Lista y Favoritos).
class TabFragment : Fragment() {

    // ----------------------------------------------------------------------
    // 1. View Binding
    // ----------------------------------------------------------------------

    private var _binding: FragmentTabBinding? = null
    // Acceso seguro al binding. Esto nos da acceso a binding.tabLayout y binding.viewPager.
    private val binding get() = _binding!!

    // ----------------------------------------------------------------------
    // 2. Creación de la Vista (Layout XML)
    // ----------------------------------------------------------------------

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inicialización del binding para fragment_tab.xml
        _binding = FragmentTabBinding.inflate(inflater, container, false)
        return binding.root // Devolvemos la vista que contiene el TabLayout y el ViewPager2.
    }

    // ----------------------------------------------------------------------
    // 3. La Vista ha sido Creada (Configuración de Pestañas)
    // ----------------------------------------------------------------------

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1. Inicializa el Adaptador de Fragmentos
        // El adaptador es el encargado de proveer los Fragments (ListFragment y FavFragment).
        // Usamos requireActivity() porque el ViewPager2 es un componente de la Activity
        // y necesita una referencia a su gestor de estado. Require lo "requiere"
        val adapter = ViewPagerAdapter(requireActivity())

        // 2. Asigna el adaptador al ViewPager2
        // Le decimos al ViewPager2: "Usa este adaptador para saber qué Fragments mostrar y en qué orden".
        binding.viewPager.adapter = adapter

        // 3. Conecta el TabLayout (etiquetas) con el ViewPager2 (contenido deslizable)
        // TabLayoutMediator es la herramienta para sincronizar ambos componentes.
        // Pasamos como argumentos los bindeos del viewpager y el tablayout, y la posición en la pestaña
        // Como lambda
        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            // Esta función se llama para cada pestaña que se crea.

            // Asigna los títulos de las pestañas basándose en la posición (0, 1, 2, etc.)
            tab.text = when (position) {
                0 -> getString(R.string.title_list) // Primera pestaña (posición 0) -> "Lista de Teclados"
                1 -> getString(R.string.title_favs) // Segunda pestaña (posición 1) -> "Favoritos"
                else -> ""
            }
        }.attach() // 'attach()' debe llamarse para que la conexión funcione.
    }

    // ----------------------------------------------------------------------
    // 4. Limpieza
    // ----------------------------------------------------------------------

    override fun onDestroyView() {
        super.onDestroyView()
        binding.viewPager.adapter = null
        _binding = null
    }
}