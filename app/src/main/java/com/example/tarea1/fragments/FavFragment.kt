package com.example.tarea1.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels // ⚠️ CLAVE: Para compartir el ViewModel entre pestañas.
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.tarea1.databinding.FragmentFavBinding
import com.example.tarea1.recycler.KeyboardAdapter
import com.example.tarea1.viewmodels.ListViewModel

// Este Fragment representa la pestaña de Favoritos
class FavFragment : Fragment() {

    // ----------------------------------------------------------------------
    // 1. View Binding y Acceso al ViewModel
    // ----------------------------------------------------------------------

    // Variable para contener la referencia al layout (FragmentFavBinding). Se inicializa a null.
    private var _binding: FragmentFavBinding? = null
    // Propiedad calculada: Acceso seguro al binding. No nullable
    private val binding get() = _binding!!

    // Obtenemos la única instancia del ListViewModel que ya existe en el Activity.
    // Compartimos el ViewModel permite que los datos cambien en ListFragment y se reflejen aquí.
    private val viewModel: ListViewModel by activityViewModels()

    // ----------------------------------------------------------------------
    // 2. Creación de la Vista (Layout XML)
    // ----------------------------------------------------------------------

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflamos el layout usando View Binding
        _binding = FragmentFavBinding.inflate(inflater, container, false)
        return binding.root // Devolvemos la vista raíz del layout inflado
    }

    // ----------------------------------------------------------------------
    // 3. La Vista ha sido Creada (Configuración)
    // ----------------------------------------------------------------------

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1. Inicialización del Adapter
        val keyboardAdapter = KeyboardAdapter(
            keyboardList = emptyList(),
            onFavoriteClick = object : KeyboardAdapter.OnFavoriteClickListener {
                override fun onFavoriteClick(keyboardTitle: String) {
                    viewModel.toggleFavorite(keyboardTitle)
                }
            },
            isFavView = true
        )


        // Configuración del RecyclerView: Le decimos qué adaptador usar y cómo organizar la lista (verticalmente).
        binding.rvFavs.adapter = keyboardAdapter
        binding.rvFavs.layoutManager = LinearLayoutManager(context)

        // 2. Observer del LiveData
        // Observamos los cambios en la lista de teclados que el ViewModel publica.
        viewModel.keyboardList.observe(viewLifecycleOwner) { fullList ->
            // Tomamos la lista completa (fullList) que viene del ViewModel,
            // y usamos la función filter{} de Kotlin para crear una nueva lista (favsList)
            // que solo contiene los elementos donde 'it.fav' es verdadero (true).
            val favsList = fullList.filter { it.fav }

            // Enviamos la lista FILTRADA al Adapter para que el RecyclerView se redibuje.
            // Si un elemento se quita de favoritos en ListFragment, el LiveData se actualiza,
            // y aquí se filtra y desaparece de esta vista.
            keyboardAdapter.submitList(favsList)
        }
    }

    // ----------------------------------------------------------------------
    // 4. Limpieza (Memoria)
    // ----------------------------------------------------------------------

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}