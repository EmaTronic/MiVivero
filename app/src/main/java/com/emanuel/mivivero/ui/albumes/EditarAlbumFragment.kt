package com.emanuel.mivivero.ui.albumes

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.emanuel.mivivero.R
import com.emanuel.mivivero.data.model.PlantaAlbum
import com.emanuel.mivivero.databinding.DialogAgregarAlbumBinding
import com.emanuel.mivivero.databinding.FragmentEditarAlbumBinding

class EditarAlbumFragment : Fragment(R.layout.fragment_editar_album) {

    private var _binding: FragmentEditarAlbumBinding? = null
    private val binding get() = _binding!!

    private val editarAlbumViewModel: EditarAlbumViewModel by viewModels()
    private val albumesViewModel: AlbumesViewModel by activityViewModels()

    private var albumId: Long = -1L

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentEditarAlbumBinding.bind(view)

        albumId = arguments?.getLong("albumId") ?: -1L
        if (albumId == -1L) {
            findNavController().popBackStack()
            return
        }

        albumesViewModel.albumActualId = albumId

        binding.recyclerPlantasAlbum.layoutManager =
            LinearLayoutManager(requireContext())

        editarAlbumViewModel.obtenerAlbum(albumId)
            .observe(viewLifecycleOwner) { album ->
                binding.txtNombreAlbum.text = album?.nombre
                binding.txtEstadoAlbum.text = album?.estado
            }

        editarAlbumViewModel.obtenerPlantasDelAlbum(albumId)
            .observe(viewLifecycleOwner) { lista ->
                binding.recyclerPlantasAlbum.adapter =
                    PlantasAlbumAdapter(lista) { planta ->
                        mostrarOpcionesPlanta(planta)
                    }
            }

        binding.btnAgregarPlantas.setOnClickListener {
            findNavController().navigate(
                R.id.action_editarAlbumFragment_to_listaPlantasFragment,
                Bundle().apply {
                    putLong("albumId", albumId)
                }
            )
        }
        binding.btnFinalizarAlbum.setOnClickListener {

            editarAlbumViewModel.finalizarAlbum(albumId) { resultado ->

                if (!resultado.esValido) {
                    Toast.makeText(
                        requireContext(),
                        resultado.mensaje,
                        Toast.LENGTH_LONG
                    ).show()
                    return@finalizarAlbum
                }

                Toast.makeText(
                    requireContext(),
                    "Álbum finalizado",
                    Toast.LENGTH_SHORT
                ).show()

                findNavController().navigate(
                    R.id.albumesFragment,
                    null,
                    androidx.navigation.NavOptions.Builder()
                        .setPopUpTo(R.id.viveroFragment, false)
                        .build()
                )
            }
        }

    }

    // ======================
    // OPCIONES
    // ======================
    private fun mostrarOpcionesPlanta(planta: PlantaAlbum) {
        AlertDialog.Builder(requireContext())
            .setTitle(planta.nombre)
            .setItems(
                arrayOf("Editar cantidad / precio", "Eliminar planta")
            ) { _, which ->
                when (which) {
                    0 -> {
                        Toast.makeText(
                            requireContext(),
                            "ENTRÓ A EDITAR",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    1 -> {
                        Toast.makeText(
                            requireContext(),
                            "ENTRÓ A ELIMINAR",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }


    private fun confirmarEliminar(planta: PlantaAlbum) {
        AlertDialog.Builder(requireContext())
            .setTitle("Eliminar planta")
            .setMessage("¿Eliminar ${planta.nombre} del álbum?")
            .setPositiveButton("Eliminar") { _, _ ->
                editarAlbumViewModel.eliminarPlantaDelAlbum(
                    albumId = albumId,
                    plantaId = planta.plantaId
                )
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun mostrarDialogoEditar(planta: PlantaAlbum) {

        Toast.makeText(
            requireContext(),
            "ENTRÓ A EDITAR ${planta.nombre}",
            Toast.LENGTH_SHORT
        ).show()


        val dialogBinding =
            com.emanuel.mivivero.databinding.DialogAgregarAlbumBinding
                .inflate(layoutInflater)

        // 🔥 precargar valores actuales
        dialogBinding.etCantidad.setText(planta.cantidad.toString())
        dialogBinding.etPrecio.setText(planta.precio.toString())

        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Editar ${planta.nombre}")
            .setView(dialogBinding.root)
            .setPositiveButton("Guardar") { _, _ ->

                val cantidadTexto = dialogBinding.etCantidad.text.toString()
                val precioTexto = dialogBinding.etPrecio.text.toString()

                val cantidad = cantidadTexto.toIntOrNull()
                val precio = precioTexto.toDoubleOrNull()

                if (cantidad == null || precio == null) {
                    Toast.makeText(
                        requireContext(),
                        "Cantidad o precio inválidos",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@setPositiveButton
                }

                // 🔥 ACÁ SE GUARDA REALMENTE
                editarAlbumViewModel.actualizarPlanta(
                    albumId = albumId,
                    plantaId = planta.plantaId,
                    cantidad = cantidad,
                    precio = precio
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
