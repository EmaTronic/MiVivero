package com.emanuel.mivivero.ui.vivero

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.emanuel.mivivero.R
import com.emanuel.mivivero.data.db.entity.FotoEntity
import kotlinx.coroutines.launch
import java.io.File

class PlantaDetalleFragment : Fragment(R.layout.fragment_planta_detalle) {


    private var plantaId: Long = -1L

    private val viewModel: PlantaDetalleViewModel by viewModels {
        PlantaDetalleViewModelFactory(requireContext())
    }

    // ===== GALERÍA =====
    private val seleccionarFoto =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let { guardarFoto(it) }
        }

    // ===== CÁMARA =====
    private var uriFotoCamara: Uri? = null

    private val sacarFotoLauncher =
        registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
            if (success) {
                uriFotoCamara?.let { guardarFoto(it) }
            }
        }

    private val permisoCamaraLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) abrirCamara()
        }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        plantaId = arguments?.getLong("plantaId") ?: return


        // ===== DATOS DE LA PLANTA =====
        viewModel.cargarPlanta(plantaId)

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.planta.collect { planta ->
                    planta ?: return@collect

                    view.findViewById<TextView>(R.id.txtDetalleNumero).text =
                        planta.numeroPlanta

                    view.findViewById<TextView>(R.id.txtDetalleFamilia).text =
                        "Familia: ${planta.familia}"

                    view.findViewById<TextView>(R.id.txtDetalleEspecie).text =
                        planta.especie ?: "Especie: Sin identificar"

                    view.findViewById<TextView>(R.id.txtDetalleCantidad).text =
                        "Cantidad: ${planta.cantidad}"
                }
            }
        }

        // ===== FOTOS (SLIDER HORIZONTAL) =====
        val recyclerFotos = view.findViewById<RecyclerView>(R.id.recyclerFotos)



        //==== ADAPTER =====

        val fotoAdapter = FotoAdapter { foto, accion ->
            when (accion) {

                AccionFoto.PRINCIPAL -> {
                    viewModel.marcarComoPrincipal(foto.plantaId, foto.id)
                }

                AccionFoto.BORRAR -> {
                    viewModel.borrarFoto(foto)
                }

                AccionFoto.COMPARAR -> {
                    // reservado para pantalla de comparación
                }
            }
        }

        recyclerFotos.layoutManager =
            LinearLayoutManager(
                requireContext(),
                LinearLayoutManager.HORIZONTAL,
                false
            )

        recyclerFotos.adapter = fotoAdapter

// cargar fotos existentes
        viewModel.cargarFotos(plantaId)

// observar cambios
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.fotos.collect { fotos ->
                    fotoAdapter.submitList(fotos)
                }
            }
        }








        // ===== BOTONES =====
        view.findViewById<Button>(R.id.btnAgregarFoto).setOnClickListener {
            seleccionarFoto.launch("image/*")
        }

        view.findViewById<Button>(R.id.btnSacarFoto).setOnClickListener {
            if (tienePermisoCamara()) {
                abrirCamara()
            } else {
                permisoCamaraLauncher.launch(Manifest.permission.CAMERA)
            }
        }
    }

    // ===== HELPERS =====

    private fun tienePermisoCamara(): Boolean =
        ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED

    private fun abrirCamara() {
        uriFotoCamara = crearUriFoto(requireContext())
        sacarFotoLauncher.launch(uriFotoCamara)
    }

    private fun crearUriFoto(context: Context): Uri {
        val dir = File(context.cacheDir, "images")
        if (!dir.exists()) dir.mkdirs()

        val archivo = File(dir, "foto_${System.currentTimeMillis()}.jpg")

        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            archivo
        )
    }

    private fun guardarFoto(uri: Uri) {
        if (plantaId == -1L) return

        val foto = FotoEntity(
            id = System.currentTimeMillis(),
            plantaId = plantaId,
            rutaLocal = uri.toString(),
            fechaFoto = System.currentTimeMillis(),
            fechaGuardado = System.currentTimeMillis(),
            observaciones = null,
            esPrincipal = false
        )

        viewModel.agregarFoto(foto)
    }

}
