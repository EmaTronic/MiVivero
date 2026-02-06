package com.emanuel.mivivero.ui.albumes

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import com.emanuel.mivivero.data.local.AppDatabase
import com.emanuel.mivivero.data.local.entity.AlbumEntity
import com.emanuel.mivivero.data.local.entity.AlbumPlantaEntity
import com.emanuel.mivivero.data.mapper.PlantaMapper
import com.emanuel.mivivero.data.model.EstadoAlbum
import com.emanuel.mivivero.data.model.Planta
import kotlinx.coroutines.launch

class CrearAlbumesViewModel(application: Application)
    : AndroidViewModel(application) {

    private val albumDao =
        AppDatabase.getInstance(application).albumDao()

    fun crearAlbum(
        nombre: String,
        observaciones: String?,
        onCreado: (Long) -> Unit
    ) {
        viewModelScope.launch {
            val id = albumDao.insert(
                AlbumEntity(
                    nombre = nombre,
                    observaciones = observaciones,
                    estado = EstadoAlbum.BORRADOR.name
                )
            )
            onCreado(id)
        }
    }
}

