package com.emanuel.mivivero.data.preferences

import android.content.Context

class UserPreferences(context: Context) {

    private val prefs =
        context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)

    fun guardarNombreVivero(nombre: String) {
        prefs.edit().putString("nombre_vivero", nombre).apply()
    }

    fun obtenerNombreVivero(): String {
        return prefs.getString("nombre_vivero", "") ?: ""
    }
}
