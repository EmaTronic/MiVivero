package com.emanuel.mivivero.ui.fragment

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.emanuel.mivivero.R

class PlantaDetalleFragment : Fragment(R.layout.fragment_planta_detalle) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val txtDetalle = view.findViewById<TextView>(R.id.txtDetalle)

        val plantaId = arguments?.getLong("plantaId") ?: -1

        txtDetalle.text = "Detalle de la planta ID: $plantaId"
    }
}
