package com.emanuel.mivivero.ui.fragment

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
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
import java.io.File
import com.emanuel.mivivero.utils.cargarCatalogo


class CrearPlantaFragment : Fragment(R.layout.fragment_crear_planta) {

    private var _binding: FragmentCrearPlantaBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ViveroViewModel by activityViewModels()

    private var plantaId: Long = -1L
    private var fotoUri: Uri? = null

    /*======================
    Catalogo
    =======================
     */

    private lateinit var catalogoFinal:
            Map<String, Map<String, List<String>>>


    /*
    private lateinit var catalogoCactus:
            Map<String, Map<String, List<String>>>

    private lateinit var catalogoSuculentas:
            Map<String, Map<String, List<String>>>
*/

    /*====================
        catalogo
     =======================*/




    /* =====================
       PERMISO CÁMARA
       ===================== */

    private val permisoCamaraLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { ok ->
            if (ok) abrirCamara()
            else Toast.makeText(
                requireContext(),
                "Permiso de cámara denegado",
                Toast.LENGTH_SHORT
            ).show()
        }

    /* =====================
       CÁMARA
       ===================== */

    private val camaraLauncher =
        registerForActivityResult(ActivityResultContracts.TakePicture()) { ok ->
            if (ok && fotoUri != null) {
                binding.imgFoto.setImageURI(fotoUri)
                binding.btnGuardar.isEnabled = true
                ocultarTeclado()
                binding.root.requestFocus()
            }
        }

    /* =====================
       GALERÍA
       ===================== */

