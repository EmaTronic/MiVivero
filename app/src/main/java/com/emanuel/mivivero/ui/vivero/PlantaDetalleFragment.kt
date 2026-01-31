package com.emanuel.mivivero.ui.fragment

import android.Manifest
import android.app.AlertDialog
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.emanuel.mivivero.R
import com.emanuel.mivivero.databinding.FragmentPlantaDetalleBinding
import com.emanuel.mivivero.data.model.FotoPlanta
import com.emanuel.mivivero.ui.adapter.FotoAdapter
import com.emanuel.mivivero.ui.viewmodel.ViveroViewModel
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.launch

class PlantaDetalleFragment : Fragment(R.layout.fragment_planta_detalle) {

    private var _binding: FragmentPlantaDetalleBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ViveroViewModel by activityViewModels()

    private var plantaId: Long = -1L
    private var fotoUri: Uri? = null

    private val fotosSeleccionadas = mutableListOf<FotoPlanta>()

    // ===== PERMISO CÁMARA =====
    private val permisoCamaraLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { ok ->
            if (ok) abrirCamara()
        }

    // ===== CÁMARA =====
    private val camaraLauncher =
        registerForActivityResult(ActivityResultContracts.TakePicture()) { ok ->
            if (ok && fotoUri != null) {
                val rutaFinal = fotoUri.toString() + "?t=" + System.currentTimeMillis()
                viewModel.agregarFotoExtra(plantaId, rutaFinal)
                cargarFotos()
            }
        }

    // ===== GALERÍA =====
    private val galeriaLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            uri?.let {
                val rutaFinal = it.toString() + "?t=" + System.currentTimeMillis()
                viewModel.agregarFotoExtra(plantaId, rutaFinal)
                cargarFotos()
            }
        }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentPlantaDetalleBinding.bind(view)

        plantaId = arguments?.getLong("plantaId") ?: return

        // Datos planta
        viewModel.plantas.observe(viewLifecycleOwner) { lista ->
            val planta = lista.find { it.id == plantaId } ?: return@observe

            planta.fotoRuta?.let {
                binding.imgDetallePlanta.setImageURI(Uri.parse(it))
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

        // Grilla
        binding.recyclerFotos.layoutManager = GridLayoutManager(requireContext(), 2)
        cargarFotos()

        // Agregar foto
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

        // Comparar
        binding.btnCompararFotos.setOnClickListener {
            if (fotosSeleccionadas.size != 2) {
                Toast.makeText(
                    requireContext(),
                    "Seleccioná 2 fotos",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            val f1 = fotosSeleccionadas[0]
            val f2 = fotosSeleccionadas[1]

            findNavController().navigate(
                R.id.compararFotosFragment,
                bundleOf(
                    "fotoArriba" to f1.ruta,
                    "fotoAbajo" to f2.ruta
                )
            )
        }

        // Editar
        binding.btnEditar.setOnClickListener {
            findNavController().navigate(
                R.id.crearPlantaFragment,
                bundleOf("plantaId" to plantaId)
            )
        }

        // Eliminar
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

    private fun cargarFotos() {
        viewLifecycleOwner.lifecycleScope.launch {
            val fotos = viewModel.obtenerFotos(plantaId)

            binding.recyclerFotos.adapter =
                FotoAdapter(
                    fotos = fotos,
                    esSeleccionable = { true },
                    estaSeleccionada = { fotosSeleccionadas.contains(it) }
                ) { foto ->
                    if (fotosSeleccionadas.contains(foto)) {
                        fotosSeleccionadas.remove(foto)
                    } else if (fotosSeleccionadas.size < 2) {
                        fotosSeleccionadas.add(foto)
                    }
                }
        }
    }

    private fun verificarPermisoCamara() {
        when {
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED -> abrirCamara()
            else -> permisoCamaraLauncher.launch(Manifest.permission.CAMERA)
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

        camaraLauncher.launch(fotoUri)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
