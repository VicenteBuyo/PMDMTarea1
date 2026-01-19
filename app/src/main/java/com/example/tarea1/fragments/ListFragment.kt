package com.example.tarea1.fragments

import android.media.MediaPlayer
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.tarea1.R
import com.example.tarea1.databinding.FragmentListBinding
import com.example.tarea1.recycler.Keyboard
import com.example.tarea1.recycler.KeyboardAdapter
import com.example.tarea1.viewmodels.ListViewModel
import java.util.Collections
import java.util.Locale

// Este Fragment es la VISTA principal que muestra la lista completa de teclados.
class ListFragment : Fragment() {

    // ----------------------------------------------------------------------
    // 1. View Binding y Acceso al ViewModel
    // ----------------------------------------------------------------------

    private var _binding: FragmentListBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: ListViewModel

    // ----------------------------------------------------------------------
    // 1.1 Estado de filtro/orden (lo que nos llega desde la toolbar)
    // ----------------------------------------------------------------------

    // Guardamos el texto actual de búsqueda para poder aplicarlo siempre que cambie la lista
    private var filtroActual: String = ""

    // Guardamos el estado de orden (asc/desc) para aplicarlo igual
    private var ordenAscendente: Boolean = true

    // Guardamos la última lista completa que nos da el ViewModel
    private var ultimaLista: List<Keyboard> = emptyList()

    // Adapter
    private lateinit var keyboardAdapter: KeyboardAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentListBinding.inflate(inflater, container, false)
        return binding.root
    }

    // ----------------------------------------------------------------------
    // 3. La Vista ha sido Creada (Configuración de Lógica)
    // ----------------------------------------------------------------------

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1) Cogemos el ViewModel de la Activity para compartirlo con FavFragment
        viewModel = ViewModelProvider(requireActivity())[ListViewModel::class.java]

        // 2) Creamos el adapter: usando interfaz
        keyboardAdapter = KeyboardAdapter(
            emptyList(),
            object : KeyboardAdapter.OnFavoriteClickListener {
                override fun onFavoriteClick(keyboardTitle: String) {
                    // La VISTA le pide al ViewModel que cambie el favorito
                    viewModel.toggleFavorite(keyboardTitle)
                }
            },
            false
        )

        // 3) RecyclerView
        binding.rv.adapter = keyboardAdapter
        binding.rv.layoutManager = LinearLayoutManager(context)

        // ----------------------------------------------------------------------
        // 3.2 Recibimos eventos de la toolbar (búsqueda y orden
        // ----------------------------------------------------------------------

        // Listener del filtro (texto de búsqueda)
        parentFragmentManager.setFragmentResultListener(
            "filter_request",
            viewLifecycleOwner
        ) { _, bundle ->
            filtroActual = bundle.getString("query", "")
            aplicarFiltroYOrden()
        }

        // Listener del orden (asc/desc)
        parentFragmentManager.setFragmentResultListener(
            "sort_request",
            viewLifecycleOwner
        ) { _, bundle ->
            ordenAscendente = bundle.getBoolean("asc", true)
            aplicarFiltroYOrden()
        }

        // ----------------------------------------------------------------------
        // 3.3 Observación del LiveData
        // ----------------------------------------------------------------------

        viewModel.keyboardList.observe(viewLifecycleOwner, Observer { updatedList ->
            // Guardamos la lista completa y repintamos con filtro/orden aplicados
            ultimaLista = updatedList
            aplicarFiltroYOrden()
        })

        viewModel.playAudioEvent.observe(viewLifecycleOwner, Observer {
            playFavoriteSound()
        })
    }

    // ----------------------------------------------------------------------
    // 3.4 Aplicar filtro y orden antes de pintar el RecyclerView
    // ----------------------------------------------------------------------

    private fun aplicarFiltroYOrden() {

        // 1) Partimos de lista completa
        val listaProcesada = ArrayList<Keyboard>()

        // 2) Filtrado
        if (filtroActual.trim().isEmpty()) {
            listaProcesada.addAll(ultimaLista)
        } else {
            val query = filtroActual.trim().lowercase(Locale.getDefault())
            for (k in ultimaLista) {
                val titulo = k.title.lowercase(Locale.getDefault())
                if (titulo.contains(query)) {
                    listaProcesada.add(k)
                }
            }
        }

        // 3) Orden
        Collections.sort(listaProcesada, object : Comparator<Keyboard> {
            override fun compare(o1: Keyboard, o2: Keyboard): Int {
                return if (ordenAscendente) {
                    o1.title.compareTo(o2.title, ignoreCase = true)
                } else {
                    o2.title.compareTo(o1.title, ignoreCase = true)
                }
            }
        })

        // 4) Pintamos en el adapter
        keyboardAdapter.submitList(listaProcesada)
    }

    // ----------------------------------------------------------------------
    // 4. Lógica de Reproducción de Audio
    // ----------------------------------------------------------------------

    private fun playFavoriteSound() {
        val ctx = context
        if (ctx != null) {
            val mediaPlayer = MediaPlayer.create(ctx, R.raw.favorite_toggle)
            if (mediaPlayer != null) {
                mediaPlayer.start()

                // Sin lambda: listener clásico
                mediaPlayer.setOnCompletionListener(object : MediaPlayer.OnCompletionListener {
                    override fun onCompletion(mp: MediaPlayer?) {
                        mp?.release()
                    }
                })
            }
        }
    }

    // ----------------------------------------------------------------------
    // 5. Limpieza
    // ----------------------------------------------------------------------

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
