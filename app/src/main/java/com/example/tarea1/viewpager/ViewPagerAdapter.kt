package com.example.tarea1.viewpager

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter // El adaptador oficial para Fragments en ViewPager2
import com.example.tarea1.fragments.FavFragment // El contenido de la segunda pestaña
import com.example.tarea1.fragments.ListFragment // El contenido de la primera pestaña

// Hereda de FragmentStateAdapter, que es la forma de gestionar fragments en ViewPager2
// Le pasamos 'fa: FragmentActivity' al constructor. Esta Activity es la dueña de estos Fragments.
// Esto permite al adaptador gestionar la memoria y el ciclo de vida de los Fragments de manera eficiente.
class ViewPagerAdapter(fa: FragmentActivity) : FragmentStateAdapter(fa) {

    // ----------------------------------------------------------------------
    // 1. Método Obligatorio: getItemCount()
    // ----------------------------------------------------------------------

    // Le dice al ViewPager2 y al TabLayoutMediator cuántas "páginas" o pestañas existen.
    override fun getItemCount(): Int = 2 // Tenemos dos pestañas: 0 (Lista) y 1 (Favoritos).

    // ----------------------------------------------------------------------
    // 2. Método Obligatorio: createFragment(position)
    // ----------------------------------------------------------------------

    // Se llama cuando el ViewPager2 necesita crear un Fragment para una posición específica.
    override fun createFragment(position: Int): Fragment {
        // Usamos una expresión 'when' para decidir qué Fragment instanciar basándonos en el índice (position).
        return when (position) {
            0 -> ListFragment()  // Primera pestaña (índice 0): Crea un Fragmento de la Lista.
            1 -> FavFragment()   // Segunda pestaña (índice 1): Crea un Fragmento de Favoritos.
            else -> ListFragment() // Caso por defecto (nunca debería ocurrir
        }
    }
}