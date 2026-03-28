package com.emanuel.mivivero.utils

import com.google.firebase.functions.FirebaseFunctions

object AdminFunctions {

    val functions = FirebaseFunctions.getInstance("us-central1")
    fun setBloqueado(
        uid: String,
        bloqueado: Boolean,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {

        val functions = FirebaseFunctions.getInstance("us-central1") // 🔥 clave

        val data = hashMapOf(
            "uid" to uid,
            "bloqueado" to bloqueado
        )

        functions
            .getHttpsCallable("setBloqueado")
            .call(data)
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener {
                onError(it)
            }
    }
}