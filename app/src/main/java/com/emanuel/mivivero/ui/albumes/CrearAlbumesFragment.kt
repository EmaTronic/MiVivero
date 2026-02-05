package com.emanuel.mivivero.ui.albumes

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.emanuel.mivivero.R
import com.emanuel.mivivero.data.model.Planta
import com.emanuel.mivivero.databinding.FragmentCrearAlbumesBinding



class CrearAlbumesFragment : Fragment(R.layout.fragment_crear_albumes) {

    private var _binding: FragmentCrearAlbumesBinding? = null
    private val binding get() = _binding!!

    private val viewModel: CrearAlbumesViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentCrearAlbumesBinding.bind(view)

        binding.btnContinuar.setOnClickListener {
            val nombre = binding.etNombreAlbum.text.toString().trim()
            val obs = binding.etObservacionesAlbum.text.toString().trim()

            if (nombre.isEmpty()) {
                binding.etNombreAlbum.error = "El nombre es obligatorio"
                return@setOnClickListener
            }

            viewModel.crearAlbum(nombre, obs)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
