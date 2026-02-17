package com.example.tarea1.fragments

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.example.tarea1.R
import com.example.tarea1.databinding.FragmentRegisterBinding
import com.example.tarea1.firebase.ServiceLocator
import com.example.tarea1.firebase.users.NewUserUiState
import com.example.tarea1.viewmodels.users.NewUserViewModel
import com.example.tarea1.viewmodels.users.NewUserViewModelFactory
import kotlinx.coroutines.launch
import java.util.Calendar

class RegisterFragment : Fragment() {

    private var _binding: FragmentRegisterBinding? = null
    private val binding get() = _binding!!

    // NewUserViewModel con dependencias de auth + users.
    private val viewModel: NewUserViewModel by viewModels {
        NewUserViewModelFactory(
            ServiceLocator.authRepository,
            ServiceLocator.userRepository
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegisterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupInputListeners()
        setupDatePicker()
        setupCollectors()
        setupButtons()
    }

    private fun setupInputListeners() {
        // Campo email/usuario.
        binding.etRegUsername.doAfterTextChanged {
            viewModel.username = it.toString()
            viewModel.validateInputData()
        }

        // Campo password.
        binding.etRegPassword.doAfterTextChanged {
            viewModel.password = it.toString()
            binding.tilRegPassword.error = null
            viewModel.validateInputData()
        }

        // Campo confirmar password.
        binding.etRegConfirmPassword.doAfterTextChanged {
            viewModel.confirmPassword = it.toString()
            binding.tilRegConfirmPassword.error = null
            viewModel.validateInputData()
        }
    }

    private fun setupDatePicker() {
        binding.etRegDob.setOnClickListener {
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            // Al elegir fecha la guardo en el input y en el ViewModel.
            val datePickerDialog = DatePickerDialog(
                requireContext(),
                { _, selectedYear, selectedMonth, selectedDay ->
                    val formattedDate = "$selectedDay/${selectedMonth + 1}/$selectedYear"
                    binding.etRegDob.setText(formattedDate)
                    viewModel.birthDate = formattedDate
                },
                year,
                month,
                day
            )

            datePickerDialog.show()
        }
    }

    private fun setupCollectors() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {

                launch {
                    // Activa/desactiva el botón crear cuenta.
                    viewModel.isButtonEnabled.collect { isEnabled ->
                        binding.btnCreateAccount.isEnabled = isEnabled
                    }
                }

                launch {
                    // Marca error visual cuando las dos contraseñas no coinciden.
                    viewModel.passwordMismatchError.collect { hasError ->
                        if (hasError) {
                            val errorMsg = getString(R.string.error_contrasenas_no_coinciden)
                            binding.tilRegPassword.error = errorMsg
                            binding.tilRegConfirmPassword.error = errorMsg
                        } else {
                            binding.tilRegPassword.error = null
                            binding.tilRegConfirmPassword.error = null
                        }
                    }
                }

                launch {
                    // Estado completo del alta.
                    viewModel.uiState.collect { state ->
                        when (state) {
                            is NewUserUiState.Idle -> Unit

                            is NewUserUiState.Loading -> {
                                // Bloqueo botón para evitar doble click.
                                binding.btnCreateAccount.isEnabled = false
                            }

                            is NewUserUiState.Created -> {
                                Toast.makeText(
                                    context,
                                    getString(R.string.mensaje_cuenta_creada),
                                    Toast.LENGTH_SHORT
                                ).show()

                                // Vuelvo a login tras crear la cuenta.
                                findNavController().popBackStack()
                            }

                            is NewUserUiState.Error -> {
                                val messageRes = when (state.type) {
                                    NewUserUiState.Error.Type.PasswordMismatch -> R.string.error_contrasenas_no_coinciden
                                    NewUserUiState.Error.Type.PasswordTooShort -> R.string.error_contrasena_minima
                                    NewUserUiState.Error.Type.EmailAlreadyExists -> R.string.error_email_ya_registrado
                                    NewUserUiState.Error.Type.Generic -> R.string.error_registro_generico
                                }

                                Toast.makeText(context, getString(messageRes), Toast.LENGTH_LONG).show()
                            }
                        }
                    }
                }
            }
        }
    }

    private fun setupButtons() {
        binding.btnCreateAccount.setOnClickListener {
            // Lógica de alta completa en ViewModel.
            viewModel.onRegisterClicked()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
