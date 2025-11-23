package com.example.tarea1.fragments
// Define el paquete donde reside este archivo. Ayuda a organizar el código.

// --- IMPORTACIONES ---
// Importamos las herramientas necesarias del SDK de Android y librerías externas.
import android.app.DatePickerDialog // Clase para mostrar el calendario emergente (Requisito 3.b).
import android.os.Bundle // Contenedor de datos para pasar información entre componentes.
import android.view.LayoutInflater // Convierte el XML en objetos visuales (Vistas).
import android.view.View // Clase base para cualquier elemento visual en pantalla.
import android.view.ViewGroup // Contenedor de vistas (el padre del layout).
import android.widget.Toast // Mensajes emergentes temporales (feedback al usuario).
import androidx.core.widget.doAfterTextChanged // Extensión Kotlin para simplificar listeners de texto.
import androidx.fragment.app.Fragment // Clase base para crear pantallas modulares.
import androidx.fragment.app.viewModels // Delegado para inicializar ViewModels fácilmente.
import androidx.navigation.fragment.findNavController // Para navegar entre pantallas (NavGraph).
import com.example.tarea1.R // Referencia a todos los recursos (layouts, strings, ids).
import com.example.tarea1.databinding.FragmentRegisterBinding // Clase generada automáticamente para acceder al XML sin errores.
import com.example.tarea1.viewmodels.NewUserViewModel // Tu lógica de negocio específica para registro.
import java.util.Calendar // Utilidad de Java para manejar fechas y horas.

// Definición de la clase RegisterFragment que hereda de Fragment.
// Actúa como el controlador de la vista "Crear Cuenta".
class RegisterFragment : Fragment() {

    // --- VIEW BINDING (Vinculación de Vistas) ---
    // Variable mutable privada para almacenar la referencia al diseño (binding).
    // Es nullable (?) porque el binding solo existe mientras la vista está viva.
    private var _binding: FragmentRegisterBinding? = null

    // Propiedad de acceso público (solo lectura).
    // Usa 'get() = _binding!!' para asegurar que no es nulo cuando lo llamamos.
    // Si lo llamáramos cuando la vista está destruida, la app se cerraría (crash),
    // pero lo gestionamos correctamente en onDestroyView.
    private val binding get() = _binding!!

    // --- VIEWMODEL (Lógica de Negocio) ---
    // Instanciamos el NewUserViewModel usando el delegado 'by viewModels()'.
    // Esto conecta automáticamente este Fragment con su cerebro
    private val viewModel: NewUserViewModel by viewModels()

    // --- CICLO DE VIDA: CREACIÓN DE VISTA ---
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflamos el archivo 'fragment_register.xml' y guardamos la referencia en _binding.
        _binding = FragmentRegisterBinding.inflate(inflater, container, false)

