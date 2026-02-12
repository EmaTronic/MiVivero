package com.emanuel.mivivero.ui.albumes

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.emanuel.mivivero.R
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

        binding.recyclerPlantasAlbum.layoutManager =
            LinearLayoutManager(requireContext())

        // Datos del álbum
        viewModel.obtenerAlbum(albumId)
            .observe(viewLifecycleOwner) { album ->

                binding.txtNombreAlbum.text = album?.nombre
                binding.txtEstadoAlbum.text = album?.estado

                when (album?.estado) {

                    "BORRADOR" -> {
                        binding.btnEditarAlbum.visibility = View.VISIBLE
                        binding.btnEditarAlbum.text = "Continuar edición"
                        binding.btnPublicarAlbum.visibility = View.GONE
                    }

                    "FINALIZADO" -> {
                        binding.btnEditarAlbum.visibility = View.VISIBLE
                        binding.btnEditarAlbum.text = "Reabrir edición"
                        binding.btnPublicarAlbum.visibility = View.VISIBLE
                    }

                    "PUBLICADO" -> {
                        binding.btnEditarAlbum.visibility = View.GONE
                        binding.btnPublicarAlbum.visibility = View.VISIBLE
                    }

                    else -> {
                        binding.btnEditarAlbum.visibility = View.GONE
                    }
                }
            }


        binding.btnEditarAlbum.setOnClickListener {

            viewModel.reabrirAlbum(albumId)

            findNavController().navigate(
                R.id.editarAlbumFragment,
                Bundle().apply {
                    putLong("albumId", albumId)
                }
            )
        }



        // Plantas del álbum
        viewModel.obtenerPlantasDelAlbum(albumId)
            .observe(viewLifecycleOwner) { lista ->

                binding.recyclerPlantasAlbum.adapter =
                    PlantasAlbumAdapter(lista) { planta ->
                        mostrarOpciones(planta)
                    }
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


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
