package com.emanuel.mivivero.ui.adapter

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.emanuel.mivivero.R
import com.emanuel.mivivero.data.model.FotoPlanta
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class FotoAdapter(
    private val fotos: List<FotoPlanta>,
    private val esSeleccionable: () -> Boolean,
    private val estaSeleccionada: (FotoPlanta) -> Boolean,
    private val onClickFoto: (FotoPlanta) -> Unit
) : RecyclerView.Adapter<FotoAdapter.FotoViewHolder>() {

    inner class FotoViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val img: ImageView = view.findViewById(R.id.imgFoto)
        val txtFecha: TextView = view.findViewById(R.id.txtFecha)
        val overlay: View = view.findViewById(R.id.overlaySeleccion)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FotoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_foto, parent, false)
        return FotoViewHolder(view)
    }

    override fun onBindViewHolder(holder: FotoViewHolder, position: Int) {
        val foto = fotos[position]

        holder.img.setImageURI(Uri.parse(foto.ruta))
        holder.txtFecha.text =
            SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                .format(Date(foto.fecha))

        holder.overlay.visibility =
            if (estaSeleccionada(foto)) View.VISIBLE else View.GONE

        holder.itemView.setOnClickListener {
            if (esSeleccionable()) {
                onClickFoto(foto)
                notifyItemChanged(position)
            }
        }
    }

    override fun getItemCount(): Int = fotos.size
}
