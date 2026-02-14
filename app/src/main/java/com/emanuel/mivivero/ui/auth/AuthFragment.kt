package com.emanuel.mivivero.ui.auth

import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.emanuel.mivivero.R
import com.emanuel.mivivero.databinding.FragmentAuthBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class AuthFragment : Fragment(R.layout.fragment_auth) {

    private var _binding: FragmentAuthBinding? = null
    private val binding get() = _binding!!

    private lateinit var auth: FirebaseAuth

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentAuthBinding.bind(view)

        auth = Firebase.auth

        binding.btnEnviarLink.setOnClickListener {

            val email = binding.etEmail.text.toString().trim()

            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                binding.etEmail.error = "Correo inválido"
                return@setOnClickListener
            }

            enviarLink(email)
        }
    }

    private fun enviarLink(email: String) {

        val actionCodeSettings =
            com.google.firebase.auth.ActionCodeSettings.newBuilder()
                .setUrl("https://mivivero-ematroniq.web.app")

                .setHandleCodeInApp(true)
                .setAndroidPackageName(
                    requireContext().packageName,
                    true,
                    null
                )
                .build()

        auth.sendSignInLinkToEmail(email, actionCodeSettings)
            .addOnCompleteListener { task ->

                if (task.isSuccessful) {

                    requireActivity()
                        .getSharedPreferences("auth", 0)
                        .edit()
                        .putString("email", email)
                        .apply()

                    Toast.makeText(
                        requireContext(),
                        "Link enviado al correo",
                        Toast.LENGTH_LONG
                    ).show()

                } else {
                    Toast.makeText(
                        requireContext(),
                        task.exception?.message ?: "Error desconocido",
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
