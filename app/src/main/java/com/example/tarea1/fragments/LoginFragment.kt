package com.example.tarea1.fragments // Declaración del paquete del proyecto.


// Importación de las clases estándar de Android necesarias para un Fragment y mensajes Toast.
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
// Importación de la extensión moderna de Kotlin para escuchar cambios de texto.
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
// DELEGADO CLAVE: Herramienta de Jetpack para inicializar ViewModels.
import androidx.fragment.app.viewModels
// Clase para acceder al controlador de navegación (NavHostFragment).
import androidx.navigation.fragment.findNavController
import com.example.tarea1.R // Archivo de recursos (strings, layouts, etc.).
// Clase generada por View Binding para acceder a las vistas del XML.
import com.example.tarea1.databinding.FragmentLoginBinding
// Clase del ViewModel.
import com.example.tarea1.viewmodels.AuthViewModel
// Componente de Material Design para mostrar mensajes SnackBar.
import com.google.android.material.snackbar.Snackbar

// Definición de la clase, que debe heredar de Fragment de AndroidX.
class LoginFragment : Fragment() {

    // ----------------------------------------------------------------------
    // 1. Vinculación de Vistas (View Binding)
    // ----------------------------------------------------------------------

    // Variable mutable para almacenar la instancia del binding
    // Es nullable para permitir la limpieza mediante onDestroyView()
    private var _binding: FragmentLoginBinding? = null

    // Getter que proporciona un acceso seguro y no-null al binding, ya que nos aseguramos de
    // haberle asignado un valor en onCreateView
    private val binding get() = _binding!!



    // ----------------------------------------------------------------------
    // 2. INSTANCIACIÓN DEL VIEWMODEL
    // ----------------------------------------------------------------------

    // El ViewModel se utiliza para la persistencia de datos tras la destrucción y
    // recreación de la View (Fragments)

    // 'by viewModels()' usa delegación de propiedades para inicializar el ViewModel.
    // Se hace así para garantizar que AuthViewModel se crea una osla vez
    // Instancia asociada al ciclo de vida del Fragment y sobrevive a rotaciones.
    private val authViewModel: AuthViewModel by viewModels()



    // ----------------------------------------------------------------------
    // 3. Ciclo de Vida: Creación de la Vista
    // ----------------------------------------------------------------------

    // Métod para inflar el layout XML e inicializar el View Binding.
    // Crea y devuelve la jerarquía de vistas que usará el Fragment
    override fun onCreateView(
        //Definimos el LayoutInflater, y el ViewGroup, en nuestro caso el NavHostFragment que
        //hemos definido en el activity_main.xml

        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle? //Bundle para guardar pequeños datos del Fragment al destruír y recrearlo
    ): View { // Infla el layout y asigna la instancia al _binding
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        // Devuelve la vista raíz (el ConstraintLayout) al sistema.
        return binding.root
    }

    // Métod llamado inmediatamente después de que la vista ha sido creada
    // Aquí llamamos a los métodos para interactuar con las vistas (listeners y observadores)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Llamadas de listeners
        setupInputListeners()
        setupButtonListeners()
        setupViewModelObservers()

