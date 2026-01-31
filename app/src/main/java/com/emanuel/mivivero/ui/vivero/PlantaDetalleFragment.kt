package com.emanuel.mivivero.ui.fragment

import android.app.AlertDialog
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.emanuel.mivivero.R
import com.emanuel.mivivero.databinding.FragmentPlantaDetalleBinding
import com.emanuel.mivivero.ui.adapter.FotoAdapter
import com.emanuel.mivivero.ui.viewmodel.ViveroViewModel
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.navigation.fragment.findNavController
import android.Manifest
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat



class PlantaDetalleFragment : Fragment(R.layout.fragment_planta_detalle) {

    private var _binding: FragmentPlantaDetalleBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ViveroViewModel by activityViewModels()

    private var plantaId: Long = -1L
    private var fotoUri: Uri? = null

    /* =====================
       GALERÍA
       ===================== */
    private val galeriaLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            uri?.let {
                viewModel.agregarFotoExtra(plantaId, it.toString())
                cargarFotos()
            }
        }

    /* =====================
       CÁMARA
       ===================== */
    private val camaraLauncher =
        registerForActivityResult(ActivityResultContracts.TakePicture()) { ok ->
            if (ok && fotoUri != null) {
                viewModel.agregarFotoExtra(plantaId, fotoUri.toString())
                cargarFotos()
            }
        }

    private val permisoCamaraLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { ok ->
            if (ok) {
                abrirCamara()
            }
        }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentPlantaDetalleBinding.bind(view)

        plantaId = arguments?.getLong("plantaId") ?: return

        // 📷 Foto principal + datos
        viewModel.plantas.observe(viewLifecycleOwner) { lista ->
            val planta = lista.find { it.id == plantaId } ?: return@observe

            if (planta.fotoRuta != null) {
                binding.imgDetallePlanta.setImageURI(Uri.parse(planta.fotoRuta))
                binding.imgDetallePlanta.visibility = View.VISIBLE
            }

            binding.txtFamilia.text = planta.familia
            binding.txtEspecie.text = planta.especie ?: "Sin especie"
            binding.txtCantidad.text = "Cantidad: ${planta.cantidad}"
            binding.txtVenta.text =
                if (planta.aLaVenta) "Disponible para la venta" else "No disponible"

            binding.txtFechaFoto.text =
                planta.fechaFoto?.let {
                    val f = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                    "Foto tomada el ${f.format(Date(it))}"
                } ?: "Sin foto"
        }

        // 🟩 Grilla de fotos (2 columnas)
        binding.recyclerFotos.layoutManager =
            GridLayoutManager(requireContext(), 2)

        cargarFotos()

        // ➕ Agregar otra foto
        binding.btnAgregarFotoExtra.setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle("Agregar foto")
                .setItems(arrayOf("Sacar foto", "Elegir de la galería")) { _, which ->
                    when (which) {
                        0 -> verificarPermisoCamara()

                        1 -> galeriaLauncher.launch("image/*")
                    }
                }
                .show()
        }

        // ✏️ Editar
        binding.btnEditar.setOnClickListener {
            val bundle = Bundle().apply {
                putLong("plantaId", plantaId)
            }
            findNavController().navigate(R.id.crearPlantaFragment, bundle)
        }

        // 🗑️ Eliminar
        binding.btnEliminar.setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle("Eliminar planta")
                .setMessage("¿Seguro que querés eliminar esta planta?")
                .setPositiveButton("Eliminar") { _, _ ->
                    viewModel.borrarPlanta(plantaId)
                    findNavController().popBackStack()
                }
                .setNegativeButton("Cancelar", null)
                .show()
        }
    }

    /* =====================
       FUNCIONES AUX
       ===================== */

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

        camaraLauncher.launch(fotoUri)
    }

    private fun cargarFotos() {
        viewLifecycleOwner.lifecycleScope.launch {
            val fotos = viewModel.obtenerFotos(plantaId)
            binding.recyclerFotos.adapter = FotoAdapter(fotos)
        }
    }


    private fun verificarPermisoCamara() {
        when {
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED -> {
                abrirCamara()
            }
            else -> {
                permisoCamaraLauncher.launch(Manifest.permission.CAMERA)
            }
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
