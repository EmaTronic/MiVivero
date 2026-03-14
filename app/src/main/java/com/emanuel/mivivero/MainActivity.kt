package com.emanuel.mivivero

import android.content.Intent
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

        // 👇 manejar login si la app se abrió desde el email
        handleEmailLink(intent)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Toolbar
        setSupportActionBar(binding.topAppBar)

        // NavController
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.navHost) as NavHostFragment

        val navController = navHostFragment.navController

        binding.bottomNav.setupWithNavController(navController)

        // botón usuario
        binding.btnUsuario.setOnClickListener {

            navController.navigate(
                R.id.registroUsuarioFragment,
                null,
                androidx.navigation.NavOptions.Builder()
                    .setLaunchSingleTop(true)
                    .build()
            )
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

    // 👇 se ejecuta si la app ya estaba abierta
    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        handleEmailLink(intent)
    }

    private fun handleEmailLink(intent: Intent?) {

        val auth = FirebaseAuth.getInstance()
        val intentData = intent?.data ?: return

        if (!auth.isSignInWithEmailLink(intentData.toString())) return

        val email = getSharedPreferences("auth", MODE_PRIVATE)
            .getString("email", null) ?: return

        auth.signInWithEmailLink(email, intentData.toString())
            .addOnCompleteListener { task ->

                if (task.isSuccessful) {

                    Toast.makeText(
                        this,
                        "Sesión iniciada",
                        Toast.LENGTH_LONG
                    ).show()

                    intent.data = null   // evita repetir login

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