        // Devolvemos la raíz del diseño (el ConstraintLayout principal) para que Android lo pinte.
        return binding.root
    }

    // --- CICLO DE VIDA: VISTA CREADA ---
    // Se llama inmediatamente después de onCreateView
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Llamamos a nuestros métodos de configuración (funciones auxiliares creadas abajo)
        // para mantener el código organizado y limpio.
        setupInputListeners() // 1. Configurar qué pasa cuando escribes.
        setupDatePicker()     // 2. Configurar el calendario
        setupObservers()      // 3. Configurar la escucha del ViewModel
        setupButtons()        // 4. Configurar el clic del botón final.
    }

    // --- CONFIGURACIÓN DE LISTENERS DE TEXTO ---
    private fun setupInputListeners() {
        // Configuración para el campo USUARIO
        // 'doAfterTextChanged': Se ejecuta cada vez que el usuario añade/borra una letra.
        binding.etRegUsername.doAfterTextChanged {
            // 1. Actualizamos la variable 'username' en el ViewModel con lo escrito.
            viewModel.username = it.toString()
            // 2. Pedimos al ViewModel que verifique si ahora los datos son válidos.
            viewModel.validateInputData()
        }

        // Configuración para el campo CONTRASEÑA
        binding.etRegPassword.doAfterTextChanged {
            // 1. Actualizamos la contraseña en el ViewModel.
            viewModel.password = it.toString()
            // 2. Limpiamos el mensaje de error rojo (si lo hubiera)
            // Si el usuario corrige, no queremos seguir mostrándole el error antiguo.
            binding.tilRegPassword.error = null
            // 3. Validamos si habilitamos el botón.
            viewModel.validateInputData()
        }

        // Configuración para el campo CONFIRMAR CONTRASEÑA
        binding.etRegConfirmPassword.doAfterTextChanged {
            // 1. Actualizamos la confirmación en el ViewModel.
            viewModel.confirmPassword = it.toString()
            // 2. Limpiamos el error visual.
            binding.tilRegConfirmPassword.error = null
            // 3. Validamos si habilitamos el botón.
            viewModel.validateInputData()
        }
    }

    // --- CONFIGURACIÓN DEL DATE PICKER  ---
    private fun setupDatePicker() {
        // Añadimos un listener de clic al campo de texto de la fecha.
        binding.etRegDob.setOnClickListener {

            // Obtenemos la fecha actual del sistema para inicializar el calendario hoy.
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            // Instanciamos el diálogo nativo de Android para seleccionar fecha.
            val datePickerDialog = DatePickerDialog(
                requireContext(), // Contexto necesario para crear ventanas emergentes.
                { _, selectedYear, selectedMonth, selectedDay ->
                    // --- CALLBACK: Esto se ejecuta cuando el usuario pulsa "Aceptar" en el calendario ---

                    // Formateamos la fecha
                    val formattedDate = "$selectedDay/${selectedMonth + 1}/$selectedYear"

                    // Mostramos la fecha en el campo de texto.
                    binding.etRegDob.setText(formattedDate)

                    // Guardamos la fecha en el ViewModel (por si la necesitamos luego).
                    viewModel.birthDate = formattedDate
                },
                year, month, day // Pasamos la fecha inicial (hoy) al diálogo.
            )
            // Mostramos el diálogo en pantalla.
            datePickerDialog.show()
        }
    }

    // --- CONFIGURACIÓN DE OBSERVADORES (LiveData) ---
    // Aquí conectamos la Vista con el ViewModel. La Vista "reacciona" a los cambios de datos.
    private fun setupObservers() {

        // 1. Observar si habilitamos el botón (Gris vs Color)
        // 'viewLifecycleOwner': La observación solo dura mientras la vista existe.
        viewModel.isButtonEnabled.observe(viewLifecycleOwner) { isEnabled ->
            // Si isEnabled es true, el botón se activa. Si es false, se desactiva (gris).
            binding.btnCreateAccount.isEnabled = isEnabled
        }

        // 2. Observar error de contraseñas (Texto rojo)
        viewModel.passwordMismatchError.observe(viewLifecycleOwner) { hasError ->
            if (hasError) {
                // Si hay error, obtenemos el mensaje desde strings.xml
                val errorMsg = getString(R.string.error_contrasenas_no_coinciden)
                // Y lo mostramos en los dos campos de contraseña (TextInputLayouts).
                binding.tilRegPassword.error = errorMsg
                binding.tilRegConfirmPassword.error = errorMsg
            } else {
                // Si no hay error, nos aseguramos de que no se muestre nada rojo.
                binding.tilRegPassword.error = null
                binding.tilRegConfirmPassword.error = null
            }
        }

        // 3. Observar éxito del registro - Navegación
        viewModel.registrationSuccess.observe(viewLifecycleOwner) { success ->
            if (success) {
                // Mostramos feedback al usuario.
                Toast.makeText(context, "Cuenta creada con éxito", Toast.LENGTH_SHORT).show()

                // REQUISITO: "Acceder a la pantalla de login si ambas coinciden".
                // 'popBackStack()': Saca el fragment actual de la pila de navegación.
                // Como el LoginFragment está justo debajo en la pila, volvemos a él.
                findNavController().popBackStack()
            }
        }
    }

    // --- CONFIGURACIÓN DE BOTONES ---
    private fun setupButtons() {
        // Listener para el botón principal "Crear Cuenta".
        binding.btnCreateAccount.setOnClickListener {
            // Delegamos la lógica al ViewModel. Él decidirá si las contraseñas coinciden.
            viewModel.onRegisterClicked()
        }
    }

    // --- CICLO DE VIDA: DESTRUCCIÓN DE VISTA ---
    // Se llama cuando el usuario navega fuera del fragmento y la vista se destruye.
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}