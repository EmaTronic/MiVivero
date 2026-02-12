package com.emanuel.mivivero.ui.albumes

import android.app.AlertDialog
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.emanuel.mivivero.R
import com.emanuel.mivivero.data.model.AlbumConCantidad
import com.emanuel.mivivero.databinding.FragmentAlbumesBinding
import com.google.android.material.snackbar.Snackbar

class AlbumesFragment : Fragment(R.layout.fragment_albumes) {

    private var _binding: FragmentAlbumesBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AlbumesViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentAlbumesBinding.bind(view)

        configurarRecycler()
        observarAlbumes()
        configurarBotones()
        configurarSwipe()
        mostrarSnackbarUndo()

        binding.recyclerAlbumes.setOnClickListener {
            val vibrator = requireContext().getSystemService(android.os.Vibrator::class.java)
            vibrator?.vibrate(300)
        }



    }

    private fun configurarRecycler() {
        binding.recyclerAlbumes.layoutManager =
            LinearLayoutManager(requireContext())
    }

    private fun observarAlbumes() {
        viewModel.albumes.observe(viewLifecycleOwner) { lista ->
            binding.recyclerAlbumes.adapter =
                AlbumesAdapter(
                    lista,
                    onClick = { album ->
                        findNavController().navigate(
                            R.id.albumDetalleFragment,
                            Bundle().apply {
                                putLong("albumId", album.id)
                            }
                        )
                    },
                    onDeleteClick = { album ->
                        mostrarConfirmacionEliminar(album)
                    }
                )

        }


    }


    private fun mostrarSnackbarUndo() {

        com.google.android.material.snackbar.Snackbar
            .make(binding.root, "Álbum eliminado", com.google.android.material.snackbar.Snackbar.LENGTH_LONG)
            .setAction("DESHACER") {
                viewModel.restaurarUltimoAlbum()
            }
            .show()
    }

    private fun mostrarConfirmacionEliminar(album: AlbumConCantidad) {

        AlertDialog.Builder(requireContext())
            .setTitle("Eliminar álbum")
            .setMessage("¿Eliminar '${album.nombre}'?")
            .setPositiveButton("Eliminar") { _, _ ->

                eliminarConEfectos(album)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }




    private fun configurarBotones() {
        binding.fabCrearAlbum.setOnClickListener {
            findNavController().navigate(
                R.id.action_albumesFragment_to_crearAlbumesFragment
            )
        }
    }

    private fun configurarSwipe() {

        val itemTouchHelperCallback =
            object : androidx.recyclerview.widget.ItemTouchHelper.SimpleCallback(
                0,
                androidx.recyclerview.widget.ItemTouchHelper.LEFT or
                        androidx.recyclerview.widget.ItemTouchHelper.RIGHT
            ) {

                override fun onMove(
                    recyclerView: androidx.recyclerview.widget.RecyclerView,
                    viewHolder: androidx.recyclerview.widget.RecyclerView.ViewHolder,
                    target: androidx.recyclerview.widget.RecyclerView.ViewHolder
                ): Boolean = false

                override fun onSwiped(
                    viewHolder: androidx.recyclerview.widget.RecyclerView.ViewHolder,
                    direction: Int
                ) {

                    val position = viewHolder.adapterPosition
                    val album = viewModel.albumes.value?.get(position)
                        ?: return

                    eliminarConEfectos(album)
                }
            }

        val itemTouchHelper =
            androidx.recyclerview.widget.ItemTouchHelper(itemTouchHelperCallback)

        itemTouchHelper.attachToRecyclerView(binding.recyclerAlbumes)
    }

    private fun eliminarConEfectos(album: AlbumConCantidad) {

        val vibrator = requireContext().getSystemService(android.os.Vibrator::class.java)

        vibrator?.let {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {

                val effect = android.os.VibrationEffect.createWaveform(
                    longArrayOf(0, 100, 50, 100), // vibra - pausa - vibra
                    -1
                )

                it.vibrate(effect)

            } else {
                @Suppress("DEPRECATION")
                it.vibrate(200)
            }
        }

        viewModel.eliminarAlbumConUndo(album.id)

        mostrarSnackbarConIcono()
    }





    private fun mostrarSnackbarConIcono() {

        val snackbar = Snackbar.make(
            binding.root,
            "  Álbum eliminado",
            Snackbar.LENGTH_LONG
        )

        snackbar.setAction("DESHACER") {
            viewModel.restaurarUltimoAlbum()
        }

        snackbar.setBackgroundTint(
            requireContext().getColor(R.color.btn_enabled)
        )

        snackbar.setTextColor(
            requireContext().getColor(R.color.white)
        )

        snackbar.setActionTextColor(
            requireContext().getColor(R.color.yellow)
        )

        snackbar.duration = 5000

        snackbar.show()
    }








    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
