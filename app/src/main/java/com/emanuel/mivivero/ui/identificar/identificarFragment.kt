package com.emanuel.mivivero.ui.identificar

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.emanuel.mivivero.R


class IdentificarFragment : Fragment() {

    private lateinit var imgPreview: ImageView
    private lateinit var btnCamara: Button
    private lateinit var btnGaleria: Button
    private lateinit var tvResultado: TextView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_identificar, container, false)

        imgPreview = view.findViewById(R.id.imgPreview)
        btnCamara = view.findViewById(R.id.btnCamara)
        btnGaleria = view.findViewById(R.id.btnGaleria)
        tvResultado = view.findViewById(R.id.tvResultado)

        btnCamara.setOnClickListener {
            tvResultado.text = "Función cámara pendiente de implementar"
        }

        btnGaleria.setOnClickListener {
            tvResultado.text = "Función galería pendiente de implementar"
        }

        return view
    }
}