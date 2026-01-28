package com.emanuel.mivivero.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.emanuel.mivivero.data.model.Planta

class ViveroViewModel : ViewModel() {

    // 🔒 backing property
    private val _plantas = MutableLiveData<List<Planta>>(emptyList())
    val plantas: LiveData<List<Planta>> = _plantas

    fun agregarPlanta(planta: Planta) {
        val listaActual = _plantas.value ?: emptyList()

        // 👇 CREAR UNA LISTA NUEVA (clave)
        _plantas.value = listaActual + planta
    }

    fun obtenerPlantaPorId(id: Long): Planta? {
        return _plantas.value?.find { it.id == id }
    }
}
