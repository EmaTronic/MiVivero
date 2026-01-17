package com.emanuel.mivivero.ui.vivero

import androidx.lifecycle.ViewModel
import com.emanuel.mivivero.data.model.PlantaConFoto
import com.emanuel.mivivero.data.repository.PlantaRepository
import kotlinx.coroutines.flow.Flow

class ViveroViewModel(
    repository: PlantaRepository
) : ViewModel() {

    val plantas: Flow<List<PlantaConFoto>> =
        repository.getPlantasConFoto()
}

