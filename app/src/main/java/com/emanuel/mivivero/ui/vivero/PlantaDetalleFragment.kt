package com.emanuel.mivivero.ui.vivero

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import com.emanuel.mivivero.R
import com.emanuel.mivivero.data.model.FotoPlanta
import com.emanuel.mivivero.data.repository.PlantaRepository
import java.io.File
import android.Manifest
import android.content.pm.PackageManager
import android.widget.ImageView
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide



class PlantaDetalleFragment : Fragment(R.layout.fragment_planta_detalle) {

    // Launcher galería
    private val seleccionarFoto =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                mostrarImagenPrueba(it)
            }
        }


    // Cámara
    private var uriFotoCamara: Uri? = null

    private val sacarFotoLauncher =
        registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
            if (success) {
                uriFotoCamara?.let { guardarFoto(it) }
            }
        }

    private fun tienePermisoCamara(): Boolean {
        return ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }

    private val permisoCamaraLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) {
                abrirCamara()
            }
        }

    private fun abrirCamara() {
        uriFotoCamara = crearUriFoto(requireContext())
        sacarFotoLauncher.launch(uriFotoCamara)
    }



    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Obtener ID de la planta
        val plantaId = arguments?.getLong("plantaId") ?: return

        val repository = PlantaRepository()
        val planta = repository.getPlantaById(plantaId) ?: return

        // Mostrar datos
        view.findViewById<TextView>(R.id.txtDetalleNumero).text =
            planta.numeroPlanta

        view.findViewById<TextView>(R.id.txtDetalleFamilia).text =
            "Familia: ${planta.familia}"

        view.findViewById<TextView>(R.id.txtDetalleEspecie).text =
            planta.especie ?: "Especie: Sin identificar"

        view.findViewById<TextView>(R.id.txtDetalleCantidad).text =
            "Cantidad: ${planta.cantidad}"

        // Botón galería
        val btnAgregarFoto = view.findViewById<Button>(R.id.btnAgregarFoto)
        btnAgregarFoto.setOnClickListener {
            seleccionarFoto.launch("image/*")
        }


        // Botón cámara
        val btnSacarFoto = view.findViewById<Button>(R.id.btnSacarFoto)
        btnSacarFoto.setOnClickListener {
            if (tienePermisoCamara()) {
                abrirCamara()
            } else {
                permisoCamaraLauncher.launch(Manifest.permission.CAMERA)
            }
        }


        // Mostrar fotos
        mostrarFotos(plantaId)
    }

    private fun mostrarImagenPrueba(uri: Uri) {
        val imageView = view?.findViewById<ImageView>(R.id.imgPlantaTest) ?: return

        Glide.with(this)
            .load(uri)
            .centerCrop()
            .into(imageView)
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

        val foto = FotoPlanta(
            id = System.currentTimeMillis(),
            plantaId = plantaId,
            rutaLocal = uri.toString(),
            fechaFoto = System.currentTimeMillis(),
            fechaGuardado = System.currentTimeMillis(),
            observaciones = null
        )

        val repository = PlantaRepository()
        repository.addFoto(foto)

        mostrarFotos(plantaId)
    }

    private fun mostrarFotos(plantaId: Long) {
        val repository = PlantaRepository()
        val fotos = repository.getFotosDePlanta(plantaId)

        val layoutFotos =
            view?.findViewById<LinearLayout>(R.id.layoutFotos) ?: return

        layoutFotos.removeAllViews()

        fotos.forEach { foto ->
            val textView = TextView(requireContext())
            textView.text = "📷 ${foto.rutaLocal}"
            layoutFotos.addView(textView)
        }
    }
}
