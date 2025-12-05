package com.example.tarea1.viewmodels

import com.example.tarea1.R // Para acceder a los recursos
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.tarea1.recycler.Keyboard

// El ViewModel NO debe tener ninguna referencia directa a la VISTA (Fragment/Activity).
// Solo maneja datos y lógica de negocio.
class ListViewModel : ViewModel() { // Herencia: extiende el ciclo de vida más allá de la Vista

    // ----------------------------------------------------------------------
    // 1. LIVE DATA
    // ----------------------------------------------------------------------

    // Variable privada: Contiene la lista de teclados. Es MutableLiveData para que el ViewModel
    // pueda cambiar su valor interno cuando se necesite
    private val _keyboardList = MutableLiveData<List<Keyboard>>()

    // Variable pública: Es LiveData (Inmutable). Esta es la "ventana" que el Fragment ve.
    // Permite que la VISTA solo LEA los cambios, NO ESCRIBA, garantizando la encapsulación.
    val keyboardList: LiveData<List<Keyboard>> get() = _keyboardList

    // LiveData para el evento de audio
    // Uso MutableLiveData<Unit> como SEÑAL. 'Unit' significa que no envío ningún dato,
    // solo notifico que un evento (el cambio de favorito) ha ocurrido.
    private val _playAudioEvent = MutableLiveData<Unit>()

    // La VISTA observa esta señal para disparar el sonido.
    // Esto separa la lógica de cambiar estado del efecto
    val playAudioEvent: LiveData<Unit> get() = _playAudioEvent

    // Inicializador: Se ejecuta inmediatamente cuando el ViewModel es creado por primera vez.
    init {
        loadKeyboards() // Llama a la función para poblar la lista inicial.
    }

    // ----------------------------------------------------------------------
    // 2. Carga Inicial
    // ----------------------------------------------------------------------

    // Función privada: Solo el ViewModel, usa esto para cargar datos.
    private fun loadKeyboards() {
        // 🚨 Lista de Teclados (Objetos Keyboard)
        val initialList = listOf<Keyboard>(
            Keyboard(
                title = "GMMK Pro (75%)",
                description = "Este teclado modular de alta gama presenta un diseño barebones con un layout compacto del 75%. Utiliza el sistema de montaje Gasket para una sensación de escritura suave y un sonido profundo. Es ideal para entusiastas que buscan una personalización avanzada, permitiendo intercambiar interruptores y keycaps fácilmente. Su carcasa de aluminio CNC le otorga durabilidad.",
                imagenId = R.drawable.gmmkpro75,
                fav = false
            ),
            Keyboard(
                title = "Keychron Q1 Pro (75%)",
                description = "Un teclado inalámbrico que mantiene el popular layout del 75% con dial giratorio, perfecto para la productividad. Construido con una robusta carcasa de aluminio CNC, soporta conectividad tri-modo (Bluetooth, 2.4Ghz y cable) y es compatible con macOS y Windows. Su montaje Gasket ofrece una experiencia de escritura flexible y cómoda y de alta fidelidad.",
                imagenId = R.drawable.keychronq1pro75,
                fav = false
            ),
            Keyboard(
                title = "MonsGeek M1W (75%)",
                description = "Representa una excelente relación calidad-precio dentro de los teclados personalizables. Cuenta con un layout del 75%, hot-swappable, y ofrece una experiencia de escritura premium gracias a sus keycaps de PBT. Viene ensamblado con los switches AKKO V3 Pro, conocidos por su sonido satisfactorio y rendimiento táctil rápido, siendo una gran opción para principiantes y entusiastas.",
                imagenId = R.drawable.monsgeekm1w,
                fav = false
            ),
            Keyboard(
                title = "NuPhy Air75 V2 (75%)",
                description = "Un teclado mecánico inalámbrico de perfil bajo (low profile) ideal para el escritorio o para llevar. Combina un diseño delgado con un layout compacto del 75%. Ofrece conectividad triple (Bluetooth 5.0, 2.4Ghz y cable) y es compatible con Windows, macOS, Android y iOS. Sus switches de bajo perfil están diseñados para una sensación táctil rápida y precisa.",
                imagenId = R.drawable.nuphyair75v2,
                fav = false
            ),
            Keyboard(
                title = "Akko 3098B (96%)",
                description = "Teclado de tamaño completo compacto (96%). Ofrece conectividad triple (USB-C, Bluetooth 5.0 y 2.4Ghz), switches Akko CS Jelly Pink, y keycaps PBT de doble disparo. Ideal para quien necesita el teclado numérico pero quiere un tamaño ligeramente más compacto.",
                imagenId = R.drawable.akko3098b,
                fav = false
            )
        )

        // Publica la lista inicial en el LiveData. (SOLO UNA VEZ DENTRO DE LA FUNCIÓN)
        _keyboardList.value = initialList
    }


    // ----------------------------------------------------------------------
    // 3. Alternar Favorito (toggleFavorite)
    // ----------------------------------------------------------------------

    // Función pública: La VISTA llama a esta función para PEDIR un cambio en el estado.
    fun toggleFavorite(keyboardTitle: String) {
        // 1. Obtener la lista actual (y hacer una copia mutable)
        val currentList = _keyboardList.value?.toMutableList() ?: return

        // 2. Encontrar la posición (índice) del teclado que la VISTA indicó que cambiara.
        val index = currentList.indexOfFirst { it.title == keyboardTitle }

        // Si lo encontré:
        if (index != -1) {
            // 3. Usamos 'copy()' para crear una COPIA inmutable del objeto Keyboard,
            // pero con el valor 'fav' invertido (!currentList[index].fav).
            // Esto garantiza que estamos manipulando el estado de forma inmutable y segura.
            val updatedKeyboard = currentList[index].copy(fav = !currentList[index].fav)

            // Reemplazamos el objeto antiguo por la copia actualizada en la lista mutable.
            currentList[index] = updatedKeyboard

            // 4. Asignamos la lista modificada al LiveData.
            // Esto dispara automáticamente el 'observe()' en ListFragment y FavFragment
            _keyboardList.value = currentList

            // Dispara el evento de audio
            // Asignamos 'Unit' al LiveData de evento.
            // El ViewModel NO hace el sonido, solo da la señal de que la acción fue exitosa.
            _playAudioEvent.value = Unit
        }
    }
}