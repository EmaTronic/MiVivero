package com.emanuel.mivivero.ui.notificaciones

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.emanuel.mivivero.R
import com.emanuel.mivivero.data.model.Notificacion
import com.google.firebase.firestore.FirebaseFirestore

class NotificacionesAdapter(
    private val lista: MutableList<Notificacion>,
    private val uid: String,
    private val onClick: (Notificacion) -> Unit
) : RecyclerView.Adapter<NotificacionesAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvMensaje: TextView = view.findViewById(R.id.tvMensaje)
        val tvFecha: TextView = view.findViewById(R.id.tvFecha)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_notificacion, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount() = lista.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        //Log.d("ADAPTER_DEBUG", "mensaje=${notif.mensaje}")

        val notif = lista[position]



        val punto = holder.itemView.findViewById<View>(R.id.puntoNoLeido)

        if (!notif.leido) {
            holder.tvMensaje.setTypeface(null, android.graphics.Typeface.BOLD)
            punto.visibility = View.VISIBLE
        } else {
            holder.tvMensaje.setTypeface(null, android.graphics.Typeface.NORMAL)
            punto.visibility = View.GONE
        }



        if (!notif.leido) {
            holder.itemView.setBackgroundColor(0xFFE3F2FD.toInt()) // celeste claro
        } else {
            holder.itemView.setBackgroundColor(0xFFFFFFFF.toInt()) // blanco
        }

        holder.tvMensaje.text = notif.mensaje
        holder.tvFecha.text = formatearFecha(notif.fecha)

        holder.itemView.setOnClickListener {

            Log.d("CLICK_TEST", "CLICK OK")

            val notif = lista[position]




            // 🔴 marcar SOLO esta como leída
            FirebaseFirestore.getInstance()
                .collection("usuarios")
                .document(uid)
                .collection("notificaciones")
                .document(notif.id)
                .update("leido", true)

            // 🔴 opcional: feedback inmediato UI
            lista[position].leido = true
            notifyItemChanged(position)

            holder.itemView.animate()
                .scaleX(0.95f)
                .scaleY(0.95f)
                .setDuration(80)
                .withEndAction {
                    holder.itemView.animate()
                        .scaleX(1f)
                        .scaleY(1f)
                        .setDuration(80)
                        .start()

                    onClick(notif)
                }
                .start()

            onClick(notif)
        }

    }

    private fun formatearFecha(timestamp: com.google.firebase.Timestamp?): String {
        if (timestamp == null) return ""

        val sdf = java.text.SimpleDateFormat("dd MMM - HH:mm", java.util.Locale.getDefault())
        return sdf.format(timestamp.toDate())
    }
}