package com.example.tarea1.fragments

import android.media.MediaPlayer
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.tarea1.R
import com.example.tarea1.alertdialog.AddKeyboardDialog
import com.example.tarea1.databinding.FragmentListBinding
import com.example.tarea1.models.Keyboard
import com.example.tarea1.recycler.KeyboardAdapter
import com.example.tarea1.viewmodels.ListViewModel
import kotlinx.coroutines.launch

class ListFragment : Fragment() {

    private var _binding: FragmentListBinding? = null
    private val binding get() = _binding!!

    // Comparto VM con FavFragment para que ambos vean la misma lista en tiempo real.
    private val viewModel: ListViewModel by activityViewModels()

    private lateinit var keyboardAdapter: KeyboardAdapter

    // Estado local de filtros de esta pestaña.
    private var currentQuery: String = ""
    private var sortAsc: Boolean = true

    private companion object {
        const val FILTER_REQUEST_LIST = "filter_request_list"
        const val SORT_REQUEST_LIST = "sort_request_list"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecycler()
        setupFragmentResults()
        setupCollectors()

        // Recargo al entrar para traer datos frescos del usuario logueado.
        viewModel.refreshKeyboards()
    }

    private fun setupRecycler() {
        keyboardAdapter = KeyboardAdapter(
            keyboardList = emptyList(),
            onFavoriteClick = object : KeyboardAdapter.OnFavoriteClickListener {
                override fun onFavoriteClick(keyboardId: String) {
                    // Cambio fav y se persiste en Firestore.
                    viewModel.toggleFavorite(keyboardId)
                }
            },
            isFavView = false
        )

        binding.rv.adapter = keyboardAdapter
        binding.rv.layoutManager = LinearLayoutManager(context)
    }

    private fun setupFragmentResults() {
        // Texto de búsqueda que llega desde toolbar.
        parentFragmentManager.setFragmentResultListener(FILTER_REQUEST_LIST, viewLifecycleOwner) { _, bundle ->
            currentQuery = bundle.getString("query").orEmpty()
            renderList(viewModel.keyboardList.value)
        }

        // Orden asc/desc que llega desde toolbar.
        parentFragmentManager.setFragmentResultListener(SORT_REQUEST_LIST, viewLifecycleOwner) { _, bundle ->
            sortAsc = bundle.getBoolean("asc", true)
            renderList(viewModel.keyboardList.value)
        }

        // Señal del FAB para abrir diálogo de alta.
        parentFragmentManager.setFragmentResultListener("add_keyboard_request", viewLifecycleOwner) { _, _ ->
            showAddKeyboardDialog()
        }
    }

    private fun setupCollectors() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {

                launch {
                    // Cuando cambia la lista en el VM, la vuelvo a pintar con filtros/orden.
                    viewModel.keyboardList.collect { keyboards ->
                        renderList(keyboards)
                    }
                }

                launch {
                    // Progress de carga mientras se consulta Firebase.
                    viewModel.isLoading.collect { isLoading ->
                        binding.pbLoading.visibility = if (isLoading) View.VISIBLE else View.GONE
                        binding.rv.visibility = if (isLoading) View.GONE else View.VISIBLE
                    }
                }

                launch {
                    // Sonido de favorito cada vez que toggle va bien.
                    viewModel.playAudioEvent.collect {
                        playFavoriteSound()
                    }
                }

                launch {
                    // Cualquier error lo paso a un mensaje sencillo.
                    viewModel.errorEvent.collect { error ->
                        val messageRes = when (error) {
                            ListViewModel.ErrorType.Load -> R.string.error_load_keyboards
                            ListViewModel.ErrorType.Add -> R.string.error_add_keyboard
                            ListViewModel.ErrorType.UpdateFavorite -> R.string.error_update_favorite
                        }
                        Toast.makeText(requireContext(), getString(messageRes), Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }

    private fun renderList(fullList: List<Keyboard>) {
        // Filtro por texto.
        val filtered = if (currentQuery.isBlank()) {
            fullList
        } else {
            fullList.filter { it.title.contains(currentQuery, ignoreCase = true) }
        }

        // Orden por título.
        val sorted = if (sortAsc) {
            filtered.sortedBy { it.title.lowercase() }
        } else {
            filtered.sortedByDescending { it.title.lowercase() }
        }

        keyboardAdapter.submitList(sorted)
    }

    private fun showAddKeyboardDialog() {
        AddKeyboardDialog.show(
            context = requireContext(),
            inflater = layoutInflater
        ) { title, description, fav ->
            // Alta de teclado nueva y refresco automático al terminar.
            viewModel.addKeyboard(title = title, description = description, fav = fav)
        }
    }

    private fun playFavoriteSound() {
        val mediaPlayer = MediaPlayer.create(requireContext(), R.raw.favorite_toggle)
        mediaPlayer?.setOnCompletionListener { it.release() }
        mediaPlayer?.start()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
