package com.emanuel.mivivero.ui.vivero

import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.emanuel.mivivero.R
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ComparacionFragment : Fragment(R.layout.fragment_comparacion) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // ===== VALIDAR ARGUMENTOS =====
        val args = arguments ?: run {
            findNavController().navigateUp()
            return
        }

        val uriAntes = args.getString("uriAntes")
        val uriDespues = args.getString("uriDespues")
        val fechaAntes = args.getLong("fechaAntes", 0L)
        val fechaDespues = args.getLong("fechaDespues", 0L)

        if (uriAntes == null || uriDespues == null) {
            findNavController().navigateUp()
            return
        }

        // ===== VIEWS =====
        val imgAntes = view.findViewById<ImageView>(R.id.imgAntes)
        val imgDespues = view.findViewById<ImageView>(R.id.imgDespues)
        val slider = view.findViewById<SeekBar>(R.id.sliderComparacion)
        val txtAntes = view.findViewById<TextView>(R.id.txtAntes)
        val txtDespues = view.findViewById<TextView>(R.id.txtDespues)
        val btnVolver = view.findViewById<Button>(R.id.btnVolver)

        // ===== CARGAR IMÁGENES (ROBUSTO) =====
        Glide.with(this)
            .load(uriAntes)
            .into(imgAntes)

        Glide.with(this)
            .load(uriDespues)
            .into(imgDespues)

        // ===== FECHAS =====
        val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

        if (fechaAntes > 0) {
            txtAntes.text = "Antes (${formatter.format(Date(fechaAntes))})"
        } else {
            txtAntes.text = "Antes"
        }

        if (fechaDespues > 0) {
            txtDespues.text = "Después (${formatter.format(Date(fechaDespues))})"
        } else {
            txtDespues.text = "Después"
        }

        // ===== SLIDER =====
        slider.max = 100
        slider.progress = 50

        slider.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val alpha = progress / 100f
                imgDespues.alpha = alpha
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        // ===== VOLVER =====
        btnVolver.setOnClickListener {
            findNavController().navigateUp()
        }
    }
}