        // Inicializar el estado del botón
        // Llamamos a su métod (Observer)
        // Se le llama aquí para asegurar que el botón está
        // deshabilitado o habilitado inmediatamente después de cargar.
        authViewModel.checkCredentialsValidity()
    }

    // ----------------------------------------------------------------------
    // 4. Listeners de Entrada
    // Lo que sucede cuando el usuario modifica el texto
    // ----------------------------------------------------------------------

    private fun setupInputListeners() {

        // Accede al EditText interno del TextInputLayout (tilUsername)
        // 'doOnTextChanged' es el listener
        binding.tilUsername.editText?.doOnTextChanged { text, _, _, _ ->
            // Actualiza la propiedad 'username' del ViewModel.
            // esta línea dispara automáticamente la lógica de validación (checkCredentialsValidity).
            authViewModel.username = text.toString()
        }

        // Listener para la contraseña
        // Extensión de KTX para la clase EditText
        // Se dispara cada vez que el texto cambia
        // Si editText existe, llama al métod en el interior
        binding.tilPassword.editText?.doOnTextChanged { text, _, _, _ ->
            // Actualiza la propiedad 'password' del ViewModel.
            authViewModel.password = text.toString()
        }
    }

    // ----------------------------------------------------------------------
    // 5. Listeners de Clic (Eventos)
    // Lo que sucede cuando el usuario interactúa con los botones
    // ----------------------------------------------------------------------

    private fun setupButtonListeners() {

        // Botón INICIAR SESIÓN
        binding.btnLogin.setOnClickListener {
            // Llama al métod de login que contiene la lógica "admin"/"1234" en el ViewModel.
            authViewModel.performLogin()
        }

        // Botón CREAR CUENTA
        binding.tvCreateAccount.setOnClickListener {
            // findNavController(): Obtiene el controlador de navegación asociado a este Fragment.
            // .navigate(): Ejecuta la "Action" definida en nav_graph.xml, yendo al RegisterFragment.
            findNavController().navigate(R.id.action_login_to_register)
        }

        // Botón INICIAR SESIÓN EN GOOGLE
        binding.btnGoogleLogin.setOnClickListener {
            // Crea un SnackBar con el mensaje de funcionalidad no implementada.
            Snackbar.make(
                binding.root, // Se muestra en la vista raíz del Fragment.
                getString(R.string.mensaje_funcionalidad_no_implementada), // Mensaje desde strings.xml
                Snackbar.LENGTH_LONG // Duración larga

            // .setAction() añade un botón de acción al SnackBar.
            // No añadimos Listener como segundo parámetro, por lo que aparecerá el botón de CERRAR
            ).setAction(getString(R.string.btn_cerrar_snackbar)) {
            }.show() //Muestra el snackbar
        }
    }

    // ----------------------------------------------------------------------
    // 6. Observadores (LiveData)
    // Sólo "observa" si la View está en un estado de vida activo
    // ----------------------------------------------------------------------

    private fun setupViewModelObservers() {

        // OBSERVACIÓN 1: Estado del Botón
        // 'observe(viewLifecycleOwner)': Suscribe el Fragmento a los cambios de LiveData.
        // viewLifecycleOwner garantiza que la observación solo ocurre mientras la vista del Fragment esté activa.
        authViewModel.isLoginButtonEnabled.observe(viewLifecycleOwner) { isEnabled ->
            // Actualiza 'isEnabled' del botón a true cuando el authviewmodel determina que
            // tanto usuario como contraseña tienen contenido
            binding.btnLogin.isEnabled = isEnabled
        }

        // OBSERVACIÓN 2: Resultado del Login
        // Se activa si el usuario pulsa Iniciar sesión y el authViewModel.performLogin() ha
        // verificado las credenciales
        // Pasamos viewLifecycleOwner como parámetro para que la observación se detenga cuando
        // la vista del Fragment se destruye, para evitar fugas de memoria
        authViewModel.loginResult.observe(viewLifecycleOwner) { isSuccess ->
            if (isSuccess == null) return@observe

            if (isSuccess) { // Si el boolean es true...
                // Muestra un mensaje
                Toast.makeText(
                    context,
                    getString(R.string.mensaje_login_exitoso),
                    Toast.LENGTH_SHORT
                ).show()
            } else { // Si el boolean arroja false...
                // Muestra un mensaje de error
                Toast.makeText(
                    context,
                    getString(R.string.mensaje_credenciales_incorrectas),
                    Toast.LENGTH_LONG
                ).show()

                // limpiamos los campos
                binding.etUsername.text?.clear()
                binding.etPassword.text?.clear()
            }
        }
    }

    // ----------------------------------------------------------------------
    // 7. Limpieza (Anti-Memory Leak)
    // ----------------------------------------------------------------------

    // Limpia la referencia al View Binding.
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}