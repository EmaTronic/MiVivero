package com.emanuel.mivivero.ui.vivero

import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.emanuel.mivivero.R
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.navigation.fragment.findNavController


class ComparacionFragment : Fragment(R.layout.fragment_comparacion) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // ===== ARGUMENTOS =====
        val uriAntes = requireArguments().getString("uriAntes")!!
        val uriDespues = requireArguments().getString("uriDespues")!!
        val fechaAntes = requireArguments().getLong("fechaAntes")
        val fechaDespues = requireArguments().getLong("fechaDespues")

        // ===== VIEWS =====
        val imgAntes = view.findViewById<ImageView>(R.id.imgAntes)
        val imgDespues = view.findViewById<ImageView>(R.id.imgDespues)
        val slider = view.findViewById<SeekBar>(R.id.sliderComparacion)
        val txtAntes = view.findViewById<TextView>(R.id.txtAntes)
        val txtDespues = view.findViewById<TextView>(R.id.txtDespues)

        // ===== CARGA DE IMÁGENES =====
        imgAntes.setImageURI(Uri.parse(uriAntes))
        imgDespues.setImageURI(Uri.parse(uriDespues))

        // ===== TEXTOS =====
        txtAntes.text = "ANTES · ${formatearFecha(fechaAntes)}"
        txtDespues.text = "DESPUÉS · ${formatearFecha(fechaDespues)}"

        // ===== SLIDER =====
        slider.max = 100
        slider.progress = 100

        slider.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(
                seekBar: SeekBar?,
                value: Int,
                fromUser: Boolean
            ) {
                // La foto "después" aparece progresivamente
                imgDespues.alpha = value / 100f
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        view.findViewById<Button>(R.id.btnVolver).setOnClickListener {
            findNavController().navigateUp()
        }

    }

    // ===== UTILIDAD =====
    private fun formatearFecha(millis: Long): String {
        return SimpleDateFormat(
            "dd/MM/yyyy",
            Locale.getDefault()
        ).format(Date(millis))
    }
}
