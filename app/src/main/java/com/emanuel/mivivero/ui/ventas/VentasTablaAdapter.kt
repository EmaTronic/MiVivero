package com.emanuel.mivivero.ui.ventas

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.emanuel.mivivero.R
import com.emanuel.mivivero.data.db.entity.VentaDetalle

class VentasTablaAdapter(
    private val onEditar: (VentaDetalle) -> Unit,
    private val onEliminar: (VentaDetalle) -> Unit,
    private val onAgregarClick: () -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>(){

    private val TYPE_VENTA = 0
    private val TYPE_ADD = 1
    private var lista: List<VentaDetalle> = emptyList()

    inner class VH(view: View) : RecyclerView.ViewHolder(view) {
        val tvNombre: TextView = view.findViewById(R.id.tvNombre)
        val tvCantidad: TextView = view.findViewById(R.id.tvCantidad)
        val tvFecha: TextView = view.findViewById(R.id.tvFecha)
        val tvTotal: TextView = view.findViewById(R.id.tvTotal)

        val btnEditar: ImageButton = view.findViewById(R.id.btnEditar)
        val btnEliminar: ImageButton = view.findViewById(R.id.btnEliminar)
    }

    inner class AddVH(view: View) : RecyclerView.ViewHolder(view) {
        val btnAdd: TextView = view.findViewById(R.id.btnAgregar)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        return if (viewType == TYPE_ADD) {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_agregar_venta, parent, false)
            AddVH(view)
        } else {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_venta_tabla, parent, false)
            VH(view)
        }
    }

    override fun getItemCount() = lista.size + 1

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        android.util.Log.e("ADAPTER", "BIND position=$position size=${lista.size}")

        if (holder is AddVH) {

            holder.btnAdd.setOnClickListener {
                onAgregarClick()
            }

        } else if (holder is VH) {

            val item = lista[position]

            holder.tvNombre.text = item.nombrePlanta
            holder.tvCantidad.text = "x${item.cantidad}"

            holder.tvFecha.text = android.text.format.DateFormat
                .format("dd/MM/yyyy", item.fecha)

            holder.tvTotal.text = "$ ${item.total}"

            holder.btnEditar.setOnClickListener {
                Log.e("EDITAR_CLICK", "CLICK EN ${item.nombrePlanta}")
                onEditar(item)
            }


            holder.btnEliminar.setOnClickListener { onEliminar(item) }
        }
    }

    fun submitList(nueva: List<VentaDetalle>) {
        lista = nueva
        notifyDataSetChanged()
    }

    override fun getItemViewType(position: Int): Int {
        return if (position == lista.size) TYPE_ADD else TYPE_VENTA
    }
}