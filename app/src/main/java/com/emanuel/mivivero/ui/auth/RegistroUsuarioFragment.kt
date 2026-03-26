package com.emanuel.mivivero.ui.auth

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.gson.Gson
import kotlinx.coroutines.launch

class RegistroUsuarioFragment : Fragment() {

    private var _binding: FragmentRegistroUsuarioBinding? = null
    private val binding get() = _binding!!

    private val viewModel: RegistroViewModel by viewModels()

    private lateinit var rootData: Root


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegistroUsuarioBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


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

        viewLifecycleOwner.lifecycleScope.launch {

            val auth = FirebaseAuth.getInstance()
            val userActual = auth.currentUser

            Log.d("USER_DEBUG", "AUTH USER: ${userActual?.uid} - ${userActual?.email}")

            if (userActual != null) {

                val uid = userActual.uid

                FirebaseFirestore.getInstance()
                    .collection("usuarios")
                    .document(uid)
                    .get()
                    .addOnSuccessListener { doc ->

                        Log.d("USER_DEBUG", "DOC EXISTS: ${doc.exists()}")
                        Log.d("USER_DEBUG", "DATA: ${doc.data}")

                        if (doc.exists()) {

                            binding.txtTituloRegistro.text = "Editar datos de usuario"
                            binding.btnRegistrar.text = "Actualizar"
                            binding.btnCerrarSesion.visibility = View.VISIBLE
                            binding.etEmail.isEnabled = false



                            binding.etNombreReal.setText(doc.getString("nombreReal") ?: "")
                            binding.etNick.setText(doc.getString("nick") ?: "")
                            binding.etNombreVivero.setText(doc.getString("nombreVivero") ?: "")
                            binding.etEmail.setText(doc.getString("email") ?: "")

                            val pais = doc.getString("pais") ?: ""
                            val provincia = doc.getString("provincia") ?: ""
                            val ciudad = doc.getString("ciudad") ?: ""

                            seleccionarUbicacion(pais, provincia, ciudad)

                        } else {
                            Log.e("USER_DEBUG", "DOCUMENTO NO EXISTE")
                        }
                    }
                    .addOnFailureListener { e ->
                        Log.e("USER_DEBUG", "ERROR FIRESTORE", e)
                    }

            } else {

                Log.e("USER_DEBUG", "USER ES NULL")

                binding.btnCerrarSesion.visibility = View.GONE
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
            val password = binding.etPassword.text.toString().trim()

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

            if (email.isBlank()) {
                binding.tilEmail.error = "Campo obligatorio"
                hayError = true
            } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                binding.tilEmail.error = "Correo inválido"
                hayError = true
            } else {
                binding.tilEmail.error = null
            }

            if (password.length < 6) {
                binding.tilPassword.error = "Mínimo 6 caracteres"
                hayError = true
            } else {
                binding.tilPassword.error = null
            }

            if (hayError) return@setOnClickListener

            Log.d("REGISTRO_DEBUG", "Intentando registrar usuario")

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

            val auth = FirebaseAuth.getInstance()

            val userActual = auth.currentUser

            if (userActual != null) {

                // 🔥 MODO EDICIÓN

                val uid = userActual.uid

                val usuarioFirestore: MutableMap<String, Any> = hashMapOf(
                    "nombreReal" to nombreReal,
                    "nick" to nick,
                    "nombreVivero" to vivero,
                    "pais" to pais,
                    "provincia" to provincia,
                    "ciudad" to ciudad,
                    "email" to email
                )

                FirebaseFirestore.getInstance()
                    .collection("usuarios")
                    .document(uid)
                    .update(usuarioFirestore)
                    .addOnSuccessListener {

                        viewLifecycleOwner.lifecycleScope.launch {
                            viewModel.guardarUsuario(usuario)

                            Toast.makeText(
                                requireContext(),
                                "Datos actualizados",
                                Toast.LENGTH_SHORT
                            ).show()

                            findNavController().popBackStack()
                        }
                    }
                    .addOnFailureListener {
                        Toast.makeText(
                            requireContext(),
                            "Error actualizando datos",
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                return@setOnClickListener
            }

            auth.createUserWithEmailAndPassword(email, password)

                .addOnSuccessListener { result ->

                    val uid = result.user?.uid ?: return@addOnSuccessListener

                    val usuarioFirestore = hashMapOf(
                        "uid" to uid,
                        "nombreReal" to nombreReal,
                        "nick" to nick,
                        "nombreVivero" to vivero,
                        "pais" to pais,
                        "provincia" to provincia,
                        "ciudad" to ciudad,
                        "email" to email,
                        "fechaRegistro" to System.currentTimeMillis(),
                        "bloqueado" to false
                    )

                    FirebaseFirestore.getInstance()
                        .collection("usuarios")
                        .document(uid)
                        .set(usuarioFirestore)

                        .addOnSuccessListener {

                            auth.currentUser?.sendEmailVerification()

                            Toast.makeText(
                                requireContext(),
                                "Revisá tu correo para verificar la cuenta",
                                Toast.LENGTH_LONG
                            ).show()

                            findNavController().navigate(R.id.verificarEmailFragment)

                            return@addOnSuccessListener

                        }

                        .addOnFailureListener { e ->

                            Log.e("REGISTRO_DEBUG", "ERROR GUARDANDO USUARIO", e)

                            Toast.makeText(
                                requireContext(),
                                "Error guardando datos de usuario",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                }

                .addOnFailureListener { e ->

                    Log.e("REGISTRO_DEBUG", "ERROR REGISTRO", e)

                    Toast.makeText(
                        requireContext(),
                        e.message ?: "Error creando usuario",
                        Toast.LENGTH_LONG
                    ).show()
                }
        }



        binding.btnCerrarSesion.setOnClickListener {

            FirebaseAuth.getInstance().signOut()

            Toast.makeText(
                requireContext(),
                "Sesión cerrada",
                Toast.LENGTH_SHORT
            ).show()

            findNavController().popBackStack(R.id.albumesFragment, false)
        }
    }

    private fun limpiarErrorAlEscribir(editText: EditText, layout: TextInputLayout) {

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
        binding.spCiudad.setText(ciudad, false)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}