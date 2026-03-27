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


        //FirebaseAuth.getInstance().signOut()

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

                AlertDialog.Builder(this)
                    .setTitle("Usuario")
                    .setMessage("¿Qué querés hacer?")
                    .setPositiveButton("Iniciar sesión") { _, _ ->
                        navController.navigate(R.id.loginFragment)
                    }
                    .setNegativeButton("Registrarse") { _, _ ->
                        navController.navigate(R.id.registroUsuarioFragment)
                    }
                    .setNeutralButton("Cancelar", null)
                    .show()

            } else {

                AlertDialog.Builder(this)
                    .setTitle("Usuario")
                    .setMessage("Sesión iniciada como:\n${usuarioActual.email}")
                    .setPositiveButton("Editar perfil") { _, _ ->
                        navController.navigate(R.id.registroUsuarioFragment)
                    }
                    .setNegativeButton("Cerrar sesión") { _, _ ->
                        FirebaseAuth.getInstance().signOut()
                    }
                    .setNeutralButton("Cancelar", null)
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

                            // 🔴 SOLUCIÓN
                            if (!user.isEmailVerified) return@addSnapshotListener

                            val remoteSessionId = doc.getString("sessionId")

                            val prefs = getSharedPreferences("session", MODE_PRIVATE)
                            val localSessionId = prefs.getString("sessionId", null)

                            val loginTime = prefs.getLong("loginTime", 0)
                            val ahora = System.currentTimeMillis()

                            if (ahora - loginTime < 2000) {
                                return@addSnapshotListener
                            }

                            if (localSessionId != null && localSessionId != remoteSessionId) {

                                if (primeraCarga) {
                                    primeraCarga = false
                                    return@addSnapshotListener
                                }

                                val navHostFragment =
                                    supportFragmentManager.findFragmentById(R.id.navHost) as NavHostFragment

                                val navController = navHostFragment.navController

                                navController.navigate(R.id.sesionCerradaFragment)

                                FirebaseAuth.getInstance().signOut()
                            }
                        }

        }

        FirebaseAuth.getInstance().addAuthStateListener(authListener!!)
    }

    override fun onResume() {
        super.onResume()

        val user = FirebaseAuth.getInstance().currentUser ?: return

        if (!user.isEmailVerified) {

            binding.root.postDelayed({

                val navController = (supportFragmentManager
                    .findFragmentById(R.id.navHost) as NavHostFragment)
                    .navController

                if (navController.currentDestination?.id != R.id.verificarEmailFragment) {

                    navController.navigate(R.id.verificarEmailFragment)
                }

            }, 300) // 🔴 clave: delay real
        }
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