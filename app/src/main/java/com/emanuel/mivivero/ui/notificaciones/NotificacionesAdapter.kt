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
    private val lista: List<Notificacion>,
    private val uid: String
) : RecyclerView.Adapter<NotificacionesAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvMensaje: TextView = view.findViewById(R.id.tvMensaje)
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

        holder.tvMensaje.text = notif.mensaje

        holder.itemView.setOnClickListener {

            FirebaseFirestore.getInstance()
                .collection("usuarios")
                .document(uid)
                .collection("notificaciones")
                .document(notif.id)
                .update("leido", true)
        }

    }
}