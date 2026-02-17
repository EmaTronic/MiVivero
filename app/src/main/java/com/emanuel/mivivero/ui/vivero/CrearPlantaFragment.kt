package com.emanuel.mivivero.ui.vivero

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.emanuel.mivivero.R
import com.emanuel.mivivero.data.model.Planta
import com.emanuel.mivivero.databinding.FragmentCrearPlantaBinding
import com.emanuel.mivivero.utils.cargarCatalogo
import java.io.File

class CrearPlantaFragment : Fragment(R.layout.fragment_crear_planta) {

    private var _binding: FragmentCrearPlantaBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ViveroViewModel by activityViewModels()

    private var fotoUri: Uri? = null
    private var plantaId: Long = -1L

    private lateinit var catalogoFinal:
            Map<String, Map<String, List<String>>>

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentCrearPlantaBinding.bind(view)

        plantaId = arguments?.getLong("plantaId") ?: -1L

        // 🔥 TÍTULO DINÁMICO
        if (plantaId != -1L) {
            binding.txtTitulo.text = "Editar planta"
            binding.btnGuardar.text = "Guardar cambios"
        } else {
            binding.txtTitulo.text = "Crear planta"
            binding.btnGuardar.text = "Guardar"
        }



        binding.imgFoto.setImageResource(R.drawable.ic_planta_placeholder)
        binding.etCantidad.setText("1")

        /* ===================== CARGAR DATOS SI ES EDICIÓN ===================== */

        if (plantaId != -1L) {
            viewModel.plantas.observe(viewLifecycleOwner) { lista ->
                val planta = lista.find { it.id == plantaId } ?: return@observe

                binding.actFamilia.setText(planta.familia, false)
                binding.etEspecie.setText(planta.especie ?: "", false)
                binding.etLugar.setText(planta.lugar)
                binding.etCantidad.setText(planta.cantidad.toString())
                binding.cbALaVenta.isChecked = planta.aLaVenta
                binding.etObservaciones.setText(planta.observaciones ?: "")

                fotoUri = Uri.parse(planta.fotoRuta)
                binding.imgFoto.setImageURI(fotoUri)

                actualizarEstadoGuardar()
            }
        }

        /* ===================== CATÁLOGOS ===================== */

        val c1 = cargarCatalogo(requireContext(), "catalogo_kakteen.json")
        val c2 = cargarCatalogo(requireContext(), "catalogo_suculentas.json")
        val c3 = cargarCatalogo(requireContext(), "catalogo_mesembry.json")
        val c4 = cargarCatalogo(requireContext(), "catalogo_topf.json")

        catalogoFinal = unirCatalogos(c1, c2, c3, c4)
        configurarAutocomplete()

        /* ===================== LISTENERS PARA HABILITAR GUARDAR ===================== */

        binding.actFamilia.addTextChangedListener { actualizarEstadoGuardar() }
        binding.etEspecie.addTextChangedListener { actualizarEstadoGuardar() }
        binding.etCantidad.addTextChangedListener { actualizarEstadoGuardar() }
        binding.etLugar.addTextChangedListener { actualizarEstadoGuardar() }
        binding.etObservaciones.addTextChangedListener { actualizarEstadoGuardar() }

        /* ===================== FOTO ===================== */

        binding.btnFoto.setOnClickListener { verificarPermisoCamara() }

        binding.btnGaleria.setOnClickListener {
            galeriaLauncher.launch("image/*")
        }

        /* ===================== GUARDAR ===================== */

        /* ===================== GUARDAR ===================== */

        binding.btnGuardar.setOnClickListener {

            val familia = binding.etFamilia.text.toString().trim()
            val especie = binding.etEspecie.text.toString().trim()

            if (familia.isEmpty()) {
                binding.actFamilia.error = "Campo obligatorio"
                return@setOnClickListener
            }

            if (fotoUri == null) {
                Toast.makeText(
                    requireContext(),
                    "Debés agregar una foto",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            val cantidad =
                binding.etCantidad.text.toString()
                    .toIntOrNull()?.coerceAtLeast(1) ?: 1

            val ahora = System.currentTimeMillis()

            val plantaExistente =
                if (plantaId != -1L)
                    viewModel.plantas.value?.find { it.id == plantaId }
                else null

            val fechaIngresoFinal =
                plantaExistente?.fechaIngreso ?: ahora

            val fechaFotoFinal =
                if (plantaExistente != null) {
                    if (fotoUri.toString() != plantaExistente.fotoRuta)
                        obtenerFechaRealDeFoto(fotoUri!!)
                    else
                        plantaExistente.fechaFoto
                } else {
                    obtenerFechaRealDeFoto(fotoUri!!)
                }

            val idFinal =
                if (plantaId != -1L)
                    plantaId
                else
                    ahora

            val planta = Planta(
                id = idFinal,
                numeroPlanta = -1,
                familia = familia,
                especie = especie.ifBlank { null },
                lugar = binding.etLugar.text.toString(),
                fechaIngreso = fechaIngresoFinal,
                cantidad = cantidad,
                aLaVenta = binding.cbALaVenta.isChecked,
                observaciones = binding.etObservaciones.text.toString().ifBlank { null },
                fotoRuta = fotoUri.toString(),
                fechaFoto = fechaFotoFinal
            )

            if (plantaId != -1L) {
                viewModel.actualizarPlanta(planta)
            } else {
                viewModel.agregarPlanta(planta)
            }

            findNavController().popBackStack()
        }

        actualizarEstadoGuardar()

    }


        private fun obtenerFechaRealDeFoto(uri: Uri): Long {
        return try {
            val inputStream = requireContext().contentResolver.openInputStream(uri)
            val exif = androidx.exifinterface.media.ExifInterface(inputStream!!)
            val fechaStr = exif.getAttribute(
                androidx.exifinterface.media.ExifInterface.TAG_DATETIME_ORIGINAL
            )

            if (fechaStr != null) {
                val formato = java.text.SimpleDateFormat(
                    "yyyy:MM:dd HH:mm:ss",
                    java.util.Locale.getDefault()
                )
                formato.parse(fechaStr)?.time ?: System.currentTimeMillis()
            } else {
                System.currentTimeMillis()
            }
        } catch (e: Exception) {
            System.currentTimeMillis()
        }
    }

    /* ===================== VALIDACIÓN BOTÓN ===================== */

    private fun actualizarEstadoGuardar() {

        val habilitar =
            binding.actFamilia.text.toString().isNotBlank() &&
                    fotoUri != null

        binding.btnGuardar.isEnabled = habilitar

        binding.btnGuardar.setBackgroundColor(
            ContextCompat.getColor(
                requireContext(),
                if (habilitar) R.color.btn_enabled
                else R.color.btn_disabled
            )
        )
    }

    /* ===================== FOTO ===================== */

    private val permisoCamaraLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { ok ->
            if (ok) abrirCamara()
        }

    private val camaraLauncher =
        registerForActivityResult(ActivityResultContracts.TakePicture()) { ok ->
            if (ok && fotoUri != null) {
                binding.imgFoto.setImageURI(fotoUri)
                actualizarEstadoGuardar()
            }
        }

    private val galeriaLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            uri?.let {
                fotoUri = copiarImagen(it)
                binding.imgFoto.setImageURI(fotoUri)
                actualizarEstadoGuardar()
            }
        }

