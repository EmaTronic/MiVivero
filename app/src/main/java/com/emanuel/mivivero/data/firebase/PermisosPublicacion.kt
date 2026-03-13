package com.emanuel.mivivero.data.firebase

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

object PermisosPublicacion {

    private val db = FirebaseFirestore.getInstance()

    fun puedePublicar(callback: (Boolean) -> Unit) {

        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return

        db.collection("usuarios")
            .document(uid)
            .get()
            .addOnSuccessListener { doc ->

                val rol = doc.getString("rol")
                val publicados = doc.getLong("albumesPublicados") ?: 0

                if (rol == "free" && publicados >= 3) {
                    callback(false)
                } else {
                    callback(true)
                }
            }
    }
}