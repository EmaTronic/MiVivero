package com.emanuel.mivivero

import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.emanuel.mivivero.databinding.ActivityMainBinding
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // inicializar FirebaseAuth
        auth = FirebaseAuth.getInstance()

        // Toolbar
        setSupportActionBar(binding.topAppBar)

        // NavController
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.navHost) as NavHostFragment

        val navController = navHostFragment.navController

        // Bottom navigation
        binding.bottomNav.setupWithNavController(navController)

        // botón usuario
        binding.btnUsuario.setOnClickListener {

            val usuarioActual = auth.currentUser

            if (usuarioActual == null) {

                navController.navigate(R.id.loginFragment)

            } else {

                AlertDialog.Builder(this)
                    .setTitle("Usuario")
                    .setMessage("Sesión iniciada como:\n${usuarioActual.email}")
                    .setPositiveButton("Cerrar sesión") { _, _ ->
                        auth.signOut()
                    }
                    .setNegativeButton("Cancelar", null)
                    .show()
            }
        }
        // logo
        binding.imgLogoToolbar.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Mi Vivero")
                .setMessage("Versión 1.0\nGestión profesional de plantas.")
                .setPositiveButton("Cerrar", null)
                .show()
        }
    }
}