    private fun verificarPermisoCamara() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            abrirCamara()
        } else {
            permisoCamaraLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    private fun abrirCamara() {
        val archivo = File(
            requireContext().getExternalFilesDir(null),
            "planta_${System.currentTimeMillis()}.jpg"
        )

        fotoUri = FileProvider.getUriForFile(
            requireContext(),
            "${requireContext().packageName}.fileprovider",
            archivo
        )

        camaraLauncher.launch(fotoUri)
    }

    private fun copiarImagen(uri: Uri): Uri {
        val inputStream =
            requireContext().contentResolver.openInputStream(uri)!!

        val archivo = File(
            requireContext().getExternalFilesDir(null),
            "planta_${System.currentTimeMillis()}.jpg"
        )

        archivo.outputStream().use { inputStream.copyTo(it) }

        return Uri.fromFile(archivo)
    }

    private fun configurarAutocomplete() {

        val familias = catalogoFinal.keys.sorted()

        binding.actFamilia.setAdapter(
            ArrayAdapter(
                requireContext(),
                android.R.layout.simple_dropdown_item_1line,
                familias
            )
        )

        // 🔥 Mostrar dropdown al tocar aunque esté vacío
        binding.actFamilia.threshold = 0
        binding.etFamilia.threshold = 0
        binding.etEspecie.threshold = 0

        binding.actFamilia.setOnTouchListener { _, event ->
            if (event.action == android.view.MotionEvent.ACTION_DOWN) {
                binding.actFamilia.showDropDown()
            }
            false
        }

        binding.etFamilia.setOnTouchListener { _, event ->
            if (event.action == android.view.MotionEvent.ACTION_DOWN) {
                binding.etFamilia.showDropDown()
            }
            false
        }

        binding.etEspecie.setOnTouchListener { _, event ->
            if (event.action == android.view.MotionEvent.ACTION_DOWN) {
                binding.etEspecie.showDropDown()
            }
            false
        }

        // 🔥 Cuando se elige familia → cargar géneros
        binding.actFamilia.setOnItemClickListener { _, _, _, _ ->

            val familia = binding.actFamilia.text.toString()

            val generos =
                catalogoFinal[familia]?.keys?.sorted()
                    ?: emptyList()

            binding.etFamilia.setAdapter(
                ArrayAdapter(
                    requireContext(),
                    android.R.layout.simple_dropdown_item_1line,
                    generos
                )
            )

            binding.etFamilia.text.clear()
            binding.etEspecie.text.clear()
            binding.etEspecie.isEnabled = false
        }

        // 🔥 Cuando se elige género → cargar especies
        binding.etFamilia.setOnItemClickListener { _, _, _, _ ->

            val familia = binding.actFamilia.text.toString()
            val genero = binding.etFamilia.text.toString()

            val especies =
                catalogoFinal[familia]?.get(genero)?.sorted()
                    ?: emptyList()

            binding.etEspecie.setAdapter(
                ArrayAdapter(
                    requireContext(),
                    android.R.layout.simple_dropdown_item_1line,
                    especies
                )
            )

            binding.etEspecie.isEnabled = true
            binding.etEspecie.text.clear()
        }
    }

    private fun unirCatalogos(
        vararg catalogos: Map<String, Map<String, List<String>>>
    ): Map<String, Map<String, List<String>>> {

        val res = mutableMapOf<String, MutableMap<String, MutableSet<String>>>()

        catalogos.forEach { cat ->
            cat.forEach { (familia, generos) ->
                val g = res.getOrPut(familia) { mutableMapOf() }
                generos.forEach { (gen, esp) ->
                    g.getOrPut(gen) { mutableSetOf() }.addAll(esp)
                }
            }
        }

        return res.mapValues { it.value.mapValues { e -> e.value.sorted() } }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
