package com.example.tarea1.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tarea1.firebase.ServiceLocator
import com.example.tarea1.firebase.keyboards.KeyboardRepository
import com.example.tarea1.models.Keyboard
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ListViewModel(
    private val keyboardRepository: KeyboardRepository = ServiceLocator.keyboardRepository
) : ViewModel() {

    private val _keyboardList = MutableStateFlow<List<Keyboard>>(emptyList())
    val keyboardList: StateFlow<List<Keyboard>> = _keyboardList.asStateFlow()

    // Esto lo usa la vista para enseñar/ocultar progress bar.
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // Evento puntual para sonido de favorito.
    private val _playAudioEvent = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val playAudioEvent: SharedFlow<Unit> = _playAudioEvent.asSharedFlow()

    // Evento puntual para errores.
    private val _errorEvent = MutableSharedFlow<ErrorType>(extraBufferCapacity = 1)
    val errorEvent: SharedFlow<ErrorType> = _errorEvent.asSharedFlow()

    fun refreshKeyboards() {
        viewModelScope.launch {
            _isLoading.value = true

            runCatching {
                keyboardRepository.getKeyboards()
            }.onSuccess { keyboards ->
                _keyboardList.value = keyboards
            }.onFailure {
                _errorEvent.tryEmit(ErrorType.Load)
            }.also {
                _isLoading.value = false
            }
        }
    }

    fun addKeyboard(title: String, description: String, fav: Boolean) {
        viewModelScope.launch {
            runCatching {
                keyboardRepository.addKeyboard(title = title, description = description, fav = fav)
            }.onSuccess {
                // Recargo lista para que se vea el teclado recién creado.
                refreshKeyboards()
            }.onFailure {
                _errorEvent.tryEmit(ErrorType.Add)
            }
        }
    }

    fun toggleFavorite(keyboardId: String) {
        val currentKeyboard = _keyboardList.value.firstOrNull { it.id == keyboardId } ?: return
        val newFavValue = !currentKeyboard.fav

        viewModelScope.launch {
            runCatching {
                keyboardRepository.setKeyboardFavorite(keyboardId, newFavValue)
            }.onSuccess {
                // Refresco para que lista y favoritos se actualicen al momento.
                refreshKeyboards()
                _playAudioEvent.tryEmit(Unit)
            }.onFailure {
                _errorEvent.tryEmit(ErrorType.UpdateFavorite)
            }
        }
    }

    enum class ErrorType {
        Load,
        Add,
        UpdateFavorite
    }
}
