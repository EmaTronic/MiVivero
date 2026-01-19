package com.emanuel.mivivero.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.emanuel.mivivero.data.local.database.AppDatabase
import com.emanuel.mivivero.data.local.entity.PlantaEntity
import kotlinx.coroutines.launch

class ViveroViewModel(application: Application) : AndroidViewModel(application) {

    private val plantaDao = AppDatabase
        .getDatabase(application)
        .plantaDao()

    private val _plantas = MutableLiveData<List<PlantaEntity>>()
    val plantas: LiveData<List<PlantaEntity>> = _plantas

    /*
    fun cargarPlantas() {
        viewModelScope.launch {
            _plantas.postValue(plantaDao.getAll())
        }
    }


     */
    fun cargarPlantas() {
        viewModelScope.launch {
            val existentes = plantaDao.getAll()
            if (existentes.isEmpty()) {
                plantaDao.insert(
                    PlantaEntity(
                        nombre = "Lavanda",
                        descripcion = "Planta aromática",
                        precio = 2500.0,
                        stock = 10
                    )
                )
            }
            _plantas.postValue(plantaDao.getAll())
        }
    }

}
