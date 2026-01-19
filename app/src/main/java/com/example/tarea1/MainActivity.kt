package com.example.tarea1

import androidx.activity.OnBackPressedCallback
import androidx.core.view.GravityCompat
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.core.os.bundleOf
import androidx.appcompat.widget.SearchView
import com.example.tarea1.databinding.ActivityMainBinding
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen

// Punto de entrada de la aplicación (Single Activity Architecture)
// Inicializa el entorno y carga el contenedor que gestiona la navegación (NavHostFragment)
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    // NavController
    private lateinit var navController: NavController

    // Configuración de la AppBar para que sepa cuándo mostrar hamburguesa o flecha atrás
    private lateinit var appBarConfiguration: AppBarConfiguration

    // Estado para ir alternando el orden (asc/desc) cuando pulsemos el icono de ordenar
    private var ordenAscendente = true

    override fun onCreate(savedInstanceState: Bundle?) {
        // 1. Iniciamos la pantalla de bienvenida (Splash Screen) antes de nada.
        installSplashScreen()

        super.onCreate(savedInstanceState)

        // 2. Ahora inflamos el diseño (creamos la vista en memoria)
        binding = ActivityMainBinding.inflate(layoutInflater)

        // 3. Establecemos la vista en la pantalla
        setContentView(binding.root)

        // 6. La toolbar la usaremos como ActionBar (para título + hamburguesa/flecha atrás)
        setSupportActionBar(binding.toolbar)

        // 7. Sacamos el NavController desde el NavHostFragment del layout
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        // 8. Estos son los destinos “principales” (los del bottom/drawer)
        // Aquí queremos hamburguesa (y no flecha atrás) cuando estemos en ellos.
        appBarConfiguration = AppBarConfiguration(
            setOf(R.id.tabFragment, R.id.contactFragment, R.id.preferencesFragment),
            binding.drawerLayout
        )

        // 9. Conectamos toolbar y navegación (gestiona hamburguesa/flecha y el título)
        setupActionBarWithNavController(navController, appBarConfiguration)

        // 10. Conectamos el menú inferior con el nav graph
        binding.bottomNavigation.setupWithNavController(navController)

        // 11. Conectamos el drawer con el nav graph
        binding.navigationView.setupWithNavController(navController)

        // 12. Logout es especial (no es un destino del nav_graph), así que lo manejamos a mano
        // Asignamos un listener al menú lateral (NavigationView)
        binding.navigationView.setNavigationItemSelectedListener(
            object : com.google.android.material.navigation.NavigationView.OnNavigationItemSelectedListener {

                // Este méto do se ejecuta cada vez que el usuario pulsa un ítem del drawer
                override fun onNavigationItemSelected(item: MenuItem): Boolean {

                    // Comprobamos si el ítem pulsado es el de cerrar sesión
                    if (item.itemId == R.id.action_logout) {

                        // Cerramos el drawer para que no se quede abierto
                        binding.drawerLayout.closeDrawers()

                        // Creamos unas opciones de navegación
                        // popUpTo sirve para limpiar la pila de fragments
                        // En este caso borramos tod o desde TabFragment hacia atrás
                        val navOptions = androidx.navigation.NavOptions.Builder()
                            .setPopUpTo(R.id.tabFragment, true)
                            .build()

                        // Navegamos manualmente al LoginFragment
                        // Así evitamos que el usuario pueda volver atrás a la app sin loguearse
                        navController.navigate(R.id.loginFragment, null, navOptions)

                        // Devolvemos true porque ya hemos gestionado el click
                        return true
                    }

                    // Si NO es logout, dejamos que NavigationUI se encargue
                    // Esto cambia automáticamente el fragment según el menú
                    val handled = NavigationUI.onNavDestinationSelected(item, navController)

                    // Si la navegación ha sido correcta, cerramos el drawer
                    if (handled) {
                        binding.drawerLayout.closeDrawers()
                    }

                    // Devolvemos si el evento ha sido manejado o no
                    return handled
                }
            }
        )



        // 13. En login/register NO queremos ver bottom ni abrir drawer
        navController.addOnDestinationChangedListener(
            object : NavController.OnDestinationChangedListener {

                override fun onDestinationChanged(
                    controller: NavController,
                    destination: androidx.navigation.NavDestination,
                    arguments: Bundle?
                ) {

                    // Si estamos en login o register, estamos en pantallas de autenticación
                    val estamosEnAuth =
                        destination.id == R.id.loginFragment || destination.id == R.id.registerFragment

                    // BottomNavigation solo se ve si NO estamos en auth
                    binding.bottomNavigation.isVisible = !estamosEnAuth

                    // El botón flotante solo debe estar visible desde ListaFragment (en tu caso TabFragment)
                    binding.fab.isVisible = destination.id == R.id.tabFragment

                    // Si estamos en login/register, bloqueamos el drawer para que no se pueda abrir
                    if (estamosEnAuth) {
                        binding.drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
                    } else {
                        binding.drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)
                    }

                    // Cada vez que cambiamos de pantalla, recalculamos si toca mostrar lupa/ordenar en la toolbar
                    invalidateOptionsMenu()
                }
            }
        )


        // Si el drawer está abierto y el usuario pulsa atrás, primero lo cerramos
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    binding.drawerLayout.closeDrawer(GravityCompat.START)
                } else {
                    // Si no está abierto, dejamos que el back funcione normal
                    isEnabled = false
                    onBackPressedDispatcher.onBackPressed()
                }
            }
        })


    }

    // Menú superior: SOLO aparece en la pantalla de Lista (TabFragment)
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val destinoActual = navController.currentDestination?.id

        if (destinoActual == R.id.tabFragment) {
            menuInflater.inflate(R.menu.toolbar_menu, menu)

            // Configuramos la lupa para ir filtrando por nombre mientras escribimos
            val searchItem = menu.findItem(R.id.action_search)
            val searchView = searchItem.actionView as SearchView
            searchView.queryHint = getString(R.string.search_by_name)

            searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?): Boolean {
                    return true
                }

                override fun onQueryTextChange(newText: String?): Boolean {
                    // Mandamos el texto a quien lo necesite (ListFragment lo recibirá)
                    supportFragmentManager.setFragmentResult(
                        "filter_request",
                        bundleOf("query" to (newText ?: ""))
                    )
                    return true
                }
            })
        }

        return true
    }

    // Clicks del menú superior
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_sort -> {
                // Alternamos el orden (asc/desc) y se lo comunicamos a la lista
                ordenAscendente = !ordenAscendente

                supportFragmentManager.setFragmentResult(
                    "sort_request",
                    bundleOf("asc" to ordenAscendente)
                )
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    // Esto es lo que hace que la hamburguesa/flecha funcionen correctamente con NavigationUI
    override fun onSupportNavigateUp(): Boolean {
        return NavigationUI.navigateUp(navController, appBarConfiguration) || super.onSupportNavigateUp()
    }
}
