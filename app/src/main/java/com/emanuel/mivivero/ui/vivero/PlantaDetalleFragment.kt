package com.emanuel.mivivero.ui.viviero

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
import com.emanuel.mivivero.data.model.Planta
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
                val rutaFinal = it.toString() + "?t=" + System.currentTimeMillis()
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

        /*======================
            ELIMINAR FOTO SOLO CON 1 SELECC
         ===================*/
        binding.btnEliminarFoto.setOnClickListener {

            if (fotosSeleccionadas.size != 1) {
                Toast.makeText(
                    requireContext(),
                    "Seleccioná una foto",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            confirmarEliminarFoto(fotosSeleccionadas[0])
        }

        // 2️⃣ MIGRACIÓN + CARGA (una sola vez)
        viewLifecycleOwner.lifecycleScope.launch {
            val planta = viewModel.obtenerPlantaPorId(plantaId) ?: return@launch
            asegurarFotoPrincipalEnDb(planta)
            cargarFotos()
        }


        // ===== GRILLA =====
        binding.recyclerFotos.layoutManager = GridLayoutManager(requireContext(), 2)

        // ===== AGREGAR FOTO =====
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
            binding.btnEliminarFoto.isEnabled = false

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
            binding.btnEliminarFoto.isEnabled = false

            // 5️⃣ Adapter
            binding.recyclerFotos.adapter =
                FotoAdapter(
                    fotos = fotos,
                    rutaFotoPrincipal = planta?.fotoRuta,

                    // 👉 solo lógica, sin Toast
                    esSeleccionable = { fotos.size > 1 },

                    estaSeleccionada = { fotosSeleccionadas.contains(it) },

                    onClickFoto = { foto ->
                        if (fotosSeleccionadas.contains(foto)) {
                            fotosSeleccionadas.remove(foto)
                        } else if (fotosSeleccionadas.size < 2) {
                            fotosSeleccionadas.add(foto)
                        }

                        // 🔁 actualizar botones
                        binding.btnCompararFotos.isEnabled =
                            fotosSeleccionadas.size == 2

                        binding.btnEliminarFoto.isEnabled =
                            fotosSeleccionadas.size == 1
                    },

                    onLongClickFoto = { foto ->
                        confirmarCambioFotoPrincipal(foto)
                    }
                )

            binding.recyclerFotos.scrollToPosition(0)
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

    private fun cambiarFotoPrincipal(foto: FotoPlanta) {
        viewLifecycleOwner.lifecycleScope.launch {

            val planta = plantaActual ?: return@launch

            val plantaActualizada = planta.copy(
                fotoRuta = foto.ruta,
                fechaFoto = foto.fecha
            )

            // ✅ actualizar DB
            viewModel.actualizarPlanta(plantaActualizada)

            // 🔁 sincronizar estado local del fragment
            plantaActual = plantaActualizada

            // 🔄 refrescar UI del detalle
            cargarFotos()

            // 🔥 limpiar selección
            fotosSeleccionadas.clear()
            binding.btnEliminarFoto.isEnabled = false

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


    private fun borrarFoto(foto: FotoPlanta) {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.borrarFoto(foto.id)
            fotosSeleccionadas.clear()
            binding.btnEliminarFoto.isEnabled = false
            cargarFotos()
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
