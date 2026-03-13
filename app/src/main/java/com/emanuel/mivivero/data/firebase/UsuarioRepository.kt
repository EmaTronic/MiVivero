package com.emanuel.mivivero.data.firebase

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

object UsuarioRepository {

    private val db = FirebaseFirestore.getInstance()

    fun crearUsuarioSiNoExiste() {

        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return

        val ref = db.collection("usuarios").document(uid)

        ref.get().addOnSuccessListener { doc ->

            if (!doc.exists()) {

                val usuario = hashMapOf(
                    "rol" to "free",
                    "albumesPublicados" to 0,
                    "bloqueado" to false,
                    "fechaRegistro" to FieldValue.serverTimestamp()
                )

                ref.set(usuario)
            }
        }
    }
}