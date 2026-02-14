package com.emanuel.mivivero

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.emanuel.mivivero.databinding.ActivityMainBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)



        val auth = Firebase.auth

        if (auth.isSignInWithEmailLink(intent?.data.toString())) {

            val email = getSharedPreferences("auth", MODE_PRIVATE)
                .getString("email", null)

            if (email != null) {
                auth.signInWithEmailLink(email, intent?.data.toString())
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {

                            getSharedPreferences("auth", MODE_PRIVATE)
                                .edit()
                                .remove("email")
                                .apply()

                        }
                    }
            }
        }


        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}
