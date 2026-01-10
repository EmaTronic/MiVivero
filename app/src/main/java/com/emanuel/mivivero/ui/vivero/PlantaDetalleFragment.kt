package com.emanuel.mivivero.ui.vivero

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.emanuel.mivivero.R
import com.emanuel.mivivero.data.repository.PlantaRepository

class PlantaDetalleFragment : Fragment(R.layout.fragment_planta_detalle) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val plantaId = arguments?.getLong("plantaId") ?: return

        val repository = PlantaRepository()
        val planta = repository.getPlantaById(plantaId) ?: return

        view.findViewById<TextView>(R.id.txtDetalleNumero).text =
            planta.numeroPlanta

        view.findViewById<TextView>(R.id.txtDetalleFamilia).text =
            "Familia: ${planta.familia}"

        view.findViewById<TextView>(R.id.txtDetalleEspecie).text =
            planta.especie ?: "Especie: Sin identificar"

        view.findViewById<TextView>(R.id.txtDetalleCantidad).text =
            "Cantidad: ${planta.cantidad}"


    }

}
