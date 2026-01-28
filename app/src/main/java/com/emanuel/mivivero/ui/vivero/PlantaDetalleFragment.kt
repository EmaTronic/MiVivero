package com.emanuel.mivivero.ui.fragment

import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.emanuel.mivivero.R
import com.emanuel.mivivero.databinding.FragmentPlantaDetalleBinding
import com.emanuel.mivivero.ui.viewmodel.ViveroViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class PlantaDetalleFragment : Fragment(R.layout.fragment_planta_detalle) {

    private var _binding: FragmentPlantaDetalleBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ViveroViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentPlantaDetalleBinding.bind(view)

        val plantaId = arguments?.getLong("plantaId") ?: return

        // 👇 OBSERVAMOS, NO LEEMOS DIRECTO
        viewModel.plantas.observe(viewLifecycleOwner) { lista ->

            val planta = lista.find { it.id == plantaId } ?: return@observe

            // FOTO
            if (planta.fotoRuta != null) {
                binding.imgDetallePlanta.visibility = View.VISIBLE
                binding.imgDetallePlanta.setImageURI(Uri.parse(planta.fotoRuta))
            }

            // DATOS
            binding.txtFamilia.text = planta.familia
            binding.txtEspecie.text = planta.especie ?: "Sin especie"
            binding.txtCantidad.text = "Cantidad: ${planta.cantidad}"
            binding.txtVenta.text =
                if (planta.aLaVenta) "Disponible para la venta"
                else "No disponible"

            // FECHA FOTO (solo detalle)
            binding.txtFechaFoto.text =
                if (planta.fechaFoto != null) {
                    val formato = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                    "Foto tomada el ${formato.format(Date(planta.fechaFoto))}"
                } else {
                    "Sin foto"
                }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
