package com.emanuel.mivivero.ui.albumes

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.fragment.app.DialogFragment
import com.bumptech.glide.Glide
import com.emanuel.mivivero.R

class FotoAmpliadaDialog(
    private val rutaFoto: String,
    private val nombre: String,
    private val fecha: String
) : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val view = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_foto_ampliada, null)

        val img = view.findViewById<ImageView>(R.id.imgFotoGrande)
        val txtNombre = view.findViewById<TextView>(R.id.txtNombrePlanta)
        val txtFecha = view.findViewById<TextView>(R.id.txtFechaFoto)

        txtNombre.text = nombre
        txtFecha.text = fecha

        Glide.with(requireContext())
            .load(rutaFoto)
            .into(img)

        val dialog = Dialog(requireContext())
        dialog.setContentView(view)
        dialog.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )

        return dialog
    }
}