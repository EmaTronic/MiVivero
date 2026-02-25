package com.emanuel.mivivero.ui.viviero

import android.Manifest
import android.app.AlertDialog
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.Outline
import android.graphics.PorterDuff
import android.net.Uri
import android.os.Bundle
import android.view.GestureDetector
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.ViewOutlineProvider
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
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
import com.emanuel.mivivero.data.model.Planta
import com.emanuel.mivivero.ui.albumes.FotoAmpliadaDialog
import com.emanuel.mivivero.ui.vivero.FotoAdapter
import com.emanuel.mivivero.ui.vivero.ViveroViewModel
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

    private var plantaActual: Planta? = null

    private val fotosSeleccionadas = mutableListOf<FotoPlanta>()

    private val fotosActuales = mutableListOf<FotoPlanta>()


    // ===== PERMISO CÁMARA =====
    private val permisoCamaraLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { ok ->
            if (ok) abrirCamara()
        }

    // ===== CÁMARA =====
    private val camaraLauncher =
        registerForActivityResult(ActivityResultContracts.TakePicture()) { ok ->
            if (ok && fotoUri != null) {

                if (fotosActuales.size >= 4) {
                    Toast.makeText(
                        requireContext(),
                        "Máximo 4 fotos por planta",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@registerForActivityResult
                }

                val rutaFinal = fotoUri.toString() + "?t=" + System.currentTimeMillis()
                viewLifecycleOwner.lifecycleScope.launch {
                    if (fotoYaExiste(rutaFinal)) {
                        Toast.makeText(
                            requireContext(),
                            "Esta foto ya fue agregada",
                            Toast.LENGTH_SHORT
                        ).show()
                        return@launch
                    }
                    viewModel.agregarFotoExtra(plantaId, rutaFinal)
                    cargarFotos()
                }
            }
        }


    // ===== GALERÍA =====
    private val galeriaLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->

            uri?.let {

                if (fotosActuales.size >= 4) {
                    Toast.makeText(
                        requireContext(),
                        "Máximo 4 fotos por planta",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@registerForActivityResult
                }

                viewLifecycleOwner.lifecycleScope.launch {

                    try {

                        val inputStream =
                            requireContext().contentResolver.openInputStream(it)

                        val archivoDestino = File(
                            requireContext().filesDir,
                            "planta_${System.currentTimeMillis()}.jpg"
                        )

                        inputStream?.use { input ->
                            archivoDestino.outputStream().use { output ->
                                input.copyTo(output)
                            }
                        }

                        val rutaFinal =
                            Uri.fromFile(archivoDestino).toString()

                        viewModel.agregarFotoExtra(plantaId, rutaFinal)

                        cargarFotos()

                    } catch (e: Exception) {
                        Toast.makeText(
                            requireContext(),
                            "Error al copiar imagen",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentPlantaDetalleBinding.bind(view)

        plantaId = arguments?.getLong("plantaId") ?: return

        // ===== OBSERVAR PLANTA =====
        viewModel.plantas.observe(viewLifecycleOwner) { lista ->
            val planta = lista.find { it.id == plantaId } ?: return@observe
            plantaActual = planta



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

            // 🔥 CLAVE: recién ahora cargar fotos   cargarFotos()
        }




        // 2️⃣ MIGRACIÓN + CARGA (una sola vez)
        viewLifecycleOwner.lifecycleScope.launch {
            val planta = viewModel.obtenerPlantaPorId(plantaId) ?: return@launch
            asegurarFotoPrincipalEnDb(planta)
            cargarFotos()
        }




        // ===== COMPARAR =====
        binding.btnCompararFotos.setOnClickListener {

            if (fotosSeleccionadas.size != 2) {
                Toast.makeText(requireContext(), "Seleccioná 2 fotos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val f1 = fotosSeleccionadas[0]
            val f2 = fotosSeleccionadas[1]

            // 🔥 limpiar selección ANTES de navegar
            fotosSeleccionadas.clear()


            findNavController().navigate(
                R.id.compararFotosFragment,
                bundleOf(
                    "fotoArriba" to f1.ruta,
                    "fotoAbajo" to f2.ruta,
                    "fechaArriba" to f1.fecha,
                    "fechaAbajo" to f2.fecha
                )
            )
        }


        // ===== EDITAR / ELIMINAR PLANTA =====
        binding.btnEditar.setOnClickListener {
            findNavController().navigate(
                R.id.crearPlantaFragment,
                bundleOf("plantaId" to plantaId)
            )
        }

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


    private fun confirmarEliminarFoto(foto: FotoPlanta) {
        AlertDialog.Builder(requireContext())
            .setTitle("Eliminar foto")
            .setMessage("¿Seguro que querés eliminar esta foto?")
            .setPositiveButton("Eliminar") { _, _ ->
                eliminarFoto(foto)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun eliminarFoto(foto: FotoPlanta) {
        viewLifecycleOwner.lifecycleScope.launch {

            // 1️⃣ borrar de DB
            viewModel.eliminarFoto(foto)

            // 2️⃣ limpiar selección
            fotosSeleccionadas.clear()

            // 3️⃣ recargar lista
            cargarFotos()

            // 4️⃣ feedback opcional
            Toast.makeText(
                requireContext(),
                "Foto eliminada",
                Toast.LENGTH_SHORT
            ).show()
        }
    }



    // ===== CARGAR FOTOS =====

    private fun cargarFotos() {
        viewLifecycleOwner.lifecycleScope.launch {

            // 1️⃣ Traer fotos desde DB
            val fotos = viewModel.obtenerFotos(plantaId).toMutableList()

            // 2️⃣ Sincronizar estado en memoria
            fotosActuales.clear()
            fotosActuales.addAll(fotos)

            val planta = plantaActual

            // 3️⃣ Mover la foto principal a posición 0
            if (planta?.fotoRuta != null) {
                val indexPrincipal = fotos.indexOfFirst {
                    it.ruta.startsWith(planta.fotoRuta!!)
                }

                if (indexPrincipal > 0) {
                    val principal = fotos.removeAt(indexPrincipal)
                    fotos.add(0, principal)
                }
            }

            // 4️⃣ Limpiar selección y estado UI
            fotosSeleccionadas.clear()
            binding.btnCompararFotos.isEnabled = false


            // 5️⃣ Adapter

            mostrarFotosEnSlots(fotos)


        }
    }


    private fun confirmarCambioFotoPrincipal(foto: FotoPlanta) {

        val planta = plantaActual ?: return

        val yaEsPrincipal =
            planta.fotoRuta != null &&
                    foto.ruta.startsWith(planta.fotoRuta!!)

        // 🔒 No permitir re-elegir la misma foto
        if (yaEsPrincipal) {
            Toast.makeText(
                requireContext(),
                "Esta foto ya es la principal",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        // ⚠️ Advertencia de flujo si hay álbum activo
        val mensaje =
            if (viewModel.hayAlbumActivo) {
                "Esta planta forma parte de un álbum de venta.\n\n" +
                        "Si cambiás la foto principal, el álbum puede quedar desactualizado.\n\n" +
                        "¿Querés continuar?"
            } else {
                "¿Querés usar esta foto como principal?"
            }

        AlertDialog.Builder(requireContext())
            .setTitle("Cambiar foto principal")
            .setMessage("¿Querés usar esta foto como principal?")
            .setPositiveButton("Aceptar") { _, _ ->
                cambiarFotoPrincipal(foto)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun mostrarFotosEnSlots(fotos: List<FotoPlanta>) {

        val slots = listOf(
            binding.slot1,
            binding.slot2,
            binding.slot3,
            binding.slot4
        )

        slots.forEach {
            it.removeAllViews()
            it.setBackgroundResource(0)
            it.setOnClickListener(null)
            it.setOnTouchListener(null)
            it.setOnLongClickListener(null)
        }

        for (i in 0 until 4) {

            val slot = slots[i]

            if (i < fotos.size) {

                val foto = fotos[i]

                val esPrincipal =
                    plantaActual?.fotoRuta != null &&
                            foto.ruta.startsWith(plantaActual!!.fotoRuta!!)

                // =========================
                // 🔹 MARCO
                // =========================
                // Quitamos fondo del slot
                slot.setBackgroundResource(0)

// CONTENEDOR EXTERIOR → MARCO
                val borderContainer = FrameLayout(requireContext()).apply {
                    layoutParams = FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.MATCH_PARENT,
                        FrameLayout.LayoutParams.MATCH_PARENT
                    )
                    setBackgroundResource(R.drawable.bg_foto_slot)
                }

// CONTENEDOR INTERIOR → RECORTE
                val clipContainer = FrameLayout(requireContext()).apply {
                    layoutParams = FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.MATCH_PARENT,
                        FrameLayout.LayoutParams.MATCH_PARENT
                    ).apply {
                        val strokePx = (6 * resources.displayMetrics.density).toInt()
                        setMargins(strokePx, strokePx, strokePx, strokePx)
                    }

                    clipToOutline = true
                    outlineProvider = object : ViewOutlineProvider() {
                        override fun getOutline(view: View, outline: Outline) {
                            val radius = 18 * resources.displayMetrics.density
                            outline.setRoundRect(0, 0, view.width, view.height, radius)
                        }
                    }
                }

// IMAGEN
                val imageView = ImageView(requireContext()).apply {
                    layoutParams = FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.MATCH_PARENT,
                        FrameLayout.LayoutParams.MATCH_PARENT
                    )
                    scaleType = ImageView.ScaleType.CENTER_CROP
                    setImageURI(Uri.parse(foto.ruta))
                }

                clipContainer.addView(imageView)
                borderContainer.addView(clipContainer)
                slot.addView(borderContainer)

                // =========================
                // 🔹 BADGE PRINCIPAL
                // =========================
                if (esPrincipal) {

                    val badge = TextView(requireContext()).apply {
                        text = "Principal"
                        setTextColor(Color.WHITE)
                        textSize = 12f
                        setPadding(20, 8, 20, 8)
                        setBackgroundResource(R.drawable.bg_badge_principal)
                    }

                    val params = FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.WRAP_CONTENT,
                        FrameLayout.LayoutParams.WRAP_CONTENT
                    )
                    params.gravity = Gravity.TOP or Gravity.START
                    params.setMargins(12, 12, 0, 0)

                    clipContainer.addView(badge, params)
                }

                // =========================
                // 🔹 OVERLAY
                // =========================
                val overlay = View(requireContext()).apply {
                    layoutParams = FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.MATCH_PARENT,
                        FrameLayout.LayoutParams.MATCH_PARENT
                    )
                    setBackgroundColor(0x5500BCD4)
                    visibility =
                        if (fotosSeleccionadas.contains(foto))
                            View.VISIBLE
                        else
                            View.GONE
                }
                clipContainer.addView(overlay)

                // =========================
                // 🔹 BOTÓN ELIMINAR
                // =========================
                val btnEliminar = ImageView(requireContext()).apply {
                    layoutParams = FrameLayout.LayoutParams(
                        110,
                        110,
                        Gravity.TOP or Gravity.END
                    ).apply {
                        topMargin = 8
                        rightMargin = 8
                    }

                    setImageResource(R.drawable.ic_delete)

                    setColorFilter(
                        ContextCompat.getColor(context, android.R.color.holo_red_dark),
                        PorterDuff.Mode.SRC_IN
                    )

                    setBackgroundResource(R.drawable.bg_boton_eliminar)
                    setPadding(16, 16, 16, 16)
                    elevation = 12f
                    visibility = View.GONE
                }

                clipContainer.addView(btnEliminar)

                // =========================
                // 🔹 GESTOS
                // =========================
                val gestureDetector = GestureDetector(
                    requireContext(),
                    object : GestureDetector.SimpleOnGestureListener() {

                        override fun onSingleTapConfirmed(e: MotionEvent): Boolean {

                            if (fotosSeleccionadas.contains(foto)) {
                                fotosSeleccionadas.remove(foto)
                            } else if (fotosSeleccionadas.size < 2) {
                                fotosSeleccionadas.add(foto)
                            }

                            val seleccionada = fotosSeleccionadas.contains(foto)

                            overlay.visibility =
                                if (seleccionada) View.VISIBLE else View.GONE

                            btnEliminar.visibility =
                                if (
                                    fotosSeleccionadas.size == 1 &&
                                    seleccionada &&
                                    !esPrincipal
                                )
                                    View.VISIBLE
                                else
                                    View.GONE

                            binding.btnCompararFotos.isEnabled =
                                fotosSeleccionadas.size == 2

                            return true
                        }

                        override fun onDoubleTap(e: MotionEvent): Boolean {

                            val formato = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                            val fechaFormateada = formato.format(Date(foto.fecha))

                            FotoAmpliadaDialog(
                                rutaFoto = foto.ruta,
                                nombre = "${plantaActual?.familia ?: ""} ${plantaActual?.especie ?: ""}".trim(),
                                fecha = "Foto tomada el $fechaFormateada"
                            ).show(parentFragmentManager, "foto")

                            return true
                        }
                    }
                )

                slot.setOnTouchListener { _, event ->
                    gestureDetector.onTouchEvent(event)
                    true
                }

                clipContainer.setOnLongClickListener {
                    confirmarCambioFotoPrincipal(foto)
                    true
                }

                btnEliminar.setOnClickListener {
                    confirmarEliminarFoto(foto)
                }

            } else {

                // =========================
                // 🔹 SLOT VACÍO
                // =========================
                slot.setBackgroundResource(R.drawable.bg_foto_slot)

                val contenedor = LinearLayout(requireContext()).apply {
                    layoutParams = FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.MATCH_PARENT,
                        FrameLayout.LayoutParams.MATCH_PARENT
                    )
                    orientation = LinearLayout.VERTICAL
                    gravity = Gravity.CENTER
                }

                val texto = TextView(requireContext()).apply {
                    text = "Agregar foto"
                    setTextColor(Color.DKGRAY)
                    textSize = 14f
                    gravity = Gravity.CENTER
                }

                val icono = ImageView(requireContext()).apply {
                    setImageResource(R.drawable.ic_camara)
                    val size = 80
                    layoutParams = LinearLayout.LayoutParams(size, size)
                }

                contenedor.addView(texto)
                contenedor.addView(icono)

                slot.addView(contenedor)

                slot.setOnClickListener {

                    if (fotosActuales.size >= 4) {
                        Toast.makeText(
                            requireContext(),
                            "Máximo 4 fotos por planta",
                            Toast.LENGTH_SHORT
                        ).show()
                        return@setOnClickListener
                    }

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
            }
        }
    }

    private fun cambiarFotoPrincipal(foto: FotoPlanta) {
        viewLifecycleOwner.lifecycleScope.launch {

            val planta = plantaActual ?: return@launch

            val plantaActualizada = planta.copy(
                fotoRuta = foto.ruta,
                fechaFoto = foto.fecha
            )

            // Actualizar DB
            viewModel.actualizarPlanta(plantaActualizada)

            // Sincronizar estado local
            plantaActual = plantaActualizada

            // 🔥 ACTUALIZAR FECHA EN PANTALLA
            val formato = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
            binding.txtFechaFoto.text =
                "Foto tomada el ${formato.format(Date(foto.fecha))}"

            // Refrescar grilla
            cargarFotos()

            Toast.makeText(
                requireContext(),
                "Foto principal actualizada",
                Toast.LENGTH_SHORT
            ).show()
        }
    }





    /*
    private fun intentarBorrarFoto(foto: FotoPlanta) {
        val planta = plantaActual ?: return
        val esPrincipal =
            planta.fotoRuta != null &&
                    foto.ruta.startsWith(planta.fotoRuta!!)

        if (esPrincipal) {
            Toast.makeText(
                requireContext(),
                "No podés borrar la foto principal.\nElegí otra primero.",
                Toast.LENGTH_LONG
            ).show()
            return
        }

        AlertDialog.Builder(requireContext())
            .setTitle("Eliminar foto")
            .setMessage("¿Seguro que querés eliminar esta foto?")
            .setPositiveButton("Eliminar") { _, _ ->
                borrarFoto(foto)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
    */



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

    private fun esFotoPrincipal(foto: FotoPlanta): Boolean {
        val planta = plantaActual ?: return false
        return planta.fotoRuta != null &&
                foto.ruta.startsWith(planta.fotoRuta!!)
    }

    private fun hayMasDeUnaFoto(): Boolean {
        return fotosActuales.size > 1
    }

    private fun fotoYaExiste(rutaNueva: String): Boolean {
        return fotosActuales.any {
            it.ruta.substringBefore("?") ==
                    rutaNueva.substringBefore("?")
        }
    }


    private suspend fun asegurarFotoPrincipalEnDb(planta: Planta) {

        if (planta.fotoRuta == null) return

        val fotos = viewModel.obtenerFotos(planta.id)

        val yaExiste = fotos.any {
            it.ruta.startsWith(planta.fotoRuta)
        }

        if (!yaExiste) {
            viewModel.agregarFotoExtra(
                planta.id,
                planta.fotoRuta
            )
        }
    }





    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
