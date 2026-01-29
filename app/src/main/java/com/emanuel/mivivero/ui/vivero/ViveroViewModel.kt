package com.emanuel.mivivero.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.emanuel.mivivero.data.local.AppDatabase
import com.emanuel.mivivero.data.mapper.PlantaMapper
import com.emanuel.mivivero.data.model.Planta
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ViveroViewModel(application: Application) : AndroidViewModel(application) {

    private val dao = AppDatabase
        .getInstance(application)
        .plantaDao()

    private val _plantas = MutableLiveData<List<Planta>>(emptyList())
    val plantas: LiveData<List<Planta>> = _plantas

    init {
        cargarPlantas()
    }

    fun cargarPlantas() {
        viewModelScope.launch(Dispatchers.IO) {
            val lista = dao.getAll().map {
                PlantaMapper.toModel(it)
            }
            _plantas.postValue(lista)
        }
    }

    fun agregarPlanta(planta: Planta) {
        viewModelScope.launch(Dispatchers.IO) {
            dao.insert(PlantaMapper.toEntity(planta))
            cargarPlantas()
        }
    }

    fun obtenerPlantaPorId(id: Long): Planta? {
        return _plantas.value?.find { it.id == id }
    }
}
