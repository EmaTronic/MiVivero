package com.emanuel.mivivero.ui.auth

import android.os.Bundle
import android.text.Editable
import android.util.Patterns
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.emanuel.mivivero.R
import com.emanuel.mivivero.data.db.entity.UsuarioEntity
import com.emanuel.mivivero.data.model.Root
import com.emanuel.mivivero.databinding.FragmentRegistroUsuarioBinding
import com.google.android.material.textfield.TextInputLayout
import com.google.gson.Gson
import kotlinx.coroutines.launch
import android.text.TextWatcher

class RegistroUsuarioFragment :
    Fragment(R.layout.fragment_registro_usuario) {

    private var _binding: FragmentRegistroUsuarioBinding? = null
    private val binding get() = _binding!!

    private val viewModel: RegistroViewModel by viewModels()

    private lateinit var rootData: Root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentRegistroUsuarioBinding.bind(view)




        limpiarErrorAlEscribir(binding.etNombreReal, binding.tilNombreReal)
        limpiarErrorAlEscribir(binding.etNick, binding.tilNick)
        limpiarErrorAlEscribir(binding.etNombreVivero, binding.tilNombreVivero)
        limpiarErrorAlEscribir(binding.spPais, binding.tilPais)
        limpiarErrorAlEscribir(binding.spProvincia, binding.tilProvincia)
        limpiarErrorAlEscribir(binding.spCiudad, binding.tilCiudad)
        binding.etEmail.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val email = s.toString()
                if (email.isBlank()) {
                    binding.tilEmail.error = null
                } else if (Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    binding.tilEmail.error = null
                }
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        cargarJson()
        configurarSpinners()

        // Cargar usuario si existe
        viewLifecycleOwner.lifecycleScope.launch {
            val usuario = viewModel.obtenerUsuario()

            if (usuario != null) {

                binding.txtTituloRegistro.text = "Editar datos de usuario"
                binding.btnRegistrar.text = "Actualizar"

                binding.etNombreReal.setText(usuario.nombreReal)
                binding.etNick.setText(usuario.nick)
                binding.etNombreVivero.setText(usuario.nombreVivero)
                binding.etEmail.setText(usuario.email)

                // Seleccionar valores guardados
                seleccionarUbicacion(usuario.pais, usuario.provincia, usuario.ciudad)
            }
        }

        binding.btnRegistrar.setOnClickListener {

            val nombreReal = binding.etNombreReal.text.toString().trim()
            val nick = binding.etNick.text.toString().trim()
            val vivero = binding.etNombreVivero.text.toString().trim()
            val pais = binding.spPais.text.toString().trim()
            val provincia = binding.spProvincia.text.toString().trim()
            val ciudad = binding.spCiudad.text.toString().trim()
            val email = binding.etEmail.text.toString().trim()

            var hayError = false

            fun validar(layout: TextInputLayout, valor: String) {
                if (valor.isBlank()) {
                    layout.error = "Campo obligatorio"
                    hayError = true
                } else {
                    layout.error = null
                }
            }

            validar(binding.tilNombreReal, nombreReal)
            validar(binding.tilNick, nick)
            validar(binding.tilNombreVivero, vivero)
            validar(binding.tilPais, pais)
            validar(binding.tilProvincia, provincia)
            validar(binding.tilCiudad, ciudad)

            // Validación email
            if (email.isBlank()) {
                binding.tilEmail.error = "Campo obligatorio"
                hayError = true
            } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                binding.tilEmail.error = "Correo inválido"
                hayError = true
            } else {
                binding.tilEmail.error = null
            }

            if (hayError) return@setOnClickListener

            val usuario = UsuarioEntity(
                nombreReal = nombreReal,
                nick = nick,
                nombreVivero = vivero,
                pais = pais,
                provincia = provincia,
                ciudad = ciudad,
                email = email,
                fechaRegistro = System.currentTimeMillis()
            )

            viewLifecycleOwner.lifecycleScope.launch {
                viewModel.guardarUsuario(usuario)

                Toast.makeText(
                    requireContext(),
                    "Usuario guardado correctamente",
                    Toast.LENGTH_SHORT
                ).show()

                findNavController().popBackStack()
            }
        }
    }


    private fun limpiarErrorAlEscribir(
        editText: EditText,
        layout: TextInputLayout
    ) {
        editText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if (!s.isNullOrBlank()) {
                    layout.error = null
                }
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }
    private fun cargarJson() {
        val jsonString = requireContext().assets
            .open("argentina_ordenado.json")
            .bufferedReader()
            .use { it.readText() }

        rootData = Gson().fromJson(jsonString, Root::class.java)
    }

    private fun configurarSpinners() {

        val paises = rootData.paises.map { it.nombre }

        val adapterPais = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_dropdown_item_1line,
            paises
        )

        binding.spPais.setAdapter(adapterPais)

        binding.spPais.setOnItemClickListener { _, _, position, _ ->

            val paisSeleccionado = rootData.paises[position]

            val provincias = paisSeleccionado.provincias.map { it.nombre }

            val adapterProvincia = ArrayAdapter(
                requireContext(),
                android.R.layout.simple_dropdown_item_1line,
                provincias
            )

            binding.spProvincia.setAdapter(adapterProvincia)
            binding.spProvincia.setText("", false)

            binding.spCiudad.setText("", false)
            binding.spCiudad.setAdapter(null)
        }

        binding.spProvincia.setOnItemClickListener { _, _, position, _ ->

            val paisNombre = binding.spPais.text.toString()

            val paisSeleccionado = rootData.paises
                .firstOrNull { it.nombre == paisNombre }
                ?: return@setOnItemClickListener

            val provinciaSeleccionada =
                paisSeleccionado.provincias[position]

            val ciudades = provinciaSeleccionada.ciudades

            val adapterCiudad = ArrayAdapter(
                requireContext(),
                android.R.layout.simple_dropdown_item_1line,
                ciudades
            )

            binding.spCiudad.setAdapter(adapterCiudad)
            binding.spCiudad.setText("", false)
        }
    }


        private fun seleccionarUbicacion(
    pais: String,
    provincia: String,
    ciudad: String
    ) {

        // 1️⃣ Setear país
        binding.spPais.setText(pais, false)

        val paisSeleccionado = rootData.paises
            .firstOrNull { it.nombre == pais }
            ?: return

        val provincias = paisSeleccionado.provincias.map { it.nombre }

        val adapterProvincia = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_dropdown_item_1line,
            provincias
        )

        binding.spProvincia.setAdapter(adapterProvincia)

        // 2️⃣ Setear provincia
        binding.spProvincia.setText(provincia, false)

        val provinciaSeleccionada = paisSeleccionado.provincias
            .firstOrNull { it.nombre == provincia }
            ?: return

        val ciudades = provinciaSeleccionada.ciudades

        val adapterCiudad = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_dropdown_item_1line,
            ciudades
        )

        binding.spCiudad.setAdapter(adapterCiudad)

        // 3️⃣ Setear ciudad
        binding.spCiudad.setText(ciudad, false)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
