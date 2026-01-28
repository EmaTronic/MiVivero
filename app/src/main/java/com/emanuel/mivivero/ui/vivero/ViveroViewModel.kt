package com.emanuel.mivivero.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.emanuel.mivivero.data.model.Planta

class ViveroViewModel : ViewModel() {

    private val _plantas = MutableLiveData<List<Planta>>(emptyList())
    val plantas: LiveData<List<Planta>> = _plantas

    private val _plantaSeleccionada = MutableLiveData<Planta>()
    val plantaSeleccionada: LiveData<Planta> = _plantaSeleccionada

    fun agregarPlanta(planta: Planta) {
        val actual = _plantas.value?.toMutableList() ?: mutableListOf()
        actual.add(planta)
        _plantas.value = actual
    }

    // 🔥 ESTE MÉTODO EXISTE Y SE USA
    fun seleccionarPlanta(planta: Planta) {
        _plantaSeleccionada.value = planta
    }
}
