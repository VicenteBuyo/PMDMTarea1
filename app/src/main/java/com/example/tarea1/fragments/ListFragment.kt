package com.example.tarea1.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.tarea1.databinding.FragmentListBinding
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.tarea1.recycler.KeyboardAdapter
import com.example.tarea1.viewmodels.ListViewModel
import android.media.MediaPlayer // Para reproducir sonidos
import androidx.fragment.app.activityViewModels // Para compartir el ViewModel.
import com.example.tarea1.R // Para acceder a IDs de recursos

// Este Fragment es la VISTA principal que muestra la lista completa de teclados.
class ListFragment : Fragment() {

    // ----------------------------------------------------------------------
    // 1. View Binding y Acceso al ViewModel
    // ----------------------------------------------------------------------

    // Variable mutable (null al inicio) para mantener la referencia al layout inflado.
    private var _binding: FragmentListBinding? = null
    // Getter solo para lectura
    private val binding get() = _binding!!

    // Usamos 'activityViewModels()' para obtener una ÚNICA instancia del ListViewModel.
    // Esto es vital porque esta instancia será COMPARTIDA con FavFragment, asegurando que ambos
    // trabajen con la MISMA lista de datos y estados (ej: el estado 'fav').
    private val viewModel: ListViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    // ----------------------------------------------------------------------
    // 2. Creación de la Vista (Layout XML)
    // ----------------------------------------------------------------------

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflamos el layout usando View Binding.
        _binding = FragmentListBinding.inflate(inflater, container, false)
        return binding.root // Devolvemos la vista raíz del layout inflado.
    }


    // ----------------------------------------------------------------------
    // 3. La Vista ha sido Creada (Configuración de Lógica)
    // ----------------------------------------------------------------------

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // ----------------------------------------------------------------------
        // 3.1 Inicialización del Adapter y Configuración del RecyclerView
        // ----------------------------------------------------------------------

        val keyboardAdapter = KeyboardAdapter(
            keyboardList = emptyList(), // Inicializamos con una lista vacía; los datos vendrán del LiveData.

            // LISTENER: Define la acción que el Adapter debe DELEGAR.
            onFavoriteClick = { keyboardTitle ->
                // La VISTA (Fragment) le pide al VIEWMODEL que ejecute la lógica de negocio.
                // El ViewModel se encargará de cambiar el estado interno del dato.
                viewModel.toggleFavorite(keyboardTitle)
            },

            // Indicamos que NO estamos en la vista de favoritos, por lo que el botón
            // de estrella en el ítem DEBE estar activo y visible para poder añadir favoritos.
            isFavView = false
        )

        // Configuración del RecyclerView:
        binding.rv.adapter = keyboardAdapter // Asignamos el puente (Adapter) a la lista.
        // Le decimos al RecyclerView que organice los ítems en una lista vertical.
        binding.rv.layoutManager = LinearLayoutManager(context)

        // ----------------------------------------------------------------------
        // 3.2 Observación del LiveData
        // ----------------------------------------------------------------------

        // 2.1 Observación de la lista de teclados:
        // 'observe()' es el observador. Cuando el ViewModel publica una lista actualizada,
        // automáticamente se llama a la función lambda
        viewModel.keyboardList.observe(viewLifecycleOwner) { updatedList ->
            // Le pasamos la lista más reciente al Adapter para que redibuje el RecyclerView.
            keyboardAdapter.submitList(updatedList)
        }

        // Observación del evento de audio
        // Observamos un LiveData de "evento". Cuando se dispara
        // llamamos a la función que maneja el efecto visual/auditivo.
        viewModel.playAudioEvent.observe(viewLifecycleOwner) {
            playFavoriteSound() // La VISTA maneja el audio
        }
    }

    // ----------------------------------------------------------------------
    // 4. Lógica de Reproducción de Audio (Efecto Secundario)
    // ----------------------------------------------------------------------

    private fun playFavoriteSound() {
        // LLAMADA SEGURA:
        // Usamos 'context?' para asegurar que el Fragment está anclado a un Activity.
        // ? : si es null sale del méto do y evita crash.
        // .let si no, ejecuta
        context?.let {
            // MediaPlayer.create() carga el recurso de sonido desde 'res/raw'.
            val mediaPlayer = MediaPlayer.create(it, R.raw.favorite_toggle)

            // Inicia la reproducción del sonido.
            mediaPlayer?.start()

            // ELe decimos al sistema que libere los recursos
            // del MediaPlayer una vez que el audio termine de reproducirse.
            mediaPlayer?.setOnCompletionListener { mp -> mp.release() }
        }
    }


    // ----------------------------------------------------------------------
    // 5. Limpieza (Buena práctica)
    // ----------------------------------------------------------------------

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}