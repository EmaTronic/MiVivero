package com.emanuel.mivivero

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.emanuel.mivivero.databinding.ActivityMainBinding
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val auth = FirebaseAuth.getInstance()
        val intentData = intent?.data

        if (intentData != null && auth.isSignInWithEmailLink(intentData.toString())) {

            val email = getSharedPreferences("auth", MODE_PRIVATE)
                .getString("email", null)

            if (email != null) {

                auth.signInWithEmailLink(email, intentData.toString())
                    .addOnCompleteListener { task ->

                        if (task.isSuccessful) {

                            Toast.makeText(
                                this,
                                "Sesión iniciada",
                                Toast.LENGTH_LONG
                            ).show()

                        } else {

                            Toast.makeText(
                                this,
                                "Error autenticando",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
            }
        }




        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Configurar Toolbar como ActionBar
        setSupportActionBar(binding.topAppBar)

        // Obtener NavController
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.navHost) as NavHostFragment

        val navController = navHostFragment.navController

        // Conectar BottomNavigation
        binding.bottomNav.setupWithNavController(navController)

        // Click icono usuario
        binding.btnUsuario.setOnClickListener {

            navController.navigate(
                R.id.registroUsuarioFragment,
                null,
                androidx.navigation.NavOptions.Builder()
                    .setLaunchSingleTop(true)
                    .build()
            )
        }

        // Click logo (Acerca de)
        binding.imgLogoToolbar.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Mi Vivero")
                .setMessage("Versión 1.0\nGestión profesional de plantas.")
                .setPositiveButton("Cerrar", null)
                .show()
        }
    }
}