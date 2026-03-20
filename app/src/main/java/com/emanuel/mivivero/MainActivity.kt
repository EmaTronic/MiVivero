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

    private var primeraCarga = true

    private var authListener: FirebaseAuth.AuthStateListener? = null


    private var ultimoLoginTimestamp: Long = 0

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


    override fun onStart() {
        super.onStart()

        authListener = FirebaseAuth.AuthStateListener { auth ->

            val user = auth.currentUser

            // limpiar listener anterior
            sessionListener?.remove()

            if (user == null) return@AuthStateListener

            val uid = user.uid

            val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()

            primeraCarga = true

            sessionListener = db.collection("usuarios")
                .document(uid)
                .addSnapshotListener { doc, _ ->

                    if (doc == null || !doc.exists()) return@addSnapshotListener

                    val remoteSessionId = doc.getString("sessionId")


                    android.util.Log.d("SESSION_DEBUG", "REMOTE: $remoteSessionId")

                    val prefs = getSharedPreferences("session", MODE_PRIVATE)
                    val localSessionId = prefs.getString("sessionId", null)

                    val loginTime = prefs.getLong("loginTime", 0)
                    val ahora = System.currentTimeMillis()

                    if (ahora - loginTime < 2000) {
                        return@addSnapshotListener
                    }

                    if (localSessionId != null && localSessionId != remoteSessionId) {
                        android.util.Log.d("SESSION_DEBUG", "LOCAL: $localSessionId")
                        if (primeraCarga) {
                            primeraCarga = false
                            return@addSnapshotListener
                        }

                        if (localSessionId != null && localSessionId != remoteSessionId) {

                            val navHostFragment =
                                supportFragmentManager.findFragmentById(R.id.navHost) as NavHostFragment

                            val navController = navHostFragment.navController

                            navController.navigate(R.id.sesionCerradaFragment)

                            FirebaseAuth.getInstance().signOut()
                        }
                    }
                }
        }

        FirebaseAuth.getInstance().addAuthStateListener(authListener!!)
    }

    override fun onStop() {
        super.onStop()
        sessionListener?.remove()
        authListener?.let { FirebaseAuth.getInstance().removeAuthStateListener(it) }
    }


    override fun onDestroy() {
        super.onDestroy()
        sessionListener?.remove()
    }
}