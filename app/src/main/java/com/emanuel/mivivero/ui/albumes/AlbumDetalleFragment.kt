package com.emanuel.mivivero.ui.albumes

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.emanuel.mivivero.R
import com.emanuel.mivivero.data.model.EstadoAlbum
import com.emanuel.mivivero.data.model.PlantaAlbum
import com.emanuel.mivivero.databinding.DialogAgregarAlbumBinding
import com.emanuel.mivivero.databinding.FragmentAlbumDetalleBinding

class AlbumDetalleFragment : Fragment(R.layout.fragment_album_detalle) {

    private var _binding: FragmentAlbumDetalleBinding? = null
    private val binding get() = _binding!!

    private val viewModel: EditarAlbumViewModel by viewModels()

    private var albumId: Long = -1L

    private lateinit var adapter: AlbumesAdapter


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentAlbumDetalleBinding.bind(view)

        albumId = arguments?.getLong("albumId") ?: -1L
        if (albumId == -1L) {
            requireActivity().onBackPressedDispatcher.onBackPressed()
            return
        }

        val columnas =
            if (resources.configuration.smallestScreenWidthDp >= 600) 5 else 3

        binding.recyclerPlantasAlbum.layoutManager =
            GridLayoutManager(requireContext(), columnas)

        // Datos del álbum
        viewModel.obtenerAlbum(albumId)
            .observe(viewLifecycleOwner) { album ->

                if (album == null) return@observe

                binding.txtNombreAlbum.text = album.nombre
                binding.txtEstadoAlbum.text = album.estado

                configurarEstadoUI(EstadoAlbum.valueOf(album.estado))
            }
        binding.btnEditarNombre.setOnClickListener {

            val editText = android.widget.EditText(requireContext())
            editText.setText(binding.txtNombreAlbum.text.toString())

            androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("Editar nombre del álbum")
                .setView(editText)
                 .setPositiveButton("Guardar") { _, _ ->

                    val nuevoNombre = editText.text.toString().trim()

                    if (nuevoNombre.isNotEmpty()) {
                        viewModel.actualizarNombre(albumId, nuevoNombre)
                    }
                }
                .setNegativeButton("Cancelar", null)
                .show()
        }



        binding.btnEditarAlbum.setOnClickListener {

            viewModel.reabrirAlbum(albumId)


                    findNavController().navigate(
                        R.id.action_albumDetalleFragment_to_editarAlbumFragment,
                        Bundle().apply {
                        putLong("albumId", albumId)
                    }

            )
        }



        // Plantas del álbum
        viewModel.obtenerPlantasDelAlbum(albumId)
            .observe(viewLifecycleOwner) { lista ->

                binding.recyclerPlantasAlbum.adapter =
                    PlantasAlbumAdapter(
                        items = lista,
                        onAgregarClick = {
                            navegarAListaPlantas()
                        },
                        onItemClick = { planta ->
                            mostrarOpcionesPlanta(planta)
                        }
                    )
            }
    }

    private fun mostrarOpciones(planta: PlantaAlbum) {
        AlertDialog.Builder(requireContext())
            .setTitle(planta.nombre)
            .setItems(
                arrayOf("Editar cantidad / precio", "Eliminar planta")
            ) { _, which ->
                when (which) {
                    0 -> mostrarDialogoEditar(planta)
                    1 ->
                        viewModel.eliminarPlantaDelAlbum(
                            albumId = albumId,
                            plantaId = planta.plantaId
                        )
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun mostrarDialogoEditar(planta: PlantaAlbum) {
        val dialogBinding =
            DialogAgregarAlbumBinding.inflate(layoutInflater)

        dialogBinding.etCantidad.setText(planta.cantidad.toString())
        dialogBinding.etPrecio.setText(planta.precio.toString())

        AlertDialog.Builder(requireContext())
            .setTitle("Editar ${planta.nombre}")
            .setView(dialogBinding.root)
            .setPositiveButton("Guardar") { _, _ ->

                viewModel.actualizarPlanta(
                    albumId = albumId,
                    plantaId = planta.plantaId,
                    cantidad = dialogBinding.etCantidad.text.toString().toInt(),
                    precio = dialogBinding.etPrecio.text.toString().toDouble()
                ) { error ->

                    if (error != null) {
                        Toast.makeText(
                            requireContext(),
                            error,
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }


            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun navegarAListaPlantas() {

        val bundle = Bundle().apply {
            putLong("albumId", albumId)
        }

        findNavController().navigate(
            R.id.listaPlantasFragment,   // ✅ destino directo
            bundle
        )
    }
    private fun mostrarOpcionesPlanta(planta: PlantaAlbum) {

        // Ejemplo básico
        Toast.makeText(
            requireContext(),
            "Planta: ${planta.nombre}",
            Toast.LENGTH_SHORT
        ).show()

        // Acá podés:
        // - Mostrar dialog
        // - Editar cantidad
        // - Eliminar del álbum
    }



    private fun configurarEstadoUI(estado: EstadoAlbum) {

        val badge = binding.txtEstadoAlbum

        // Animación suave
        badge.animate()
            .alpha(0f)
            .setDuration(120)
            .withEndAction {

                when (estado) {

                    EstadoAlbum.BORRADOR -> {
                        badge.text = "BORRADOR"
                        badge.setBackgroundResource(R.drawable.bg_estado_borrador)
                        badge.setTextColor(requireContext().getColor(R.color.orange_700))
                        badge.setCompoundDrawablesWithIntrinsicBounds(
                            R.drawable.ic_borrador, 0, 0, 0
                        )
                    }

                    EstadoAlbum.FINALIZADO -> {
                        badge.text = "FINALIZADO"
                        badge.setBackgroundResource(R.drawable.bg_estado_finalizado)
                        badge.setTextColor(requireContext().getColor(R.color.blue_700))
                        badge.setCompoundDrawablesWithIntrinsicBounds(
                            R.drawable.ic_finalizado, 0, 0, 0
                        )
                    }

                    EstadoAlbum.PUBLICADO -> {
                        badge.text = "PUBLICADO"
                        badge.setBackgroundResource(R.drawable.bg_estado_publicado)
                        badge.setTextColor(requireContext().getColor(R.color.green_700))
                        badge.setCompoundDrawablesWithIntrinsicBounds(
                            R.drawable.ic_publicado, 0, 0, 0
                        )
                    }
                }

                badge.animate()
                    .alpha(1f)
                    .setDuration(150)
                    .start()
            }
            .start()
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
