package com.emanuel.mivivero.ui.home

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.emanuel.mivivero.R

class HomeActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        auth = FirebaseAuth.getInstance()

        val tvUser = findViewById<TextView>(R.id.tvUser)
        val btnLogout = findViewById<Button>(R.id.btnLogout)

        val currentUser = auth.currentUser

        if (currentUser != null) {
            tvUser.text = "Usuario: ${currentUser.email}"
        } else {
            tvUser.text = "No autenticado"
        }

        btnLogout.setOnClickListener {

            auth.signOut()

            Toast.makeText(
                this,
                "Sesión cerrada",
                Toast.LENGTH_SHORT
            ).show()

            finish()
        }
    }
}