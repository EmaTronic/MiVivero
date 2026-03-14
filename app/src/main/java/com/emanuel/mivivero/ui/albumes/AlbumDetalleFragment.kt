package com.emanuel.mivivero.ui.albumes

import android.animation.ValueAnimator
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.emanuel.mivivero.R
import com.emanuel.mivivero.data.model.EstadoAlbum
import com.emanuel.mivivero.data.model.PlantaAlbum
import com.emanuel.mivivero.databinding.DialogAgregarAlbumBinding
import com.emanuel.mivivero.databinding.FragmentAlbumDetalleBinding
import kotlinx.coroutines.launch

class AlbumDetalleFragment : Fragment(R.layout.fragment_album_detalle) {

    private var _binding: FragmentAlbumDetalleBinding? = null
    private val binding get() = _binding!!

    private val viewModel: EditarAlbumViewModel by viewModels()

    private var albumId: Long = -1L

    private lateinit var adapter: AlbumesAdapter




    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        _binding = FragmentAlbumDetalleBinding.bind(view)

        val albumId = requireArguments().getLong("albumId")

        // 1️⃣ LISTENER (una sola vez)
        binding.btnFinalizarAlbum.setOnClickListener {

            viewModel.finalizarAlbum(albumId) { resultado ->

                if (!resultado.esValido) {
                    Toast.makeText(
                        requireContext(),
                        resultado.mensaje,
                        Toast.LENGTH_LONG
                    ).show()
                    return@finalizarAlbum
                }

                lifecycleScope.launch {

                    val plantas =
                        viewModel.obtenerPlantasDelAlbum(albumId).value ?: emptyList()

                    val portadaUri =
                        com.emanuel.mivivero.ui.utils.AlbumPublisher.generarPortadaAlbum(
                            requireContext(),
                            albumId,
                            binding.txtNombreAlbum.text.toString(),
                            1,
                            null,
                            null,
                            null,
                            null,
                            null,
                            null
                        )

                    val fotosUris =
                        com.emanuel.mivivero.ui.utils.AlbumPublisher.generarImagenesAlbum(
                            requireContext(),
                            plantas,
                            binding.txtNombreAlbum.text.toString()
                        )

                    com.emanuel.mivivero.ui.utils.FirebaseAlbumPublisher.publicarAlbum(
                        albumId = albumId.toString(),
                        titulo = binding.txtNombreAlbum.text.toString(),
                        portadaUri = portadaUri,
                        fotos = fotosUris
                    )

                    Toast.makeText(
                        requireContext(),
                        "Álbum publicado en comunidad",
                        Toast.LENGTH_LONG
                    ).show()

                    findNavController().popBackStack()
                }
            }
        }

        binding.btnEditarAlbum.setOnClickListener {
            findNavController().navigate(
                R.id.action_albumDetalleFragment_to_editarAlbumFragment,
                bundleOf("albumId" to albumId)
            )
        }

        // 🔹 Cargar plantas del álbum en Detalle
        binding.recyclerPlantasAlbum.layoutManager =
            GridLayoutManager(requireContext(), 3)

        viewModel.obtenerPlantasDelAlbum(albumId)
            .observe(viewLifecycleOwner) { lista ->

                binding.recyclerPlantasAlbum.adapter =
                    PlantasAlbumAdapter(
                        items = lista,
                        onAgregarClick = { },
                        onItemClick = { },
                        onItemLongClick = { planta ->
                            mostrarOpcionesPlanta(planta)
                        },
                        esEditable = false
                    )

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
        viewModel.obtenerAlbum(albumId)
            .observe(viewLifecycleOwner) { album ->

                if (album == null) return@observe

                val estado = EstadoAlbum.valueOf(album.estado)

                binding.txtNombreAlbum.text = album.nombre
                configurarEstadoUI(estado)

                when (estado) {

                    EstadoAlbum.BORRADOR -> {
                        binding.btnFinalizarAlbum.visibility = View.VISIBLE
                        binding.btnEditarAlbum.visibility = View.VISIBLE
                    }

                    EstadoAlbum.FINALIZADO -> {
                        binding.btnFinalizarAlbum.visibility = View.GONE
                        binding.btnEditarAlbum.visibility = View.VISIBLE
                    }

                    EstadoAlbum.PUBLICADO -> {
                        binding.btnFinalizarAlbum.visibility = View.GONE
                        binding.btnEditarAlbum.visibility = View.GONE
                    }
                }
            }
    }




