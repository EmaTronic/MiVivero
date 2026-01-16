package com.emanuel.mivivero.ui.vivero

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.bumptech.glide.Glide
import com.emanuel.mivivero.R
import com.emanuel.mivivero.data.db.entity.FotoEntity
import kotlinx.coroutines.launch
import java.io.File

class PlantaDetalleFragment : Fragment(R.layout.fragment_planta_detalle) {

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

        val plantaId = arguments?.getLong("plantaId") ?: return

        // ===== CARGAR PLANTA =====
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

        // ===== FOTOS =====
        viewModel.cargarFotos(plantaId)

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.fotos.collect { fotos ->
                    val layout = view.findViewById<LinearLayout>(R.id.layoutFotos)
                    layout.removeAllViews()

                    fotos.forEach { foto ->
                        val imageView = ImageView(requireContext()).apply {
                            layoutParams = LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                500
                            )
                            scaleType = ImageView.ScaleType.CENTER_CROP
                        }

                        Glide.with(this@PlantaDetalleFragment)
                            .load(foto.rutaLocal)
                            .into(imageView)

                        layout.addView(imageView)
                    }
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
        val plantaId = arguments?.getLong("plantaId") ?: return

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
