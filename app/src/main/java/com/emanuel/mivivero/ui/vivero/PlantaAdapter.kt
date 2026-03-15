package com.emanuel.mivivero.ui.vivero

import android.content.Context
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.emanuel.mivivero.R
import com.emanuel.mivivero.data.model.Planta
import java.text.Normalizer

class PlantaAdapter(
    plantas: List<Planta>,

    var queryActual: String = "",
    private val modoAgregarAlbum: Boolean = false,
    private val onAgregarPlantaAlbum: ((Planta) -> Unit)?
) : RecyclerView.Adapter<PlantaAdapter.PlantaViewHolder>() {

    private val plantas = plantas.toMutableList()


    inner class PlantaViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {

        val imgPlanta: ImageView = itemView.findViewById(R.id.imgPlanta)
        val txtNumero: TextView = itemView.findViewById(R.id.txtNumero)
        val txtFamiliaEspecie: TextView =
            itemView.findViewById(R.id.txtFamiliaEspecie)
        val txtCantidad: TextView = itemView.findViewById(R.id.txtCantidad)
        val txtLugarIcono: TextView = itemView.findViewById(R.id.txtLugarIcono)
        val txtVenta: TextView = itemView.findViewById(R.id.txtVenta)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): PlantaViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_planta, parent, false)
        return PlantaViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: PlantaViewHolder,
        position: Int
    ) {
        val planta = plantas[position]

        // 🔢 Número
        holder.txtNumero.text = "N° ${planta.numeroPlanta}"

        // 🌿 Nombre (familia + especie)
        val nombre = "${planta.familia} ${planta.especie ?: ""}".trim()

        holder.txtFamiliaEspecie.text =
            resaltarTexto(nombre, queryActual, holder.itemView.context)




        // 📦 Cantidad
        holder.txtCantidad.text = "Stock: ${planta.cantidad}"

        // 🟠 Stock bajo → naranja
        if (planta.cantidad <= 2) {
            holder.txtCantidad.setTextColor(
                ContextCompat.getColor(
                    holder.itemView.context,
                    R.color.orange_warning
                )
            )
        } else {
            holder.txtCantidad.setTextColor(
                ContextCompat.getColor(
                    holder.itemView.context,
                    R.color.text_primary
                )
            )
        }

        if (!planta.lugarIcono.isNullOrBlank()) {
            holder.txtLugarIcono.visibility = View.VISIBLE
            holder.txtLugarIcono.text = planta.lugarIcono
        } else {
            holder.txtLugarIcono.visibility = View.GONE
        }

        // 💰 Badge venta (solo visible si está en venta)
        if (planta.aLaVenta) {
            holder.txtVenta.visibility = View.VISIBLE
            holder.txtVenta.text = "En venta"
        } else {
            holder.txtVenta.visibility = View.GONE
        }

        // 📷 Foto
        if (!planta.fotoRuta.isNullOrEmpty()) {
            holder.imgPlanta.setImageURI(Uri.parse(planta.fotoRuta))
        } else {
            holder.imgPlanta.setImageResource(R.drawable.ic_planta_placeholder)
        }

        // 👉 Navegación / Modo álbum
        holder.itemView.setOnClickListener {

            if (modoAgregarAlbum && onAgregarPlantaAlbum != null) {
                onAgregarPlantaAlbum.invoke(planta)
            } else {
                val bundle = Bundle().apply {
                    putLong("plantaId", planta.id)
                }

                it.findNavController()
                    .navigate(R.id.plantaDetalleFragment, bundle)
            }
        }
    }

    fun actualizarLista(nuevaLista: List<Planta>) {
        plantas.clear()
        plantas.addAll(nuevaLista)
        notifyDataSetChanged()
    }


    private fun normalizar(texto: String): String {
        val normalized = Normalizer.normalize(texto, Normalizer.Form.NFD)
        return normalized.replace("\\p{InCombiningDiacriticalMarks}+".toRegex(), "")
    }


    private fun resaltarTexto(texto: String, query: String, context: Context): SpannableString {

        val spannable = SpannableString(texto)

        if (query.isBlank()) return spannable

        val textoNormal = normalizar(texto).lowercase()
        val queryNormal = normalizar(query).lowercase()

        var inicio = textoNormal.indexOf(queryNormal)

        while (inicio >= 0) {

            val fin = inicio + queryNormal.length

            spannable.setSpan(
                ForegroundColorSpan(
                    ContextCompat.getColor(context, R.color.teal_primary)
                ),
                inicio,
                fin,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )

            spannable.setSpan(
                StyleSpan(Typeface.BOLD),
                inicio,
                fin,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )

            inicio = textoNormal.indexOf(queryNormal, fin)
        }

        return spannable
    }



    override fun getItemCount(): Int = plantas.size
}