    private fun confirmarEliminarPlanta(planta: PlantaAlbum) {

        AlertDialog.Builder(requireContext())
            .setTitle("Eliminar planta")
            .setMessage("¿Eliminar ${planta.nombreCompleto} del álbum?")
            .setPositiveButton("Eliminar") { _, _ ->

                viewModel.eliminarPlantaDelAlbum(
                    albumId = albumId,
                    plantaId = planta.plantaId
                )
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
            .setTitle("Editar ${planta.nombreCompleto}")
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
            .setNeutralButton("Eliminar") { _, _ ->
                viewModel.eliminarPlantaDelAlbum(
                    albumId = albumId,
                    plantaId = planta.plantaId
                )
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

        val inputCantidad = android.widget.EditText(requireContext())
        inputCantidad.hint = "Cantidad"

        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Reservar ${planta.nombreCompleto}")
            .setView(inputCantidad)
            .setPositiveButton("Reservar") { _, _ ->

                val cantidad = inputCantidad.text.toString().toIntOrNull()

                if (cantidad == null || cantidad <= 0) {
                    Toast.makeText(
                        requireContext(),
                        "Cantidad inválida",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@setPositiveButton
                }

                val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()

                val reserva = hashMapOf(

                    "uidAutor" to com.google.firebase.auth.FirebaseAuth
                        .getInstance().uid,

                    "emailAutor" to com.google.firebase.auth.FirebaseAuth
                        .getInstance().currentUser?.email,

                    "tipo" to "reserva",

                    "plantaIndex" to planta.plantaId,

                    "cantidad" to cantidad,

                    "texto" to "Reservo $cantidad unidades de la planta ${planta.plantaId}",

                    "estado" to "pendiente",

                    "fecha" to com.google.firebase.firestore.FieldValue.serverTimestamp()
                )

                db.collection("albumsFeed")
                    .document(albumId.toString())
                    .collection("comentarios")
                    .add(reserva)

                Toast.makeText(
                    requireContext(),
                    "Reserva enviada",
                    Toast.LENGTH_SHORT
                ).show()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }


    data class Quad(
        val texto: String,
        val fondo: Int,
        val textoColor: Int,
        val icono: Int
    )

    private fun configurarEstadoUI(estado: EstadoAlbum) {

        val chip = binding.txtEstadoAlbum

        val (texto, colorFondo, colorTexto, icono) = when (estado) {

            EstadoAlbum.BORRADOR -> Quad(
                "BORRADOR",
                requireContext().getColor(R.color.orange_200),
                requireContext().getColor(R.color.orange_700),
                R.drawable.ic_borrador
            )

            EstadoAlbum.FINALIZADO -> Quad(
                "FINALIZADO",
                requireContext().getColor(R.color.blue_200),
                requireContext().getColor(R.color.blue_700),
                R.drawable.ic_finalizado
            )

            EstadoAlbum.PUBLICADO -> Quad(
                "PUBLICADO",
                requireContext().getColor(R.color.green_200),
                requireContext().getColor(R.color.green_700),
                R.drawable.ic_publicado
            )
        }

        chip.text = texto
        chip.setTextColor(colorTexto)
        chip.setChipIconResource(icono)

        chip.chipBackgroundColor = ColorStateList.valueOf(colorFondo)

        chip.animate()
            .scaleX(0.9f)
            .scaleY(0.9f)
            .setDuration(100)
            .withEndAction {
                chip.animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(150)
                    .start()
            }
            .start()

        if (estado == EstadoAlbum.PUBLICADO) {
            chip.postDelayed({
                chip.animate()
                    .scaleX(1.08f)
                    .scaleY(1.08f)
                    .setDuration(100)
                    .withEndAction {
                        chip.animate()
                            .scaleX(1f)
                            .scaleY(1f)
                            .setDuration(100)
                            .start()
                    }
                    .start()
            }, 150)
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
