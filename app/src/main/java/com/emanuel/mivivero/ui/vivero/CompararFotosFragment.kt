package com.emanuel.mivivero.ui.fragment

import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.SeekBar
import androidx.fragment.app.Fragment
import com.emanuel.mivivero.R
import com.emanuel.mivivero.databinding.FragmentCompararFotosBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CompararFotosFragment : Fragment(R.layout.fragment_comparar_fotos) {

    private var _binding: FragmentCompararFotosBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentCompararFotosBinding.bind(view)

        val rutaArriba = arguments?.getString("fotoArriba")
        val fechaArriba = arguments?.getLong("fechaArriba")

        val rutaAbajo = arguments?.getString("fotoAbajo")
        val fechaAbajo = arguments?.getLong("fechaAbajo")

        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

        // 📷 Cargar imágenes
        rutaArriba?.let { binding.imgArriba.setImageURI(Uri.parse(it)) }
        rutaAbajo?.let { binding.imgAbajo.setImageURI(Uri.parse(it)) }
        binding.slider.post {
            binding.slider.progress = 50
        }


        // 🗓️ Fechas
        fechaArriba?.let { binding.txtFechaArriba.text = sdf.format(Date(it)) }
        fechaAbajo?.let { binding.txtFechaAbajo.text = sdf.format(Date(it)) }

        // 🎚️ Slider controla visibilidad de la foto nueva
        binding.slider.setOnSeekBarChangeListener(object :
            SeekBar.OnSeekBarChangeListener {

            override fun onProgressChanged(
                seekBar: SeekBar?,
                progress: Int,
                fromUser: Boolean
            ) {
                // progress: 0..100
                val pesoArriba = progress / 100f
                val pesoAbajo = 1f - pesoArriba

                val paramsArriba =
                    binding.contenedorArriba.layoutParams as LinearLayout.LayoutParams
                val paramsAbajo =
                    binding.contenedorAbajo.layoutParams as LinearLayout.LayoutParams

                paramsArriba.weight = pesoArriba
                paramsAbajo.weight = pesoAbajo

                binding.contenedorArriba.layoutParams = paramsArriba
                binding.contenedorAbajo.layoutParams = paramsAbajo
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })



    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
