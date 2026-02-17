package com.example.tarea1.recycler

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.tarea1.R
import com.example.tarea1.databinding.ItemLayoutBinding
import com.example.tarea1.models.Keyboard

// Adapter del RecyclerView de teclados.
class KeyboardAdapter(
    private var keyboardList: List<Keyboard>,
    private val onFavoriteClick: OnFavoriteClickListener,
    private val isFavView: Boolean = false
) : RecyclerView.Adapter<KeyboardAdapter.KeyboardViewHolder>() {

    // Callback para notificar click en estrella al Fragment.
    interface OnFavoriteClickListener {
        fun onFavoriteClick(keyboardId: String)
    }

    // Reemplaza la lista en memoria y repinta.
    fun submitList(newList: List<Keyboard>) {
        keyboardList = newList
        notifyDataSetChanged()
    }

    // Crea un ViewHolder nuevo inflando item_layout.
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): KeyboardViewHolder {
        val binding = ItemLayoutBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return KeyboardViewHolder(binding)
    }

    // Enlaza datos del teclado con la fila.
    override fun onBindViewHolder(holder: KeyboardViewHolder, position: Int) {
        holder.bind(keyboardList[position])
    }

    // Total de filas.
    override fun getItemCount(): Int = keyboardList.size

    inner class KeyboardViewHolder(private val binding: ItemLayoutBinding) :
        RecyclerView.ViewHolder(binding.root) {

        // Pinta una fila y configura su comportamiento.
        fun bind(keyboard: Keyboard) {
            binding.tvTeclado.text = keyboard.title
            binding.tvDescripcion.text = keyboard.description
            binding.ivTeclado.setImageResource(resolveKeyboardImage(keyboard.title))
            binding.ivFavorito.visibility = View.VISIBLE

            updateFavoriteIcon(keyboard.fav)

            // En favoritos dejo la estrella bloqueada para no desmarcar desde ahí.
            binding.ivFavorito.alpha = if (isFavView) 0.85f else 1f
            if (isFavView) {
                binding.ivFavorito.setOnClickListener(null)
                binding.cl.setOnClickListener(null)
            } else {
                binding.ivFavorito.setOnClickListener {
                    onFavoriteClick.onFavoriteClick(keyboard.id)
                }
                binding.cl.setOnClickListener(null)
            }
        }

        // Cambia icono según si está marcado o no.
        private fun updateFavoriteIcon(isFavorite: Boolean) {
            binding.ivFavorito.setImageResource(
                if (isFavorite) R.drawable.star_on else R.drawable.star_off
            )
        }

        // Traduce el título a una imagen local del drawable.
        private fun resolveKeyboardImage(title: String): Int {
            val normalized = title.lowercase()
            return when {
                normalized.contains("gmmk") -> R.drawable.gmmkpro75
                normalized.contains("keychron") -> R.drawable.keychronq1pro75
                normalized.contains("nuphy") -> R.drawable.nuphyair75v2
                normalized.contains("akko") -> R.drawable.akko3098b
                normalized.contains("monsgeek") -> R.drawable.monsgeekm1w
                normalized.contains("higround") -> R.drawable.higroundbasecamp65
                else -> R.drawable.splashscreen
            }
        }
    }
}
