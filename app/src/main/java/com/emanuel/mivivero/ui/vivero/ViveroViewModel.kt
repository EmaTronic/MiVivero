package com.emanuel.mivivero.ui.vivero

import androidx.lifecycle.ViewModel
import com.emanuel.mivivero.data.model.Planta
import com.emanuel.mivivero.data.repository.PlantaRepository

class ViveroViewModel : ViewModel() {

    private val repository = PlantaRepository()

    fun getPlantas(): List<Planta> {
        return repository.getPlantas()
    }
}
