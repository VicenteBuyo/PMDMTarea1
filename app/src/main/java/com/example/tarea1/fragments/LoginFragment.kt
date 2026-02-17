package com.example.tarea1.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.example.tarea1.R
import com.example.tarea1.databinding.FragmentLoginBinding
import com.example.tarea1.firebase.users.UserUiState
import com.example.tarea1.viewmodels.users.AuthViewModel
import com.example.tarea1.viewmodels.users.AuthViewModelFactory
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    // Esto evita repetir navegación o mensajes si el flow vuelve a emitir el último estado.
    private var loginHandled = false

    // AuthViewModel con su factory para que entre con repos ya montado.
    private val authViewModel: AuthViewModel by viewModels {
        AuthViewModelFactory()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupInputListeners()
        setupButtonListeners()
        setupViewModelObservers()
    }

    private fun setupInputListeners() {
        // Cada vez que se escribe en usuario, actualizo el valor en el VM.
        binding.tilUsername.editText?.doOnTextChanged { text, _, _, _ ->
            authViewModel.username = text.toString()
        }

        // Igual para password.
        binding.tilPassword.editText?.doOnTextChanged { text, _, _, _ ->
            authViewModel.password = text.toString()
        }
    }

    private fun setupButtonListeners() {
        binding.btnLogin.setOnClickListener {
            // Llama al login (auth contra Firebase).
            authViewModel.performLogin()
        }

        binding.tvCreateAccount.setOnClickListener {
            // Salto a pantalla de registro.
            findNavController().navigate(R.id.action_login_to_register)
        }

        binding.btnGoogleLogin.setOnClickListener {
            // Este botón no está implementado; aprovecho para dejar acceso a reset de password.
            Snackbar.make(
                binding.root,
                getString(R.string.mensaje_funcionalidad_no_implementada),
                Snackbar.LENGTH_LONG
            ).setAction(getString(R.string.btn_recuperar_password)) {
                authViewModel.resetPassword()
            }.show()
        }
    }

    private fun setupViewModelObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {

                launch {
                    // Habilita/deshabilita botón según validación del formulario.
                    authViewModel.isLoginButtonEnabled.collect { isEnabled ->
                        binding.btnLogin.isEnabled = isEnabled
                    }
                }

                launch {
                    // Estado global de auth: loading, ok o error.
                    authViewModel.userUiState.collect { state ->
                        when (state) {
                            is UserUiState.Idle -> Unit

                            is UserUiState.Loading -> {
                                binding.btnLogin.isEnabled = false
                            }

                            is UserUiState.Authenticated -> {
                                if (loginHandled) return@collect
                                loginHandled = true

                                Toast.makeText(
                                    context,
                                    getString(R.string.mensaje_login_exitoso),
                                    Toast.LENGTH_SHORT
                                ).show()

                                findNavController().navigate(R.id.action_login_to_tabFragment)
                            }

                            is UserUiState.Error -> {
                                // Si viene mensaje vacío, uso texto genérico.
                                val errorMessage = state.message.ifBlank {
                                    getString(R.string.mensaje_credenciales_incorrectas)
                                }

                                Snackbar.make(binding.root, errorMessage, Snackbar.LENGTH_LONG).show()

                                // Limpio campos para que el usuario vuelva a intentar.
                                binding.tilUsername.editText?.text?.clear()
                                binding.tilPassword.editText?.text?.clear()
                                loginHandled = false
                            }
                        }
                    }
                }

                launch {
                    // Resultado del reset de password.
                    authViewModel.passwordResetResult.collect { result ->
                        val messageRes = when (result) {
                            AuthViewModel.PasswordResetResult.Sent -> R.string.msg_reset_password_sent
                            AuthViewModel.PasswordResetResult.EmptyEmail -> R.string.msg_reset_password_empty_email
                            AuthViewModel.PasswordResetResult.Failed -> R.string.msg_reset_password_failed
                        }

                        Toast.makeText(context, getString(messageRes), Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
