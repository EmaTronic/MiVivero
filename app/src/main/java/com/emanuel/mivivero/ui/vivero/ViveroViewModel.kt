package com.emanuel.mivivero.ui.vivero

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.emanuel.mivivero.data.db.entity.PlantaEntity
import com.emanuel.mivivero.data.repository.PlantaRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class ViveroViewModel(
    private val repository: PlantaRepository
) : ViewModel() {

    val plantas: StateFlow<List<PlantaEntity>> =
        repository.getPlantas()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = emptyList()
            )
}
