package com.example.tarea1

import androidx.activity.OnBackPressedCallback
import androidx.core.view.GravityCompat
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
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
import com.example.tarea1.firebase.ServiceLocator

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    private lateinit var appBarConfiguration: AppBarConfiguration

    // Lo uso para alternar entre ascendente y descendente al pulsar ordenar.
    private var ordenAscendente = true

    private companion object {
        // Claves separadas para lista y favoritos.
        // Si compartimos clave, se pisan entre sí.
        const val FILTER_REQUEST_LIST = "filter_request_list"
        const val FILTER_REQUEST_FAV = "filter_request_fav"
        const val SORT_REQUEST_LIST = "sort_request_list"
        const val SORT_REQUEST_FAV = "sort_request_fav"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        // Aquí saco el NavController desde el NavHostFragment principal.
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        // Destinos top-level: aquí quiero menú hamburguesa en vez de flecha atrás.
        appBarConfiguration = AppBarConfiguration(
            setOf(R.id.tabFragment, R.id.contactFragment, R.id.preferencesFragment),
            binding.drawerLayout
        )

        setupActionBarWithNavController(navController, appBarConfiguration)
        binding.bottomNavigation.setupWithNavController(navController)
        binding.navigationView.setupWithNavController(navController)

        setupFab()
        setupDrawerMenu()
        setupDestinationChanges()
        setupBackBehavior()
    }

    private fun setupDrawerMenu() {
        binding.navigationView.setNavigationItemSelectedListener { item ->
            if (item.itemId == R.id.action_logout) {
                // Cierro sesión en auth y mando al login limpiando pila.
                ServiceLocator.authRepository.signOut()
                updateDrawerHeaderEmail()
                binding.drawerLayout.closeDrawers()

                val navOptions = androidx.navigation.NavOptions.Builder()
                    .setPopUpTo(R.id.tabFragment, true)
                    .build()

                navController.navigate(R.id.loginFragment, null, navOptions)
                return@setNavigationItemSelectedListener true
            }

            // Para el resto de items, que navegue NavigationUI.
            val handled = NavigationUI.onNavDestinationSelected(item, navController)
            if (handled) binding.drawerLayout.closeDrawers()
            handled
        }
    }

    private fun setupDestinationChanges() {
        navController.addOnDestinationChangedListener { _, destination, _ ->
            val estamosEnAuth =
                destination.id == R.id.loginFragment || destination.id == R.id.registerFragment

            // En login/register escondo barra inferior y bloqueo drawer.
            binding.bottomNavigation.isVisible = !estamosEnAuth
            binding.drawerLayout.setDrawerLockMode(
                if (estamosEnAuth) DrawerLayout.LOCK_MODE_LOCKED_CLOSED
                else DrawerLayout.LOCK_MODE_UNLOCKED
            )

            // El FAB solo tiene sentido en la pantalla de tabs.
            binding.fab.isVisible = destination.id == R.id.tabFragment

            // Recalculo toolbar menu (buscar/ordenar) en cada cambio.
            invalidateOptionsMenu()
            updateDrawerHeaderEmail()
        }
    }

    private fun setupBackBehavior() {
        // Si el drawer está abierto y pulso atrás, primero lo cierro.
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    binding.drawerLayout.closeDrawer(GravityCompat.START)
                } else {
                    isEnabled = false
                    onBackPressedDispatcher.onBackPressed()
                }
            }
        })
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Solo muestro lupa/orden en la pantalla principal de tabs.
        val destinoActual = navController.currentDestination?.id
        if (destinoActual == R.id.tabFragment) {
            menuInflater.inflate(R.menu.toolbar_menu, menu)

            val searchItem = menu.findItem(R.id.action_search)
            val searchView = searchItem.actionView as SearchView
            searchView.queryHint = getString(R.string.search_by_name)

            // Cada cambio de texto se manda a lista y favoritos.
            searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?): Boolean = true

                override fun onQueryTextChange(newText: String?): Boolean {
                    val query = newText ?: ""
                    supportFragmentManager.setFragmentResult(
                        FILTER_REQUEST_LIST,
                        bundleOf("query" to query)
                    )
                    supportFragmentManager.setFragmentResult(
                        FILTER_REQUEST_FAV,
                        bundleOf("query" to query)
                    )
                    return true
                }
            })
        }

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_sort -> {
                // Voy alternando el boolean y lo mando a ambos fragments.
                ordenAscendente = !ordenAscendente

                supportFragmentManager.setFragmentResult(
                    SORT_REQUEST_LIST,
                    bundleOf("asc" to ordenAscendente)
                )
                supportFragmentManager.setFragmentResult(
                    SORT_REQUEST_FAV,
                    bundleOf("asc" to ordenAscendente)
                )
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        // Dejo que NavigationUI gestione hamburguesa/flecha.
        return NavigationUI.navigateUp(navController, appBarConfiguration) || super.onSupportNavigateUp()
    }

    private fun setupFab() {
        // El FAB vive en Activity, pero la acción real la ejecuta ListFragment.
        binding.fab.setOnClickListener {
            if (navController.currentDestination?.id == R.id.tabFragment) {
                supportFragmentManager.setFragmentResult("add_keyboard_request", Bundle.EMPTY)
            }
        }
    }

    private fun updateDrawerHeaderEmail() {
        // Pinto en el header el correo del usuario actual.
        val headerView = binding.navigationView.getHeaderView(0)
        val tvUserName = headerView.findViewById<TextView>(R.id.tvUserName)
        val currentEmail = ServiceLocator.authRepository.currentUser()?.email
        tvUserName.text = currentEmail ?: getString(R.string.user_name)
    }
}
