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
import androidx.core.widget.addTextChangedListener
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

    private var plantaId: Long = -1L
    private var fotoUri: Uri? = null

    private lateinit var catalogoFinal:
            Map<String, Map<String, List<String>>>

    /* =====================
       PERMISOS / FOTO
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

    private val camaraLauncher =
        registerForActivityResult(ActivityResultContracts.TakePicture()) { ok ->
            if (ok && fotoUri != null) {
                binding.imgFoto.setImageURI(fotoUri)
                ocultarTeclado()
                actualizarEstadoGuardar()
            }
        }

    private val galeriaLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            uri?.let {
                fotoUri = copiarImagenAGaleriaInterna(it)
                binding.imgFoto.setImageURI(fotoUri)
                ocultarTeclado()
                actualizarEstadoGuardar()
            }
        }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentCrearPlantaBinding.bind(view)

        // Placeholder solo visual
        binding.imgFoto.setImageResource(R.drawable.ic_planta_placeholder)
        fotoUri = null

        // Cantidad por defecto = 1
        binding.etCantidad.setText("1")

        // Botón deshabilitado al inicio
        binding.btnGuardar.isEnabled = false
        binding.btnGuardar.setBackgroundColor(
            ContextCompat.getColor(requireContext(), R.color.btn_disabled)
        )

        plantaId = arguments?.getLong("plantaId") ?: -1L

        /* =====================
           CATÁLOGOS (NO SE TOCA)
           ===================== */

        val c1 = cargarCatalogo(requireContext(), "catalogo_kakteen.json")
        val c2 = cargarCatalogo(requireContext(), "catalogo_suculentas.json")
        val c3 = cargarCatalogo(requireContext(), "catalogo_mesembry.json")
        val c4 = cargarCatalogo(requireContext(), "catalogo_topf.json")

        catalogoFinal = unirCatalogos(c1, c2, c3, c4)

        /* =====================
           AUTOCOMPLETE
           ===================== */

        val familias = catalogoFinal.keys.toList()

        binding.actFamilia.setAdapter(
            ArrayAdapter(
                requireContext(),
                android.R.layout.simple_dropdown_item_1line,
                familias
            )
        )

        binding.actFamilia.setOnItemClickListener { _, _, _, _ ->
            val familia = binding.actFamilia.text.toString()
            val generos = catalogoFinal[familia]?.keys?.toList() ?: emptyList()

            binding.etFamilia.setAdapter(
                ArrayAdapter(
                    requireContext(),
                    android.R.layout.simple_dropdown_item_1line,
                    generos
                )
            )

            binding.etFamilia.text.clear()
            binding.etEspecie.text.clear()
            actualizarEstadoGuardar()
        }

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
            actualizarEstadoGuardar()
        }

        /* =====================
           VALIDACIÓN EN VIVO
           ===================== */

        binding.etFamilia.addTextChangedListener {
            actualizarEstadoGuardar()
        }

        /* =====================
           BOTONES FOTO
           ===================== */

        binding.btnFoto.setOnClickListener { verificarPermisoCamara() }
        binding.btnGaleria.setOnClickListener {
            galeriaLauncher.launch("image/*")
        }

        /* =====================
           GUARDAR
           ===================== */

        binding.btnGuardar.setOnClickListener {

            val genero = binding.etFamilia.text.toString().trim()

            if (genero.isEmpty()) {
                Toast.makeText(
                    requireContext(),
                    "Debés completar el género o nombre vulgar",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            if (fotoUri == null) {
                Toast.makeText(
                    requireContext(),
                    "Debés agregar una foto de la planta",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            val ahora = System.currentTimeMillis()

            val planta = Planta(
                id = ahora,
                numeroPlanta = -1,
                familia = genero,
                especie = binding.etEspecie.text.toString().ifBlank { null },
                lugar = binding.etLugar.text.toString(),
                fechaIngreso = ahora,
                cantidad = binding.etCantidad.text.toString().toInt(),
                aLaVenta = binding.cbALaVenta.isChecked,
                observaciones = binding.etObservaciones.text.toString().ifBlank { null },
                fotoRuta = fotoUri!!.toString(),
                fechaFoto = ahora
            )

            viewModel.agregarPlanta(planta)
            findNavController().popBackStack()
        }

        actualizarEstadoGuardar()
    }

    /* =====================
       VALIDACIÓN CENTRAL
       ===================== */

    private fun actualizarEstadoGuardar() {
        val generoOk = binding.etFamilia.text.toString().trim().isNotEmpty()
        val fotoOk = fotoUri != null

        val habilitar = generoOk && fotoOk

        binding.btnGuardar.isEnabled = habilitar
        binding.btnGuardar.setBackgroundColor(
            ContextCompat.getColor(
                requireContext(),
                if (habilitar) R.color.btn_enabled else R.color.btn_disabled
            )
        )
    }

    /* =====================
       AUX
       ===================== */

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
        val inputStream =
            requireContext().contentResolver.openInputStream(uri)!!

        val archivo = File(
            requireContext().filesDir,
            "planta_${System.currentTimeMillis()}.jpg"
        )

        archivo.outputStream().use { inputStream.copyTo(it) }
        return Uri.fromFile(archivo)
    }

    private fun unirCatalogos(
        vararg catalogos: Map<String, Map<String, List<String>>>
    ): Map<String, Map<String, List<String>>> {

        val resultado = mutableMapOf<String, MutableMap<String, MutableSet<String>>>()

        catalogos.forEach { catalogo ->
            catalogo.forEach { (familia, generos) ->
                val g = resultado.getOrPut(familia) { mutableMapOf() }
                generos.forEach { (gen, esp) ->
                    g.getOrPut(gen) { mutableSetOf() }.addAll(esp)
                }
            }
        }

        return resultado.mapValues { it.value.mapValues { e -> e.value.sorted() } }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
