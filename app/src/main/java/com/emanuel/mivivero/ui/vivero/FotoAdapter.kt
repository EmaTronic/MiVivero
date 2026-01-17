package com.emanuel.mivivero.ui.vivero

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.PopupMenu
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.emanuel.mivivero.R
import com.emanuel.mivivero.data.db.entity.FotoEntity

class FotoAdapter(
    private val onAccionFoto: (FotoEntity, AccionFoto) -> Unit
) : RecyclerView.Adapter<FotoAdapter.FotoViewHolder>() {

    private val fotos = mutableListOf<FotoEntity>()

    fun submitList(nuevaLista: List<FotoEntity>) {
        fotos.clear()
        fotos.addAll(nuevaLista)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FotoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_foto, parent, false)
        return FotoViewHolder(view)
    }

    override fun onBindViewHolder(holder: FotoViewHolder, position: Int) {
        val foto = fotos[position]

        Glide.with(holder.itemView)
            .load(foto.rutaLocal)
            .into(holder.image)

        // mostrar marco si es principal
        holder.marco.visibility =
            if (foto.esPrincipal) View.VISIBLE else View.GONE

        holder.image.setOnClickListener {
            mostrarMenu(it, foto)
        }
    }

    override fun getItemCount(): Int = fotos.size

    private fun mostrarMenu(view: View, foto: FotoEntity) {
        val popup = PopupMenu(view.context, view)
        popup.inflate(R.menu.menu_foto)

        popup.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_principal ->
                    onAccionFoto(foto, AccionFoto.PRINCIPAL)

                R.id.action_borrar ->
                    onAccionFoto(foto, AccionFoto.BORRAR)

                R.id.action_comparar ->
                    onAccionFoto(foto, AccionFoto.COMPARAR)
            }
            true
        }

        popup.show()
    }

    class FotoViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val image: ImageView = view.findViewById(R.id.imgFoto)
        val marco: View = view.findViewById(R.id.marcoPrincipal)
    }
}

enum class AccionFoto {
    PRINCIPAL,
    BORRAR,
    COMPARAR
}
