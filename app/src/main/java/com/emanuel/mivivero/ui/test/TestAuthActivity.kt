package com.emanuel.mivivero.ui.test

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.emanuel.mivivero.R
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

class TestAuthActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_test_auth)

        auth = FirebaseAuth.getInstance()

        val email = findViewById<EditText>(R.id.etEmail)
        val password = findViewById<EditText>(R.id.etPassword)
        val button = findViewById<Button>(R.id.btnRegister)

        button.setOnClickListener {



            val emailText = email.text.toString()
            val passText = password.text.toString()


            Log.d("AUTH", "Botón presionado")

            if (emailText.isBlank() || passText.isBlank()) {
                Log.d("AUTH", "Campos vacíos")
                return@setOnClickListener
            }

            auth.createUserWithEmailAndPassword(emailText, passText)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val uid = auth.currentUser?.uid

                        val db = FirebaseFirestore.getInstance()

                        val usuario = hashMapOf(
                            "rol" to "user",
                            "bloqueado" to false,
                            "fechaRegistro" to FieldValue.serverTimestamp()
                        )

                        uid?.let {
                            db.collection("usuarios")
                                .document(it)
                                .set(usuario)
                                .addOnSuccessListener {
                                    Log.d("AUTH", "Usuario guardado en Firestore")
                                    Toast.makeText(this, "Usuario registrado correctamente", Toast.LENGTH_SHORT).show()
                                }
                                .addOnFailureListener {
                                    Log.e("AUTH", "Error guardando usuario")
                                    Toast.makeText(this, "Error al guardar en Firestore", Toast.LENGTH_LONG).show()
                                }
                        }



                        Log.d("AUTH", "Usuario creado")
                    } else {
                        Log.e("AUTH", "Error: ${task.exception?.message}")
                    }
                }
        }
    }
}