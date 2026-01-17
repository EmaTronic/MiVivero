package com.emanuel.mivivero.ui.vivero

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.emanuel.mivivero.data.db.entity.FotoEntity
import com.emanuel.mivivero.data.db.entity.PlantaEntity
import com.emanuel.mivivero.data.repository.PlantaRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class PlantaDetalleViewModel(
    private val repository: PlantaRepository
) : ViewModel() {

    // ===== PLANTA =====
    private val _planta = MutableStateFlow<PlantaEntity?>(null)
    val planta: StateFlow<PlantaEntity?> = _planta

    fun cargarPlanta(plantaId: Long) {
        viewModelScope.launch {
            _planta.value = repository.getPlantaById(plantaId)
        }
    }

    // ===== FOTOS =====
    private val _fotos = MutableStateFlow<List<FotoEntity>>(emptyList())
    val fotos: StateFlow<List<FotoEntity>> = _fotos

    fun cargarFotos(plantaId: Long) {
        viewModelScope.launch {
            _fotos.value = repository.getFotosDePlanta(plantaId)
        }
    }

    fun agregarFoto(foto: FotoEntity) {
        viewModelScope.launch {
            repository.insertarFoto(foto)
            cargarFotos(foto.plantaId)
        }
    }

    fun marcarComoPrincipal(plantaId: Long, fotoId: Long) {
        viewModelScope.launch {
            repository.marcarFotoPrincipal(plantaId, fotoId)
            cargarFotos(plantaId)
        }
    }

    fun borrarFoto(foto: FotoEntity) {
        viewModelScope.launch {
            repository.borrarFoto(foto)
            cargarFotos(foto.plantaId)
        }
    }

}
