package com.emanuel.mivivero.ui.comunidad

import android.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.emanuel.mivivero.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ReservasAdapter(
    private val lista: MutableList<Map<String, Any>>, // 🔥 AHORA MUTABLE
    private val albumId: String
) : RecyclerView.Adapter<ReservasAdapter.ViewHolder>() {

    private val db = FirebaseFirestore.getInstance()

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val texto: TextView = view.findViewById(R.id.tvReserva)
        val btnEditar: Button = view.findViewById(R.id.btnEditar)
        val btnEliminar: Button = view.findViewById(R.id.btnEliminar)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_reserva, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = lista.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val reserva = lista[position]

        val planta = (reserva["plantaNumero"] as? Long)?.toInt() ?: 0
        val cantidad = (reserva["cantidad"] as? Long)?.toInt() ?: 0
        val usuario = reserva["nickUsuario"] ?: "usuario"

        val uidActual = FirebaseAuth.getInstance().currentUser?.uid
        val uidReserva = reserva["uidUsuario"]

        val unidadTexto = if (cantidad == 1) "unidad" else "unidades"

        holder.texto.text = "Planta $planta\n$cantidad $unidadTexto - $usuario"

        // ======================
        // VISIBILIDAD BOTONES
        // ======================
        if (uidActual == uidReserva) {
            holder.btnEditar.visibility = View.VISIBLE
            holder.btnEliminar.visibility = View.VISIBLE
        } else {
            holder.btnEditar.visibility = View.GONE
            holder.btnEliminar.visibility = View.GONE
        }

        val id = reserva["id"] as String

        // ======================
        // ELIMINAR
        // ======================
        holder.btnEliminar.setOnClickListener {
            db.collection("albumsFeed")
                .document(albumId)
                .collection("reservas")
                .document(id)
                .delete()
        }

        // ======================
        // EDITAR
        // ======================
        holder.btnEditar.setOnClickListener {

            val context = holder.itemView.context

            val layout = LinearLayout(context)
            layout.orientation = LinearLayout.VERTICAL

            val etPlanta = EditText(context)
            etPlanta.hint = "Planta"
            etPlanta.setText(planta.toString())

            val etCantidad = EditText(context)
            etCantidad.hint = "Cantidad"
            etCantidad.setText(cantidad.toString())

            layout.addView(etPlanta)
            layout.addView(etCantidad)

            AlertDialog.Builder(context)
                .setTitle("Editar reserva")
                .setView(layout)
                .setPositiveButton("Guardar") { _, _ ->

                    val nuevaPlanta = etPlanta.text.toString().toIntOrNull()
                    val nuevaCantidad = etCantidad.text.toString().toIntOrNull()

                    if (nuevaPlanta != null && nuevaCantidad != null) {

                        db.collection("albumsFeed")
                            .document(albumId)
                            .collection("reservas")
                            .document(id)
                            .update(
                                mapOf(
                                    "plantaNumero" to nuevaPlanta,
                                    "cantidad" to nuevaCantidad
                                )
                            )
                    }
                }
                .setNegativeButton("Cancelar", null)
                .show()
        }
    }

    // =====================================
    // 🔥 ACTUALIZAR LISTA (CLAVE)
    // =====================================
    fun actualizarLista(nuevaLista: List<Map<String, Any>>) {
        lista.clear()
        lista.addAll(nuevaLista)
        notifyDataSetChanged()
    }
}