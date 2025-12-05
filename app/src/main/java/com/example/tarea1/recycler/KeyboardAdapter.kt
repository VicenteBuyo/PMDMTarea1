package com.example.tarea1.recycler

import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.View // Necesario para controlar la visibilidad (View.VISIBLE)
import androidx.recyclerview.widget.RecyclerView // La clase base de la que heredamos
import com.example.tarea1.R // Para acceder a los recursos
import com.example.tarea1.databinding.ItemLayoutBinding // El binding para acceder a los elementos de cada fila

// ----------------------------------------------------------------------
// 1. CONSTRUCTOR
// ----------------------------------------------------------------------

// Un Adapter actúa como un CONTRATO entre los datos y la lista visible.
class KeyboardAdapter (
    // Los Datos: La lista que vamos a dibujar. Es 'private var' para poder actualizarla después.
    private var keyboardList: List<Keyboard>,

    // La función que la VISTA (Adapter) llama al VIEWMODEL para pedir un cambio.
    // Recibe un String (el título del teclado) y no devuelve nada (Unit).
    private val onFavoriteClick: (String) -> Unit,

    // El Interruptor Lógico: Nos dice si estamos en la vista de Favoritos (true) o en la Lista Principal (false).
    private val isFavView: Boolean = false
) : RecyclerView.Adapter<KeyboardAdapter.KeyboardViewHolder>() { // Hereda de RecyclerView.Adapter, obligándonos a implementar 3 métodos.

    // ----------------------------------------------------------------------
    // 2. FUNCIÓN submitList (Actualización de la Lista)
    // ----------------------------------------------------------------------

    // Permite que el LiveData del ViewModel envíe una nueva lista filtrada o actualizada.
    fun submitList(newList: List<Keyboard>) {
        this.keyboardList = newList // Reemplazo la lista vieja por la nueva.
        notifyDataSetChanged() // Le dice al RecyclerView que se redibuje por completo porque hay cambios
    }

    // Métodos obligatorios del Adapter

    // 1. onCreateViewHolder: Se llama cuando el RecyclerView necesita crear una NUEVA fila visible
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): KeyboardViewHolder {
        // Inflamos el layout 'item_layout.xml' usando View Binding.
        val binding = ItemLayoutBinding.inflate(
            LayoutInflater.from(parent.context), // Usamos el Context para saber qué inflater usar, quien es su parent
            parent,
            false // NO adjuntar a la raíz inmediatamente.
        )
        return KeyboardViewHolder(binding) // Devolvemos nuestro contenedor de la fila.
    }

    // 2. onBindViewHolder: Se llama cada vez que una fila DEBE MOSTRAR nuevos datos
    override fun onBindViewHolder(holder: KeyboardViewHolder, position: Int) {
        val keyboard = keyboardList[position] // Obtengo el objeto Keyboard en la posición actual.
        holder.bind(keyboard) // Le paso ese objeto a mi ViewHolder para que dibuje el contenido.
    }

    // 3. getItemCount: Le dice al RecyclerView cuántos ítems tiene que dibujar.
    override fun getItemCount(): Int = keyboardList.size

    // ----------------------------------------------------------------------
    // 3. VIEWHOLDER
    // ----------------------------------------------------------------------

    // El ViewHolder: Contiene las referencias a los elementos de UNA sola fila y la lógica para llenarlos.
    inner class KeyboardViewHolder(private val binding: ItemLayoutBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(keyboard: Keyboard) {
            // Llenado de datos
            binding.tvTeclado.text = keyboard.title
            binding.tvDescripcion.text = keyboard.description
            binding.ivTeclado.setImageResource(keyboard.imagenId)

            // El icono de favorito SIEMPRE es visible: Se asegura de que la estrella esté en el layout.
            binding.ivFavorito.visibility = View.VISIBLE

            // (El Interruptor isFavView)
            if (isFavView) {
                // ESTAMOS EN FAVFRAGMENT: El botón de estrella está deshabilitado.

                // 1. Deshabilitar visualmente: Se ve gris para indicar que no se puede interactuar.
                binding.ivFavorito.alpha = 0.5f

                // 2. QUITAR EL LISTENER: Ponerlo a 'null' deshabilita el clic en la estrella.
                binding.ivFavorito.setOnClickListener(null)

                // 3. LÓGICA DE FAVORITO: Asignamos la acción de 'quitar favorito' al clic en TODA la fila (binding.cl).
                // Al pulsar el ítem, se llama al callback para que el ViewModel cambie 'fav' a false.
                binding.cl.setOnClickListener {
                    onFavoriteClick(keyboard.title) // Esto QUITA el teclado de la lista de favoritos.
                }
            } else {
                // ESTAMOS EN LISTFRAGMENT: El botón de estrella está ACTIVO.

                binding.ivFavorito.alpha = 1.0f // Opacidad normal (activo)
                updateFavoriteIcon(keyboard.fav) // Usa la función de abajo para mostrar la estrella llena o vacía.

                // 4.  botón de favorito activo
                binding.ivFavorito.setOnClickListener {
                    // Si pulso la estrella, llamo al callback para que el ViewModel ALTERNE el estado.
                    onFavoriteClick(keyboard.title)
                }

                // En ListFragment, el clic en la fila completa solo se usa para navegar.
                binding.cl.setOnClickListener {
                    //TODO: Implementar lógica de navegación o detalle, más adelante si se pide
                }
            }
        }

        // Función auxiliar que cambia el icono de la estrella
        private fun updateFavoriteIcon(isFavorite: Boolean){
            binding.ivFavorito.setImageResource(
                // Si 'isFavorite' es true, usa mi imagen 'star_on'.
                if (isFavorite) R.drawable.star_on
                // Si es false, usa el icono de estrella vacía por defecto de Android.
                else android.R.drawable.star_off
            )
        }
    }
}