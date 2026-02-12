package com.emanuel.mivivero.ui.albumes

import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.emanuel.mivivero.R
import com.emanuel.mivivero.data.model.AlbumConCantidad
import com.emanuel.mivivero.data.model.PlantaAlbum
import com.emanuel.mivivero.databinding.FragmentAlbumesBinding
import com.google.android.material.snackbar.Snackbar

class AlbumesFragment : Fragment(R.layout.fragment_albumes) {

    private var _binding: FragmentAlbumesBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AlbumesViewModel by viewModels()

    private lateinit var adapter: AlbumesAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentAlbumesBinding.bind(view)

        configurarRecycler()
        observarAlbumes()
        configurarBotones()
        configurarSwipe()
    }

    // ================================
    // RECYCLER
    // ================================

    private fun configurarRecycler() {

        adapter = AlbumesAdapter(
            items = emptyList(),
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
            },
            onPublicarClick = { album ->
                publicarAlbum(album)
            }
        )

        binding.recyclerAlbumes.layoutManager =
            LinearLayoutManager(requireContext())

        binding.recyclerAlbumes.adapter = adapter
    }


    private fun observarAlbumes() {
        viewModel.albumes.observe(viewLifecycleOwner) { lista ->
            adapter.actualizarLista(lista)
        }
    }


    // ================================
    // PUBLICAR
    // ================================

    private fun publicarAlbum(album: AlbumConCantidad) {

        val liveData = viewModel.obtenerPlantasDelAlbumRaw(album.id)

        liveData.observe(viewLifecycleOwner) { plantas: List<PlantaAlbum> ->

            if (plantas.isEmpty()) {
                Toast.makeText(
                    requireContext(),
                    "El álbum no tiene plantas",
                    Toast.LENGTH_SHORT
                ).show()
                return@observe
            }

            // 🔥 remover observer para que no se acumule
            liveData.removeObservers(viewLifecycleOwner)

            val nombreVivero = "Mi Vivero"

            val uris =
                com.emanuel.mivivero.ui.utils.AlbumPublisher
                    .generarImagenesAlbum(
                        requireContext(),
                        plantas,
                        nombreVivero
                    )

            val intent = Intent(Intent.ACTION_SEND_MULTIPLE).apply {
                type = "image/*"
                putParcelableArrayListExtra(Intent.EXTRA_STREAM, ArrayList(uris))
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            startActivity(Intent.createChooser(intent, "Compartir álbum"))
        }
    }

    // ================================
    // ELIMINAR
    // ================================

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

    private fun eliminarConEfectos(album: AlbumConCantidad) {

        val vibrator =
            requireContext().getSystemService(android.os.Vibrator::class.java)

        vibrator?.let {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {

                val effect = android.os.VibrationEffect.createWaveform(
                    longArrayOf(0, 100, 50, 100),
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
            "Álbum eliminado",
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

    // ================================
    // BOTONES
    // ================================

    private fun configurarBotones() {
        binding.fabCrearAlbum.setOnClickListener {
            findNavController().navigate(
                R.id.action_albumesFragment_to_crearAlbumesFragment
            )
        }
    }

    // ================================
    // SWIPE
    // ================================

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
                    val album = adapter.getItem(position)

                    eliminarConEfectos(album)
                }
            }

        val itemTouchHelper =
            androidx.recyclerview.widget.ItemTouchHelper(itemTouchHelperCallback)

        itemTouchHelper.attachToRecyclerView(binding.recyclerAlbumes)
    }

    // ================================
    // CLEANUP
    // ================================

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
