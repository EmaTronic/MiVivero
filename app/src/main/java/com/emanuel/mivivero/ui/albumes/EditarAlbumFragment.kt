package com.emanuel.mivivero.ui.albumes

import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.emanuel.mivivero.R
import com.emanuel.mivivero.data.local.entity.AlbumEntity
import com.emanuel.mivivero.data.model.EstadoAlbum
import com.emanuel.mivivero.data.model.PlantaAlbum
import com.emanuel.mivivero.databinding.DialogAgregarAlbumBinding
import com.emanuel.mivivero.databinding.FragmentEditarAlbumBinding
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.launch

class EditarAlbumFragment : Fragment(R.layout.fragment_editar_album) {


    private var estadoActualAlbum: String? = null

    private var yaNavego = false

    private var _binding: FragmentEditarAlbumBinding? = null
    private val binding get() = _binding!!

    private val editarAlbumViewModel: EditarAlbumViewModel by viewModels()
    private val albumesViewModel: AlbumesViewModel by activityViewModels()

    private var albumId: Long = -1L

    private var albumActual: AlbumEntity? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentEditarAlbumBinding.bind(view)

        albumId = arguments?.getLong("albumId") ?: -1L
        if (albumId == -1L) {
            findNavController().popBackStack()
            return
        }

        val columnas =
            if (resources.configuration.smallestScreenWidthDp >= 600) 5 else 3

        binding.recyclerPlantasAlbum.layoutManager =
            GridLayoutManager(requireContext(), columnas)

        albumesViewModel.albumActualId = albumId


        // 🔹 OBSERVAR DATOS DEL ÁLBUM
        // 🔹 OBSERVAR DATOS DEL ÁLBUM
        editarAlbumViewModel.obtenerAlbum(albumId)
            .observe(viewLifecycleOwner) { album ->

                if (album == null) return@observe

                albumActual = album

                binding.btnAgregarPlantas.isEnabled =
                    album.estado == EstadoAlbum.BORRADOR.name

                binding.btnAgregarPlantas.alpha =
                    if (album.estado == EstadoAlbum.BORRADOR.name) 1f else 0.4f


                estadoActualAlbum = album.estado

                binding.txtNombreAlbum.text = album.nombre
                binding.txtEstadoAlbum.text = album.estado

                when (album.estado) {

                    "BORRADOR" -> {
                        binding.txtEstadoAlbum.setBackgroundResource(
                            R.drawable.bg_estado_borrador
                        )
                        binding.txtEstadoAlbum.setCompoundDrawablesWithIntrinsicBounds(
                            android.R.drawable.ic_menu_edit,
                            0, 0, 0
                        )
                    }

                    "FINALIZADO" -> {
                        binding.txtEstadoAlbum.setBackgroundResource(
                            R.drawable.bg_estado_finalizado
                        )
                        binding.txtEstadoAlbum.setCompoundDrawablesWithIntrinsicBounds(
                            android.R.drawable.ic_menu_agenda,
                            0, 0, 0
                        )



                    }

                    "PUBLICADO" -> {
                        binding.txtEstadoAlbum.setBackgroundResource(
                            R.drawable.bg_estado_publicado
                        )
                        binding.txtEstadoAlbum.setCompoundDrawablesWithIntrinsicBounds(
                            android.R.drawable.ic_menu_share,
                            0, 0, 0
                        )
                    }
                }
            }



        // 🔹 OBSERVAR PLANTAS DEL ÁLBUM (SEPARADO)
        editarAlbumViewModel.obtenerPlantasDelAlbum(albumId)
            .observe(viewLifecycleOwner) { lista ->

                binding.recyclerPlantasAlbum.adapter =
                    PlantasAlbumAdapter(
                        items = lista,
                        esEditable = albumActual?.estado == EstadoAlbum.BORRADOR.name,
                        onAgregarClick = { navegarAListaPlantas() },
                        onItemClick = { planta -> },
                        onItemLongClick = { planta ->

                            if (albumActual?.estado != EstadoAlbum.BORRADOR.name) {
                                Toast.makeText(
                                    requireContext(),
                                    "No se puede editar álbumes finalizados",
                                    Toast.LENGTH_SHORT
                                ).show()
                                return@PlantasAlbumAdapter
                            }

                            mostrarOpcionesPlanta(planta)
                        }
                    )
            }



        // 🔹 BOTÓN AGREGAR
        binding.btnAgregarPlantas.setOnClickListener {
            findNavController().navigate(
                R.id.action_editarAlbumFragment_to_listaPlantasFragment,
                Bundle().apply {
                    putLong("albumId", albumId)
                }
            )
        }

        // 🔹 BOTÓN FINALIZAR
        // 🔹 BOTÓN VOLVER
        binding.btnVolverDetalle.setOnClickListener {
            findNavController().popBackStack()
        }


    }




    private fun navegarAListaPlantas() {

        val bundle = Bundle().apply {
            putLong("albumId", albumId)
        }

        findNavController().navigate(
            R.id.action_editarAlbumFragment_to_listaPlantasFragment,
            bundle
        )
    }
    // ======================
    // OPCIONES
    // ======================
    private fun mostrarOpcionesPlanta(planta: PlantaAlbum) {

        val dialogView = layoutInflater
            .inflate(R.layout.dialog_opciones_planta, null)

        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .create()

        dialogView.findViewById<TextView>(R.id.txtTitulo)
            .text = planta.nombreCompleto

        dialogView.findViewById<MaterialButton>(R.id.btnEditar)
            .setOnClickListener {
                dialog.dismiss()
                mostrarDialogoEditar(planta)
            }

        dialogView.findViewById<MaterialButton>(R.id.btnEliminar)
            .setOnClickListener {
                dialog.dismiss()
                confirmarEliminar(planta)
            }

        dialogView.findViewById<TextView>(R.id.txtCancelar)
            .setOnClickListener {
                dialog.dismiss()
            }

        dialog.show()
    }


    private fun confirmarEliminar(planta: PlantaAlbum) {
        AlertDialog.Builder(requireContext())
            .setTitle("Eliminar planta")
            .setMessage("¿Eliminar ${planta.nombreCompleto} del álbum?")
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
            "ENTRÓ A EDITAR ${planta.nombreCompleto}",
            Toast.LENGTH_SHORT
        ).show()


        val dialogBinding =
            com.emanuel.mivivero.databinding.DialogAgregarAlbumBinding
                .inflate(layoutInflater)

        // 🔥 precargar valores actuales
        dialogBinding.etCantidad.setText(planta.cantidad.toString())
        dialogBinding.etPrecio.setText(planta.precio.toString())

        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Editar ${planta.nombreCompleto}")
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
