package com.emanuel.mivivero.ui.albumes

import android.app.DatePickerDialog
import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import androidx.fragment.app.DialogFragment
import com.emanuel.mivivero.databinding.DialogPublicarAlbumBinding
import com.emanuel.mivivero.ui.utils.AccordionHelper
import java.util.*

class PublicarAlbumDialog(
    private val onConfirmar: (
        fondo: Int,
        nombreVivero: String?,
        fechaHasta: Long?,
        pago: String?,
        envio: String?,
        retiro: String?,
        observaciones: String?
    ) -> Unit
) : DialogFragment() {

    private var fechaSeleccionada: Long? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        val binding = DialogPublicarAlbumBinding.inflate(
            LayoutInflater.from(context)
        )

        val dialog = androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setView(binding.root)
            .create()

        // =============================
        // ACCORDION SETUP
        // =============================

        AccordionHelper.setupSingleAccordion(
            parent = binding.containerAccordion,
            sections = listOf(
                AccordionHelper.Section(
                    binding.headerInfo,
                    binding.contentInfo,
                    binding.arrowInfo
                ),
                AccordionHelper.Section(
                    binding.headerCondiciones,
                    binding.contentCondiciones,
                    binding.arrowCondiciones
                ),
                AccordionHelper.Section(
                    binding.headerDiseno,
                    binding.contentDiseno,
                    binding.arrowDiseno
                )
            ),
            initiallyOpenIndex = 0
        )

        // =============================
        // DATE PICKER
        // =============================

        binding.btnFecha.setOnClickListener {

            val calendario = Calendar.getInstance()

            DatePickerDialog(
                requireContext(),
                { _, year, month, day ->
                    val cal = Calendar.getInstance()
                    cal.set(year, month, day)
                    fechaSeleccionada = cal.timeInMillis
                    binding.btnFecha.text = "$day/${month + 1}/$year"
                },
                calendario.get(Calendar.YEAR),
                calendario.get(Calendar.MONTH),
                calendario.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        // =============================
        // BOTÓN PUBLICAR
        // =============================

        binding.btnPublicarFinal.setOnClickListener {

            val fondo = when (binding.rgFondos.checkedRadioButtonId) {
                binding.rbCactus.id -> 1
                binding.rbTropical.id -> 2
                else -> 3
            }

            onConfirmar(
                fondo,
                if (binding.cbNombreVivero.isChecked)
                    binding.etNombreVivero.text.toString()
                else null,

                if (binding.cbFechaHasta.isChecked)
                    fechaSeleccionada
                else null,

                if (binding.cbPago.isChecked)
                    binding.etPago.text.toString()
                else null,

                if (binding.cbEnvio.isChecked)
                    binding.etEnvio.text.toString()
                else null,

                if (binding.cbRetiro.isChecked)
                    binding.etRetiro.text.toString()
                else null,

                if (binding.cbObservaciones.isChecked)
                    binding.etObservaciones.text.toString()
                else null
            )

            dialog.dismiss()
        }

        return dialog
    }
}
