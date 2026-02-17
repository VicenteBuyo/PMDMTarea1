package com.example.tarea1.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.tarea1.databinding.FragmentFavBinding
import com.example.tarea1.models.Keyboard
import com.example.tarea1.recycler.KeyboardAdapter
import com.example.tarea1.viewmodels.ListViewModel
import kotlinx.coroutines.launch

// Pestaña de favoritos. Solo enseña teclados con fav=true.
class FavFragment : Fragment() {

    private var _binding: FragmentFavBinding? = null
    private val binding get() = _binding!!

    // Mismo VM compartido con ListFragment.
    private val viewModel: ListViewModel by activityViewModels()

    private lateinit var keyboardAdapter: KeyboardAdapter

    // Estado local de filtro + orden para esta pestaña.
    private var currentQuery: String = ""
    private var sortAsc: Boolean = true

    private companion object {
        const val FILTER_REQUEST_FAV = "filter_request_fav"
        const val SORT_REQUEST_FAV = "sort_request_fav"
    }

    // Infla layout de favoritos.
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFavBinding.inflate(inflater, container, false)
        return binding.root
    }

    // Configura recycler y listeners.
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        keyboardAdapter = KeyboardAdapter(
            keyboardList = emptyList(),
            onFavoriteClick = object : KeyboardAdapter.OnFavoriteClickListener {
                override fun onFavoriteClick(keyboardId: String) {
                    viewModel.toggleFavorite(keyboardId)
                }
            },
            isFavView = true
        )

        binding.rvFavs.adapter = keyboardAdapter
        binding.rvFavs.layoutManager = LinearLayoutManager(context)

        setupFragmentResults()
        setupCollectors()
    }

    // Escucha eventos de búsqueda y orden enviados desde MainActivity.
    private fun setupFragmentResults() {
        parentFragmentManager.setFragmentResultListener(FILTER_REQUEST_FAV, viewLifecycleOwner) { _, bundle ->
            currentQuery = bundle.getString("query").orEmpty()
            renderFavList(viewModel.keyboardList.value)
        }

        parentFragmentManager.setFragmentResultListener(SORT_REQUEST_FAV, viewLifecycleOwner) { _, bundle ->
            sortAsc = bundle.getBoolean("asc", true)
            renderFavList(viewModel.keyboardList.value)
        }
    }

    // Recoge lista compartida y repinta favoritos cuando cambie.
    private fun setupCollectors() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.keyboardList.collect { fullList ->
                    renderFavList(fullList)
                }
            }
        }
    }

    // Aplica: 1) solo favoritos, 2) filtro por texto, 3) orden A-Z / Z-A.
    private fun renderFavList(fullList: List<Keyboard>) {
        val onlyFavs = fullList.filter { it.fav }

        val filtered = if (currentQuery.isBlank()) {
            onlyFavs
        } else {
            onlyFavs.filter { it.title.contains(currentQuery, ignoreCase = true) }
        }

        val sorted = if (sortAsc) {
            filtered.sortedBy { it.title.lowercase() }
        } else {
            filtered.sortedByDescending { it.title.lowercase() }
        }

        keyboardAdapter.submitList(sorted)
    }

    // Limpieza normal de binding.
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
