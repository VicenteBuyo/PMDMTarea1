package com.example.tarea1.viewpager

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.tarea1.fragments.FavFragment
import com.example.tarea1.fragments.ListFragment

// Adapter del ViewPager2 para cargar las dos pestañas.
class ViewPagerAdapter(fa: FragmentActivity) : FragmentStateAdapter(fa) {

    // Número fijo de páginas.
    override fun getItemCount(): Int = 2

    // Devuelve qué fragment toca en cada posición.
    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> ListFragment()
            1 -> FavFragment()
            else -> ListFragment()
        }
    }
}
