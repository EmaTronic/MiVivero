package com.emanuel.mivivero.ui.fragment

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.emanuel.mivivero.R
import com.emanuel.mivivero.data.model.Planta
import com.emanuel.mivivero.databinding.FragmentCrearPlantaBinding
import com.emanuel.mivivero.ui.viewmodel.ViveroViewModel
import java.io.File

class CrearPlantaFragment : Fragment(R.layout.fragment_crear_planta) {

    private var _binding: FragmentCrearPlantaBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ViveroViewModel by activityViewModels()

    private var fotoUri: Uri? = null

    /* =========================
       PERMISO DE CÁMARA
       ========================= */

    private val pedirPermisoCamara =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { concedido ->
            if (concedido) {
                abrirCamara()
            } else {
                Toast.makeText(
                    requireContext(),
                    "Permiso de cámara denegado",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

    /* =========================
       TOMAR FOTO
       ========================= */

    private val tomarFoto =
        registerForActivityResult(ActivityResultContracts.TakePicture()) { ok ->
            if (ok) {
                binding.imgFoto.setImageURI(fotoUri)
            }
        }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentCrearPlantaBinding.bind(view)

        binding.btnFoto.setOnClickListener {
            verificarPermisoCamara()
        }

        binding.btnGuardar.setOnClickListener {

            val planta = Planta(
                id = System.currentTimeMillis(),
                numeroPlanta = binding.etNumeroPlanta.text.toString(),
                familia = binding.etFamilia.text.toString(),
                especie = binding.etEspecie.text.toString().ifBlank { null },
                lugar = binding.etLugar.text.toString(),
                fechaIngreso = System.currentTimeMillis(),
                cantidad = binding.etCantidad.text.toString().toIntOrNull() ?: 0,
                aLaVenta = binding.cbALaVenta.isChecked,
                observaciones = binding.etObservaciones.text.toString().ifBlank { null },
                fotoRuta = fotoUri?.toString()
            )

            viewModel.agregarPlanta(planta)
            findNavController().popBackStack()
        }
    }

    /* =========================
       FUNCIONES AUXILIARES
       ========================= */

    private fun verificarPermisoCamara() {
        when {
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED -> {
                abrirCamara()
            }

            else -> {
                pedirPermisoCamara.launch(Manifest.permission.CAMERA)
            }
        }
    }

    private fun abrirCamara() {
        val archivo = File(
            requireContext().filesDir,
            "planta_${System.currentTimeMillis()}.jpg"
        )

        fotoUri = FileProvider.getUriForFile(
            requireContext(),
            "${requireContext().packageName}.fileprovider",
            archivo
        )

        tomarFoto.launch(fotoUri)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
