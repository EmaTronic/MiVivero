package com.emanuel.mivivero.utils

import com.google.firebase.auth.FirebaseAuth

object AuthManager {

    fun usuarioLogueado(): Boolean {
        return FirebaseAuth.getInstance().currentUser != null
    }

    fun uid(): String? {
        return FirebaseAuth.getInstance().currentUser?.uid
    }

}