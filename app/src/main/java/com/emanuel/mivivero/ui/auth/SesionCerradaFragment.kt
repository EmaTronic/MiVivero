package com.emanuel.mivivero.ui.auth

import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.fragment.app.Fragment
import com.emanuel.mivivero.R
import androidx.navigation.fragment.findNavController

class SesionCerradaFragment : Fragment(R.layout.fragment_sesion_cerrada) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val btn = view.findViewById<Button>(R.id.btnVolverLogin)

        btn.setOnClickListener {
            findNavController().navigate(R.id.loginFragment)
        }
    }
}
