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
        // Configurar los OBSERVERS (Observadores de LiveData).
        // La VISTA (Fragment) reacciona automáticamente a los cambios en el VIEWMODEL.

        // Observamos el LiveData que me dice si el botón de login debe estar activo o no.
        authViewModel.isLoginButtonEnabled.observe(viewLifecycleOwner) { isEnabled ->
            // Si el ViewModel me dice 'true', activo el botón; si me dice 'false', lo deshabilito.
            // Esto previene que el usuario pulse login si faltan credenciales (ej: un campo vacío).
            binding.btnLogin.isEnabled = isEnabled
        }

        // Observo el resultado final de la validación del login que el ViewModel me da.
        // El resultado es un booleano (true para éxito, false para error).
        authViewModel.loginResult.observe(viewLifecycleOwner) { isSuccess ->
            // Si el valor es null, salimos
            if (isSuccess == null) return@observe

            if (isSuccess) { // Si el boolean es true (Login Exitoso)...

                // Muestra un mensaje de éxito.
                Toast.makeText(
                    context,
                    getString(R.string.mensaje_login_exitoso),
                    Toast.LENGTH_SHORT
                ).show()

                // NUEVO:
                // Usamos el 'findNavController()' para obtener el controlador de navegación.
                // Uso el ID de la acción que definí en mi archivo de navegación (nav_graph.xml).
                // Esto lleva del LoginFragment al TabFragment, iniciando la siguiente pantalla.
                findNavController().navigate(R.id.action_login_to_tabFragment)

            } else { // Si el boolean arroja false (Login Fallido)...

                // Muestra un mensaje de error más largo para que el usuario lo vea bien.
                Toast.makeText(
                    context,
                    getString(R.string.mensaje_credenciales_incorrectas),
                    Toast.LENGTH_LONG
                ).show()

                // Lógica de limpieza: Borramos lo que haya escrito en los campos para obligar
                // al usuario a intentarlo de nuevo.
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