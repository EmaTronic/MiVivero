package com.emanuel.mivivero.ui.lugares

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.emanuel.mivivero.data.local.LugarConConteo
import com.emanuel.mivivero.databinding.ItemLugarBinding

class LugaresAdapter(
    private val onEditar: (LugarConConteo) -> Unit,
    private val onEliminar: (LugarConConteo) -> Unit
) : RecyclerView.Adapter<LugaresAdapter.LugarViewHolder>() {

    private val items = mutableListOf<LugarConConteo>()

    inner class LugarViewHolder(private val binding: ItemLugarBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: LugarConConteo) {
            binding.txtIcono.text = item.icono
            binding.txtNombre.text = item.nombre
            binding.txtCantidad.text = "(${item.cantidad})"
            binding.btnEditar.setOnClickListener { onEditar(item) }
            binding.btnEliminar.setOnClickListener { onEliminar(item) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LugarViewHolder {
        val binding = ItemLugarBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return LugarViewHolder(binding)
    }

    override fun onBindViewHolder(holder: LugarViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    fun submitList(nuevaLista: List<LugarConConteo>) {
        items.clear()
        items.addAll(nuevaLista)
        notifyDataSetChanged()
    }
}
