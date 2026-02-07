package com.emanuel.mivivero.ui.albumes

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import com.emanuel.mivivero.data.model.Planta
import com.emanuel.mivivero.databinding.DialogAgregarAlbumBinding

class AgregarPlantaAlbumDialog(
    private val planta: Planta
) : DialogFragment() {

    private val viewModel: AlbumesViewModel by activityViewModels()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val binding = DialogAgregarAlbumBinding.inflate(
            LayoutInflater.from(requireContext())
        )

        return AlertDialog.Builder(requireContext())
            .setTitle(
                "${planta.familia} ${planta.especie ?: ""}"
            )
            .setView(binding.root)
            .setPositiveButton("Agregar") { _, _ ->
                val cantidad =
                    binding.etCantidad.text.toString().toIntOrNull() ?: 0
                val precio =
                    binding.etPrecio.text.toString().toDoubleOrNull() ?: 0.0

                viewModel.agregarPlantaAlAlbum(
                    planta = planta,
                    cantidad = cantidad,
                    precio = precio
                )
            }
            .setNegativeButton("Cancelar", null)
            .create()
    }
}
