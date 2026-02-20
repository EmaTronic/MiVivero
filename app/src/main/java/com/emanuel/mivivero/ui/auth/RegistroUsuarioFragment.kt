package com.emanuel.mivivero.ui.auth

import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.emanuel.mivivero.R
import com.emanuel.mivivero.data.db.entity.UsuarioEntity
import com.emanuel.mivivero.data.model.Root
import com.emanuel.mivivero.databinding.FragmentRegistroUsuarioBinding
import com.google.gson.Gson
import kotlinx.coroutines.launch

class RegistroUsuarioFragment :
    Fragment(R.layout.fragment_registro_usuario) {

    private var _binding: FragmentRegistroUsuarioBinding? = null
    private val binding get() = _binding!!

    private val viewModel: RegistroViewModel by viewModels()

    private lateinit var rootData: Root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentRegistroUsuarioBinding.bind(view)

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
            val pais = binding.spPais.selectedItem?.toString() ?: ""
            val provincia = binding.spProvincia.selectedItem?.toString() ?: ""
            val ciudad = binding.spCiudad.selectedItem?.toString() ?: ""
            val email = binding.etEmail.text.toString().trim()

            if (nombreReal.isEmpty() ||
                nick.isEmpty() ||
                vivero.isEmpty() ||
                pais.isEmpty() ||
                provincia.isEmpty() ||
                ciudad.isEmpty()
            ) {
                Toast.makeText(
                    requireContext(),
                    "Completar todos los campos",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                binding.etEmail.error = "Correo inválido"
                return@setOnClickListener
            }

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
            R.layout.spinner_item,
            paises
        )

        adapterPais.setDropDownViewResource(R.layout.spinner_dropdown_item)

        binding.spPais.adapter = adapterPais

        binding.spPais.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {

                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {

                    val paisSeleccionado = rootData.paises[position]




                    val provincias = paisSeleccionado.provincias
                        .map { it.nombre }

                    val adapterProvincia = ArrayAdapter(
                        requireContext(),
                        R.layout.spinner_item,
                        provincias
                    )

                    adapterProvincia.setDropDownViewResource(
                        R.layout.spinner_dropdown_item
                    )

                    binding.spProvincia.adapter = adapterProvincia
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }

        binding.spProvincia.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {

                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {

                    val paisIndex = binding.spPais.selectedItemPosition
                    val paisSeleccionado = rootData.paises[paisIndex]

                    val provinciaSeleccionada =
                        paisSeleccionado.provincias[position]

                    val ciudades = provinciaSeleccionada.ciudades

                    val adapterCiudad = ArrayAdapter(
                        requireContext(),
                        R.layout.spinner_item,
                        ciudades
                    )

                    adapterCiudad.setDropDownViewResource(
                        R.layout.spinner_dropdown_item
                    )

                    binding.spCiudad.adapter = adapterCiudad
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }
    }

    private fun seleccionarUbicacion(
        pais: String,
        provincia: String,
        ciudad: String
    ) {

        val paisIndex = rootData.paises.indexOfFirst { it.nombre == pais }
        if (paisIndex >= 0) {
            binding.spPais.setSelection(paisIndex)

            val provincias = rootData.paises[paisIndex].provincias
            val provinciaIndex =
                provincias.indexOfFirst { it.nombre == provincia }

            if (provinciaIndex >= 0) {
                binding.spProvincia.setSelection(provinciaIndex)

                val ciudades = provincias[provinciaIndex].ciudades
                val ciudadIndex = ciudades.indexOfFirst { it == ciudad }

                if (ciudadIndex >= 0) {
                    binding.spCiudad.setSelection(ciudadIndex)
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
