package com.emanuel.mivivero.ui.albumes

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.emanuel.mivivero.data.model.AlbumConCantidad
import com.emanuel.mivivero.databinding.ItemAlbumBinding
import com.emanuel.mivivero.R

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
class AlbumesAdapter(
    private var items: List<AlbumConCantidad>,
    private val onClick: (AlbumConCantidad) -> Unit,
    private val onDeleteClick: (AlbumConCantidad) -> Unit,
    private val onPublicarClick: (AlbumConCantidad) -> Unit
) : RecyclerView.Adapter<AlbumesAdapter.VH>() {

    inner class VH(val b: ItemAlbumBinding) :
        RecyclerView.ViewHolder(b.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val binding = ItemAlbumBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return VH(binding)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val album = items[position]

        holder.b.txtNombreAlbum.text = album.nombre
        holder.b.txtObservaciones.text =
            album.observaciones ?: "Sin observaciones"

        holder.b.txtCantidadPlantas.text =
            "Plantas: ${album.cantidadPlantas}"

        holder.b.btnEliminarAlbum.setOnClickListener {
            onDeleteClick(album)
        }

        holder.itemView.setOnClickListener {
            onClick(album)
        }

        holder.b.txtEstadoAlbum.text = album.estado

        when (album.estado) {
            "BORRADOR" -> holder.b.txtEstadoAlbum.setBackgroundResource(R.drawable.bg_estado_borrador)
            "FINALIZADO" -> holder.b.txtEstadoAlbum.setBackgroundResource(R.drawable.bg_estado_finalizado)
            "PUBLICADO" -> holder.b.txtEstadoAlbum.setBackgroundResource(R.drawable.bg_estado_publicado)
        }

        holder.b.btnPublicar.visibility =
            if (album.estado == "FINALIZADO") View.VISIBLE else View.GONE

        holder.b.btnPublicar.setOnClickListener {
            onPublicarClick(album)
        }
    }

    override fun getItemCount(): Int = items.size

    fun actualizarLista(nuevaLista: List<AlbumConCantidad>) {
        items = nuevaLista
        notifyDataSetChanged()
    }

    fun getItem(position: Int): AlbumConCantidad {
        return items[position]
    }
}
