package com.emanuel.mivivero.ui.lugares

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import com.emanuel.mivivero.data.local.LugarConConteo
import com.emanuel.mivivero.databinding.DialogAgregarLugarBinding
import com.emanuel.mivivero.ui.vivero.ViveroViewModel
import com.google.android.material.chip.Chip

class AgregarLugarDialog : DialogFragment() {

    private val viewModel: ViveroViewModel by activityViewModels()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val binding = DialogAgregarLugarBinding.inflate(LayoutInflater.from(context))

        val lugarId = arguments?.getInt(ARG_LUGAR_ID)?.takeIf { it > 0 }
        val nombre = arguments?.getString(ARG_LUGAR_NOMBRE).orEmpty()
        val icono = arguments?.getString(ARG_LUGAR_ICONO).orEmpty()

        binding.etNombreLugar.setText(nombre)

        ICONOS_DISPONIBLES.forEach { opcion ->
            val chip = Chip(requireContext()).apply {
                id = android.view.View.generateViewId()
                text = opcion
                isCheckable = true
                isChecked = if (icono.isBlank()) opcion == ICONO_DEFAULT else opcion == icono
            }
            binding.chipIconos.addView(chip)
        }

        return AlertDialog.Builder(requireContext())
            .setTitle(if (lugarId == null) "Agregar lugar" else "Editar lugar")
            .setView(binding.root)
            .setPositiveButton("Guardar") { _, _ ->
                val idChip = binding.chipIconos.checkedChipId
                val iconoSeleccionado = if (idChip == -1) {
                    ICONO_DEFAULT
                } else {
                    binding.root.findViewById<Chip>(idChip).text.toString()
                }

                viewModel.guardarLugar(
                    nombre = binding.etNombreLugar.text.toString(),
                    icono = iconoSeleccionado,
                    lugarId = lugarId
                )
            }
            .setNegativeButton("Cancelar", null)
            .create()
    }

    companion object {
        private const val ARG_LUGAR_ID = "arg_lugar_id"
        private const val ARG_LUGAR_NOMBRE = "arg_lugar_nombre"
        private const val ARG_LUGAR_ICONO = "arg_lugar_icono"

        private const val ICONO_DEFAULT = "🪴"
        private val ICONOS_DISPONIBLES = listOf("🏡", "🌿", "🌱", "🌳", "🌵", "🪴")

        fun nuevaInstancia(lugar: LugarConConteo? = null): AgregarLugarDialog {
            return AgregarLugarDialog().apply {
                arguments = Bundle().apply {
                    putInt(ARG_LUGAR_ID, lugar?.id ?: -1)
                    putString(ARG_LUGAR_NOMBRE, lugar?.nombre)
                    putString(ARG_LUGAR_ICONO, lugar?.icono)
                }
            }
        }
    }
}
