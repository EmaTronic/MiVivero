package com.emanuel.mivivero.ui.vivero
import android.net.Uri
import android.util.Log
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
    private val rutaFotoPrincipal: String?,
    private val esSeleccionable: () -> Boolean,
    private val estaSeleccionada: (FotoPlanta) -> Boolean,
    private val onClickFoto: (FotoPlanta) -> Unit,
    private val onLongClickFoto: (FotoPlanta) -> Unit
) : RecyclerView.Adapter<FotoAdapter.FotoViewHolder>() {

    inner class FotoViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val img: ImageView = view.findViewById(R.id.imgFoto)
        val txtFecha: TextView = view.findViewById(R.id.txtFecha)
        val overlay: View = view.findViewById(R.id.overlaySeleccion)
        val txtPrincipal: TextView = view.findViewById(R.id.txtPrincipal)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FotoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_foto, parent, false)
        return FotoViewHolder(view)
    }

    override fun onBindViewHolder(holder: FotoViewHolder, position: Int) {
        val foto = fotos[position]

        // Imagen
        holder.img.setImageURI(Uri.parse(foto.ruta))

        // Fecha
        holder.txtFecha.text =
            SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                .format(Date(foto.fecha))

        // Selección
        holder.overlay.visibility =
            if (estaSeleccionada(foto)) View.VISIBLE else View.GONE

        // Badge PRINCIPAL (comparación robusta de URI)
        if (
            rutaFotoPrincipal != null &&
            foto.ruta.startsWith(rutaFotoPrincipal)
        ) {
            holder.txtPrincipal.visibility = View.VISIBLE
        } else {
            holder.txtPrincipal.visibility = View.GONE
        }

        // Click corto: seleccionar
        holder.itemView.setOnClickListener {
            if (esSeleccionable()) {
                onClickFoto(foto)
                notifyItemChanged(position)
            }
        }

        // Click largo: cambiar foto principal
        holder.itemView.setOnLongClickListener {
            Log.d("LONG_CLICK_TEST", "Long click detectado en foto: ${foto.ruta}")
            onLongClickFoto(foto)
            true
        }
    }

    override fun getItemCount(): Int = fotos.size
}
