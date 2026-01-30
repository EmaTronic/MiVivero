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
            val lista = dao.getAll()
                .map { PlantaMapper.toModel(it) }
                .sortedBy { it.numeroPlanta }   // 🔥 CLAVE
            _plantas.postValue(lista)
        }
    }


    fun agregarPlanta(planta: Planta) {
        viewModelScope.launch(Dispatchers.IO) {

            val numero = if (planta.numeroPlanta == -1) {
                obtenerProximoNumeroPlanta()
            } else {
                planta.numeroPlanta
            }

            val plantaFinal = planta.copy(numeroPlanta = numero)

            dao.insert(PlantaMapper.toEntity(plantaFinal))
            cargarPlantas()
        }
    }


    fun obtenerPlantaPorId(id: Long): Planta? {
        return _plantas.value?.find { it.id == id }
    }

    fun borrarPlanta(id: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            dao.deleteById(id)
            cargarPlantas()
        }
    }
    suspend fun obtenerProximoNumeroPlanta(): Int {
        val ultimo = dao.getUltimoNumeroPlanta()
        return (ultimo ?: 0) + 1
    }




}
