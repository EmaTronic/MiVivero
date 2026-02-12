package com.emanuel.mivivero.ui.fragment

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
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.emanuel.mivivero.R
import com.emanuel.mivivero.data.model.Planta
import com.emanuel.mivivero.databinding.FragmentCrearPlantaBinding
import com.emanuel.mivivero.ui.viewmodel.ViveroViewModel
import com.emanuel.mivivero.utils.cargarCatalogo
import java.io.File

class CrearPlantaFragment : Fragment(R.layout.fragment_crear_planta) {

    private var _binding: FragmentCrearPlantaBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ViveroViewModel by activityViewModels()

    private var fotoUri: Uri? = null

    private lateinit var catalogoFinal:
            Map<String, Map<String, List<String>>>

    /* ===================== FOTO / PERMISOS ===================== */

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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentCrearPlantaBinding.bind(view)

        binding.imgFoto.setImageResource(R.drawable.ic_planta_placeholder)
        binding.etCantidad.setText("1")

        /* ===================== CARGAR CATÁLOGOS ===================== */

        val c1 = cargarCatalogo(requireContext(), "catalogo_kakteen.json")
        val c2 = cargarCatalogo(requireContext(), "catalogo_suculentas.json")
        val c3 = cargarCatalogo(requireContext(), "catalogo_mesembry.json")
        val c4 = cargarCatalogo(requireContext(), "catalogo_topf.json")

        catalogoFinal = unirCatalogos(c1, c2, c3, c4)

        configurarAutocomplete()

        /* ===================== BOTONES FOTO ===================== */



        binding.btnFoto.setOnClickListener { verificarPermisoCamara() }

        binding.btnGaleria.setOnClickListener {
            galeriaLauncher.launch("image/*")
        }

        /* ===================== GUARDAR ===================== */

        binding.btnGuardar.setOnClickListener {

            val familia = binding.actFamilia.text.toString().trim()
            val genero = binding.etFamilia.text.toString().trim()
            val especie = binding.etEspecie.text.toString().trim()

            if (familia.isEmpty()) {
                binding.actFamilia.error = "Campo obligatorio"
                return@setOnClickListener
            }

            if (genero.isEmpty()) {
                binding.etFamilia.error = "Campo obligatorio"
                return@setOnClickListener
            }

            if (fotoUri == null) {
                Toast.makeText(requireContext(),
                    "Debés agregar una foto",
                    Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val cantidad =
                binding.etCantidad.text.toString()
                    .toIntOrNull()?.coerceAtLeast(1) ?: 1

            val ahora = System.currentTimeMillis()

            val planta = Planta(
                id = ahora,
                numeroPlanta = -1,
                familia = familia,
                especie = especie.ifBlank { null },
                lugar = binding.etLugar.text.toString(),
                fechaIngreso = ahora,
                cantidad = cantidad,
                aLaVenta = binding.cbALaVenta.isChecked,
                observaciones = binding.etObservaciones.text.toString().ifBlank { null },
                fotoRuta = fotoUri.toString(),
                fechaFoto = ahora
            )

            viewModel.agregarPlanta(planta)
            findNavController().popBackStack()
        }

        actualizarEstadoGuardar()
    }

    /* ===================== AUTOCOMPLETE LIMPIO ===================== */

    private fun configurarAutocomplete() {

        val familias = catalogoFinal.keys.sorted()

        binding.actFamilia.setAdapter(
            ArrayAdapter(
                requireContext(),
                android.R.layout.simple_dropdown_item_1line,
                familias
            )
        )


        binding.actFamilia.setOnTouchListener { v, event ->
            if (event.action == android.view.MotionEvent.ACTION_DOWN) {
                binding.actFamilia.showDropDown()
            }
            false
        }

        binding.etFamilia.setOnTouchListener { v, event ->
            if (event.action == android.view.MotionEvent.ACTION_DOWN) {
                binding.etFamilia.showDropDown()
            }
            false
        }

        binding.etEspecie.setOnTouchListener { v, event ->
            if (event.action == android.view.MotionEvent.ACTION_DOWN) {
                binding.etEspecie.showDropDown()
            }
            false
        }

        binding.actFamilia.threshold = 0
        binding.etFamilia.threshold = 0
        binding.etEspecie.threshold = 0



        binding.actFamilia.setOnClickListener {
            binding.actFamilia.showDropDown()
        }

        binding.etFamilia.setOnClickListener {
            binding.etFamilia.showDropDown()
        }

        binding.etEspecie.setOnClickListener {
            binding.etEspecie.showDropDown()
        }



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

    /* ===================== VALIDACIÓN ===================== */

    private fun actualizarEstadoGuardar() {
        val habilitar =
            binding.actFamilia.text.toString().isNotBlank() &&
                    binding.etFamilia.text.toString().isNotBlank() &&
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

    /* ===================== AUX ===================== */

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
