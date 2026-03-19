package com.emanuel.mivivero.ui.auth


import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.emanuel.mivivero.R
import com.emanuel.mivivero.databinding.FragmentLoginBinding
import com.google.firebase.auth.FirebaseAuth

class LoginFragment : Fragment(R.layout.fragment_login) {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    private lateinit var auth: FirebaseAuth

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        _binding = FragmentLoginBinding.bind(view)
        auth = FirebaseAuth.getInstance()

        binding.btnLogin.setOnClickListener {

            val email = binding.etEmail.text.toString()
            val pass = binding.etPassword.text.toString()

            if (email.isEmpty() || pass.isEmpty()) {
                Toast.makeText(requireContext(),"Completar datos",Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            auth.signInWithEmailAndPassword(email, pass)
                .addOnSuccessListener {

                    val user = FirebaseAuth.getInstance().currentUser ?: return@addOnSuccessListener
                    val uid = user.uid

                    val sessionId = System.currentTimeMillis().toString()

                    // 🔹 Firestore
                    com.google.firebase.firestore.FirebaseFirestore.getInstance()
                        .collection("usuarios")
                        .document(uid)
                        .set(
                            mapOf(
                                "sessionId" to sessionId
                            ),
                            com.google.firebase.firestore.SetOptions.merge()
                        )

                    // 🔹 Local
                    val prefs = requireContext().getSharedPreferences("session", 0)
                    prefs.edit().putString("sessionId", sessionId).apply()

                    Toast.makeText(
                        requireContext(),
                        "Sesión iniciada",
                        Toast.LENGTH_SHORT
                    ).show()

                    findNavController().popBackStack()
                }
                .addOnFailureListener {
                    Toast.makeText(
                        requireContext(),
                        "Email o contraseña incorrectos",
                        Toast.LENGTH_SHORT
                    ).show()
                }
        }

        binding.btnIrRegistro.setOnClickListener {

            findNavController().navigate(R.id.registroUsuarioFragment)

        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}