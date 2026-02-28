package com.emanuel.mivivero.ui.home

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.emanuel.mivivero.R
import com.emanuel.mivivero.ui.auth.LoginActivity

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
            tvUser.text = "UID: ${currentUser.uid}"
        }

        btnLogout.setOnClickListener {
            auth.signOut()

            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    override fun onStart() {
        super.onStart()

        if (auth.currentUser == null) {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}