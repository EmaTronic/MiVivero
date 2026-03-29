package com.emanuel.mivivero.ui.notificaciones

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.emanuel.mivivero.R
import com.emanuel.mivivero.data.model.Notificacion
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlin.jvm.java

class NotificacionesFragment : Fragment(R.layout.fragment_notificaciones) {

    private val db = FirebaseFirestore.getInstance()
    private lateinit var recycler: RecyclerView

    private lateinit var adapter: NotificacionesAdapter
    private val lista = mutableListOf<Notificacion>()





    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recycler = view.findViewById(R.id.recyclerNotificaciones)
        recycler.layoutManager = LinearLayoutManager(requireContext())

        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return

        adapter = NotificacionesAdapter(lista, uid)
        recycler.adapter = adapter

        cargarNotificaciones()


    }

    private fun cargarNotificaciones() {

        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return

        Log.d("NOTIF_DEBUG", "UID=$uid")

        db.collection("usuarios")
            .document(uid)
            .collection("notificaciones")
            .orderBy("fecha", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { snapshot ->

                val nuevaLista = snapshot.documents.map { doc ->
                    val notif = doc.toObject(Notificacion::class.java)!!
                    notif.copy(id = doc.id)
                }

                // 🔴 ACÁ ES LA ACTUALIZACIÓN REAL
                lista.clear()
                lista.addAll(nuevaLista)
                adapter.notifyDataSetChanged()
            }
            .addOnFailureListener {
                Log.e("NOTIF_DEBUG", "ERROR", it)
            }
    }
}