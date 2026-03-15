package com.emanuel.mivivero.ui.auth

import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.emanuel.mivivero.R
import com.emanuel.mivivero.databinding.FragmentAuthBinding
import com.google.firebase.auth.FirebaseAuth

class AuthFragment : Fragment(R.layout.fragment_auth) {

    private var _binding: FragmentAuthBinding? = null
    private val binding get() = _binding!!

    private lateinit var auth: FirebaseAuth

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        _binding = FragmentAuthBinding.bind(view)
        auth = FirebaseAuth.getInstance()

        binding.btnEnviarLink.setOnClickListener {

            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                binding.etEmail.error = "Correo inválido"
                return@setOnClickListener
            }

            if (password.length < 6) {
                binding.etPassword.error = "Mínimo 6 caracteres"
                return@setOnClickListener
            }

            registrarUsuario(email, password)
        }
    }

    private fun registrarUsuario(email: String, password: String) {

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->

                if (task.isSuccessful) {

                    auth.currentUser?.sendEmailVerification()

                    Toast.makeText(
                        requireContext(),
                        "Cuenta creada. Revisa tu correo para verificar.",
                        Toast.LENGTH_LONG
                    ).show()

                } else {

                    Toast.makeText(
                        requireContext(),
                        task.exception?.message ?: "Error registrando usuario",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}