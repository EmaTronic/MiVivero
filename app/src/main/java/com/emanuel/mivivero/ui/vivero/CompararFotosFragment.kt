package com.emanuel.mivivero.ui.fragment

import android.net.Uri
import android.os.Bundle
import android.view.View
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

        rutaArriba?.let {
            binding.imgArriba.setImageURI(Uri.parse(it))
        }
        fechaArriba?.let {
            binding.txtFechaArriba.text = sdf.format(Date(it))
        }

        rutaAbajo?.let {
            binding.imgAbajo.setImageURI(Uri.parse(it))
        }
        fechaAbajo?.let {
            binding.txtFechaAbajo.text = sdf.format(Date(it))
        }
    }




    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
