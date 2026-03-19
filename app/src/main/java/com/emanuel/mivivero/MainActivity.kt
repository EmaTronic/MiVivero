package com.emanuel.mivivero

import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.emanuel.mivivero.databinding.ActivityMainBinding
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {

    private var sessionListener: com.google.firebase.firestore.ListenerRegistration? = null
    private lateinit var binding: ActivityMainBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // inicializar FirebaseAuth
        auth = FirebaseAuth.getInstance()

        val user = FirebaseAuth.getInstance().currentUser

        if (user != null) {

            val uid = user.uid

            sessionListener = com.google.firebase.firestore.FirebaseFirestore.getInstance()
                .collection("usuarios")
                .document(uid)
                .addSnapshotListener { doc, _ ->

                    android.util.Log.d("SESSION_DEBUG", "🔥 LISTENER DISPARADO")

                    if (doc == null || !doc.exists()) {
                        android.util.Log.d("SESSION_DEBUG", "❌ DOC NULL O NO EXISTE")
                        return@addSnapshotListener
                    }

                  val remoteSessionId = doc.getString("sessionId")

                    val prefs = getSharedPreferences("session", MODE_PRIVATE)
                    val localSessionId = prefs.getString("sessionId", null)

                        // 👇 SOLO el que NO coincide muestra aviso
                    if (localSessionId != null && localSessionId != remoteSessionId) {

                        android.widget.Toast.makeText(
                            this,
                            "Tu sesión fue iniciada en otro dispositivo",
                            android.widget.Toast.LENGTH_LONG
                        ).show()

                        FirebaseAuth.getInstance().signOut()

                        val navHostFragment =
                            supportFragmentManager.findFragmentById(R.id.navHost) as NavHostFragment

                        val navController = navHostFragment.navController

                        navController.navigate(R.id.loginFragment)
                    }



                    if (localSessionId != null && localSessionId != remoteSessionId) {


                        android.widget.Toast.makeText(
                            this,
                            "Tu sesión fue iniciada en otro dispositivo",
                            android.widget.Toast.LENGTH_LONG
                        ).show()

                        val navHostFragment =
                            supportFragmentManager.findFragmentById(R.id.navHost) as NavHostFragment

                        val navController = navHostFragment.navController

                        navController.navigate(R.id.loginFragment)

                        FirebaseAuth.getInstance().signOut()
                    }
                }
        }
        // Toolbar
        setSupportActionBar(binding.topAppBar)

        // NavController
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.navHost) as NavHostFragment

        val navController = navHostFragment.navController

        // Bottom navigation
        binding.bottomNav.setOnItemSelectedListener { item ->

            when (item.itemId) {

                R.id.comunidadFragment -> {
                    navController.popBackStack(R.id.comunidadFragment, false)
                    navController.navigate(R.id.comunidadFragment)
                    true
                }

                else -> {
                    navController.navigate(item.itemId)
                    true
                }
            }
        }

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

    override fun onDestroy() {
        super.onDestroy()
        sessionListener?.remove()
    }
}