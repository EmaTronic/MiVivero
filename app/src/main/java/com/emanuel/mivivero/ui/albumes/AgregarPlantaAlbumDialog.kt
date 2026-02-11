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

        val dialog = AlertDialog.Builder(requireContext())
            .setTitle("${planta.familia} ${planta.especie ?: ""}")
            .setView(binding.root)
            .setPositiveButton("Agregar", null) // 🔥 IMPORTANTE
            .setNegativeButton("Cancelar", null)
            .create()

        dialog.setOnShowListener {

            val btnAgregar = dialog.getButton(AlertDialog.BUTTON_POSITIVE)

            btnAgregar.setOnClickListener {

                val cantidad =
                    binding.etCantidad.text.toString().toIntOrNull() ?: 0
                val precio =
                    binding.etPrecio.text.toString().toDoubleOrNull() ?: 0.0

                viewModel.agregarPlantaAlAlbum(
                    planta = planta,
                    cantidad = cantidad,
                    precio = precio



                ) { error ->

                    if (error != null) {

                        // Error por duplicado
                        if (error.contains("ya está")) {
                            android.widget.Toast.makeText(
                                requireContext(),
                                error,
                                android.widget.Toast.LENGTH_SHORT
                            ).show()
                            return@agregarPlantaAlAlbum
                        }

                        // Error por validación
                        binding.etCantidad.error =
                            if (cantidad <= 0) "Cantidad debe ser > 0" else null

                        binding.etPrecio.error =
                            if (precio <= 0.0) "Precio debe ser > 0" else null

                    } else {

                        android.widget.Toast.makeText(
                            requireContext(),
                            "Planta agregada al álbum",
                            android.widget.Toast.LENGTH_SHORT
                        ).show()

                        dialog.dismiss()
                    }

                }
            }
        }

        return dialog
    }

}
