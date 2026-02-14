package com.emanuel.mivivero.ui.auth

import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.emanuel.mivivero.R
import com.emanuel.mivivero.data.db.entity.UsuarioEntity
import com.emanuel.mivivero.databinding.FragmentRegistroUsuarioBinding
import kotlinx.coroutines.launch

class RegistroUsuarioFragment :
    Fragment(R.layout.fragment_registro_usuario) {

    private var _binding: FragmentRegistroUsuarioBinding? = null
    private val binding get() = _binding!!

    private val viewModel: RegistroViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentRegistroUsuarioBinding.bind(view)

        viewLifecycleOwner.lifecycleScope.launch {
            val usuario = viewModel.obtenerUsuario()

            if (usuario != null) {
                binding.etNombreReal.setText(usuario.nombreReal)
                binding.etNick.setText(usuario.nick)
                binding.etNombreVivero.setText(usuario.nombreVivero)
                binding.etPais.setText(usuario.pais)
                binding.etProvincia.setText(usuario.provincia)
                binding.etCiudad.setText(usuario.ciudad)
                binding.etEmail.setText(usuario.email)

                binding.txtTituloRegistro.text = "Editar datos de usuario"
                binding.btnRegistrar.text = "Actualizar"
            }
        }

        binding.btnRegistrar.setOnClickListener {

            val nombre = binding.etNombreReal.text.toString().trim()
            val nick = binding.etNick.text.toString().trim()
            val vivero = binding.etNombreVivero.text.toString().trim()
            val pais = binding.etPais.text.toString().trim()
            val provincia = binding.etProvincia.text.toString().trim()
            val ciudad = binding.etCiudad.text.toString().trim()
            val email = binding.etEmail.text.toString().trim()

            if (
                nombre.isEmpty() ||
                nick.isEmpty() ||
                vivero.isEmpty() ||
                pais.isEmpty() ||
                provincia.isEmpty() ||
                ciudad.isEmpty() ||
                !Patterns.EMAIL_ADDRESS.matcher(email).matches()
            ) {
                Toast.makeText(
                    requireContext(),
                    "Complete todos los campos correctamente",
                    Toast.LENGTH_LONG
                ).show()
                return@setOnClickListener
            }

            viewLifecycleOwner.lifecycleScope.launch {

                val existente = viewModel.obtenerUsuario()

                val usuario = UsuarioEntity(
                    id = 1,
                    nombreReal = nombre,
                    nick = nick,
                    nombreVivero = vivero,
                    pais = pais,
                    provincia = provincia,
                    ciudad = ciudad,
                    email = email,
                    fechaRegistro = existente?.fechaRegistro
                        ?: System.currentTimeMillis()
                )

                if (existente == null) {
                    viewModel.insertarUsuario(usuario)
                } else {
                    viewModel.actualizarUsuario(usuario)
                }

                Toast.makeText(
                    requireContext(),
                    "Datos guardados",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