    private val galeriaLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            uri?.let {
                try {
                    val uriLocal = copiarImagenAGaleriaInterna(it)
                    fotoUri = uriLocal
                    binding.imgFoto.setImageURI(uriLocal)
                    binding.btnGuardar.isEnabled = true
                    ocultarTeclado()
                    binding.root.requestFocus()
                } catch (e: Exception) {
                    Toast.makeText(
                        requireContext(),
                        "Error al procesar la imagen",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        _binding = FragmentCrearPlantaBinding.bind(view)

        // 🔹 Placeholder por defecto
        binding.imgFoto.setImageResource(R.drawable.ic_planta_placeholder)

        binding.btnGuardar.isEnabled = false

        plantaId = arguments?.getLong("plantaId") ?: -1L

        binding.root.isFocusableInTouchMode = true
        binding.root.requestFocus()


        /* ====================
             CATÁLOGOS
             ==================== */

        val catalogoCactus =
            cargarCatalogo(requireContext(), "catalogo_kakteen.json")

        val catalogoSuculentas =
            cargarCatalogo(requireContext(), "catalogo_suculentas.json")

        val catalogoMesemby =
            cargarCatalogo(requireContext(), "catalogo_mesembry.json")

        val catalogoVarias =
            cargarCatalogo(requireContext(), "catalogo_topf.json")

        // 👉 CATÁLOGO FINAL (UNIFICADO)
        val catalogoFinal = unirCatalogos(
            catalogoCactus,
            catalogoSuculentas,
            catalogoMesemby,
            catalogoVarias
        )

        /* ====================
           AUTOCOMPLETE
           ==================== */

        // ===== FAMILIAS =====
        val familias = catalogoFinal.keys.toList()

        binding.actFamilia.setAdapter(
            ArrayAdapter(
                requireContext(),
                android.R.layout.simple_dropdown_item_1line,
                familias
            )
        )

        binding.actFamilia.setOnClickListener {
            binding.actFamilia.showDropDown()
        }

        // ===== CUANDO SE ELIGE FAMILIA =====
        binding.actFamilia.setOnItemClickListener { _, _, _, _ ->
            val familia = binding.actFamilia.text.toString()

            val generos =
                catalogoFinal[familia]?.keys?.toList() ?: emptyList()

            binding.etFamilia.setAdapter(
                ArrayAdapter(
                    requireContext(),
                    android.R.layout.simple_dropdown_item_1line,
                    generos
                )
            )

            binding.etFamilia.text.clear()
            binding.etEspecie.text.clear()
        }


        // ===== CUANDO SE ELIGE GÉNERO =====
        binding.etFamilia.setOnItemClickListener { _, _, _, _ ->
            val familia = binding.actFamilia.text.toString()
            val genero = binding.etFamilia.text.toString()

            val especies =
                catalogoFinal[familia]?.get(genero) ?: emptyList()

            binding.etEspecie.setAdapter(
                ArrayAdapter(
                    requireContext(),
                    android.R.layout.simple_dropdown_item_1line,
                    especies
                )
            )

            binding.etEspecie.showDropDown()
        }










        /* =====================
           MODO EDICIÓN
           ===================== */

        if (plantaId != -1L) {
            val planta = viewModel.obtenerPlantaPorId(plantaId)

            planta?.let {
                binding.etFamilia.setText(it.familia)
                binding.etEspecie.setText(it.especie)
                binding.etLugar.setText(it.lugar)
                binding.etCantidad.setText(it.cantidad.toString())
                binding.cbALaVenta.isChecked = it.aLaVenta
                binding.etObservaciones.setText(it.observaciones)

                it.fotoRuta?.let { ruta ->
                    fotoUri = Uri.parse(ruta)
                    binding.imgFoto.setImageURI(fotoUri)
                    binding.btnGuardar.isEnabled = true
                }
            }
        }

        /* =====================
           BOTONES FOTO
           ===================== */

        binding.btnFoto.setOnClickListener {
            verificarPermisoCamara()
        }

        binding.btnGaleria.setOnClickListener {
            galeriaLauncher.launch("image/*")
        }

        /* =====================
           GUARDAR
           ===================== */

        binding.btnGuardar.setOnClickListener {

            // ❌ FOTO OBLIGATORIA
            if (fotoUri == null) {
                Log.e("CREAR_PLANTA", "Guardar bloqueado: sin foto")
                Toast.makeText(
                    requireContext(),
                    "Debés agregar una foto de la planta",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            Log.d("CREAR_PLANTA", "Guardar planta con foto OK")

            val ahora = System.currentTimeMillis()

            val plantaExistente =
                if (plantaId != -1L) viewModel.obtenerPlantaPorId(plantaId)
                else null

            val fechaIngresoFinal =
                plantaExistente?.fechaIngreso ?: ahora

            val planta = Planta(
                id = if (plantaId == -1L) ahora else plantaId,
                numeroPlanta = plantaExistente?.numeroPlanta ?: -1,
                familia = binding.etFamilia.text.toString(),
                especie = binding.etEspecie.text.toString().ifBlank { null },
                lugar = binding.etLugar.text.toString(),
                fechaIngreso = fechaIngresoFinal,
                cantidad = binding.etCantidad.text.toString().toIntOrNull() ?: 0,
                aLaVenta = binding.cbALaVenta.isChecked,
                observaciones = binding.etObservaciones.text.toString().ifBlank { null },
                fotoRuta = fotoUri!!.toString(),
                fechaFoto = ahora
            )

            if (plantaId == -1L) {
                viewModel.agregarPlanta(planta)
            } else {
                viewModel.actualizarPlanta(planta)
            }

            findNavController().popBackStack()
        }
    }

    /* =====================
       FUNCIONES AUX
       ===================== */

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

    private fun ocultarTeclado() {
        val imm = requireContext()
            .getSystemService(android.content.Context.INPUT_METHOD_SERVICE)
                as android.view.inputmethod.InputMethodManager

        imm.hideSoftInputFromWindow(requireView().windowToken, 0)
    }

    private fun copiarImagenAGaleriaInterna(uri: Uri): Uri {
        val inputStream = requireContext().contentResolver.openInputStream(uri)
            ?: throw IllegalArgumentException("No se pudo abrir la imagen")

        val archivo = File(
            requireContext().filesDir,
            "planta_${System.currentTimeMillis()}.jpg"
        )

        archivo.outputStream().use { output ->
            inputStream.copyTo(output)
        }

        return Uri.fromFile(archivo)
    }

    private fun unirCatalogos(
        vararg catalogos: Map<String, Map<String, List<String>>>
    ): Map<String, Map<String, List<String>>> {

        val resultado = mutableMapOf<String, MutableMap<String, MutableSet<String>>>()

        catalogos.forEach { catalogo ->
            catalogo.forEach { (familia, generos) ->
                val generosResultado =
                    resultado.getOrPut(familia) { mutableMapOf() }

                generos.forEach { (genero, especies) ->
                    val especiesResultado =
                        generosResultado.getOrPut(genero) { mutableSetOf() }

                    especiesResultado.addAll(especies)
                }
            }
        }

        return resultado.mapValues { (_, generos) ->
            generos.mapValues { (_, especies) ->
                especies.sorted()
            }
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
