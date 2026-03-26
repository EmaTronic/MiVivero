package com.emanuel.mivivero.ui.auth

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.emanuel.mivivero.R
import com.emanuel.mivivero.databinding.FragmentVerificarEmailBinding
import com.google.firebase.auth.FirebaseAuth

class VerificarEmailFragment : Fragment(R.layout.fragment_verificar_email) {

    private var _binding: FragmentVerificarEmailBinding? = null
    private val binding get() = _binding!!

    private lateinit var auth: FirebaseAuth

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        _binding = FragmentVerificarEmailBinding.bind(view)
        auth = FirebaseAuth.getInstance()

        val user = auth.currentUser

        binding.tvEmail.text = user?.email ?: "Sin email"

        // 🔁 BOTÓN: YA VERIFIQUÉ
        binding.btnVerificar.setOnClickListener {

            val currentUser = auth.currentUser ?: return@setOnClickListener

            currentUser.reload().addOnSuccessListener {

                if (currentUser.isEmailVerified) {

                    Toast.makeText(
                        requireContext(),
                        "Correo verificado ✔",
                        Toast.LENGTH_SHORT
                    ).show()

                    findNavController().navigate(
                        R.id.comunidadFragment,
                        null,
                        androidx.navigation.NavOptions.Builder()
                            .setPopUpTo(R.id.nav_graph, true)
                            .build()
                    )

                } else {

                    Toast.makeText(
                        requireContext(),
                        "Aún no verificaste el correo",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }

        // 📩 BOTÓN: REENVIAR EMAIL
        binding.btnReenviar.setOnClickListener {

            user?.sendEmailVerification()

            Toast.makeText(
                requireContext(),
                "Correo reenviado",
                Toast.LENGTH_SHORT
            ).show()
        }

        // 🚪 OPCIONAL: cancelar (logout)
        binding.btnCancelar.setOnClickListener {

            auth.signOut()

            findNavController().navigate(R.id.loginFragment) {
                popUpTo(R.id.nav_graph) {
                    inclusive = true
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}