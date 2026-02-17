package com.example.tarea1.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.tarea1.R
import com.example.tarea1.databinding.FragmentTabBinding
import com.example.tarea1.viewpager.ViewPagerAdapter
import com.google.android.material.tabs.TabLayoutMediator

// Fragment contenedor de pestañas (lista + favoritos).
class TabFragment : Fragment() {

    private var _binding: FragmentTabBinding? = null
    private val binding get() = _binding!!

    // Infla la vista del contenedor de tabs.
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTabBinding.inflate(inflater, container, false)
        return binding.root
    }

    // Configura ViewPager y títulos de tabs.
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val adapter = ViewPagerAdapter(requireActivity())
        binding.viewPager.adapter = adapter

        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> getString(R.string.title_list)
                1 -> getString(R.string.title_favs)
                else -> ""
            }
        }.attach()
    }

    // Limpia adapter y binding al destruir la vista.
    override fun onDestroyView() {
        super.onDestroyView()
        binding.viewPager.adapter = null
        _binding = null
    }
}
