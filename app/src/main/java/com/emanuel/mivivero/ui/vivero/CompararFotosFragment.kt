package com.emanuel.mivivero.ui.fragment

import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.emanuel.mivivero.R
import com.emanuel.mivivero.databinding.FragmentCompararFotosBinding

class CompararFotosFragment : Fragment(R.layout.fragment_comparar_fotos) {

    private var _binding: FragmentCompararFotosBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentCompararFotosBinding.bind(view)

        val arriba = arguments?.getString("fotoArriba")
        val abajo = arguments?.getString("fotoAbajo")

        arriba?.let {
            binding.imgArriba.setImageURI(Uri.parse(it))
        }

        abajo?.let {
            binding.imgAbajo.setImageURI(Uri.parse(it))
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
