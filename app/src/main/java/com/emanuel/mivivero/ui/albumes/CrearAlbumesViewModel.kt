package com.emanuel.mivivero.ui.albumes

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.emanuel.mivivero.data.local.AppDatabase
import com.emanuel.mivivero.data.local.entity.AlbumEntity
import com.emanuel.mivivero.data.model.EstadoAlbum
import kotlinx.coroutines.launch

class CrearAlbumesViewModel(application: Application)
    : AndroidViewModel(application) {

    // =====================
    // DATABASE
    // =====================

    private val albumDao =
        AppDatabase.getInstance(application).albumDao()

    // =====================
    // LIVE DATA
    // =====================

    private val _albumCreadoId = MutableLiveData<Long>()
    val albumCreadoId: LiveData<Long> = _albumCreadoId

    // =====================
    // CREAR ÁLBUM
    // =====================

    fun crearAlbum(
        nombre: String,
        observaciones: String?
    ) {
        viewModelScope.launch {
            val id = albumDao.insert(
                AlbumEntity(
                    nombre = nombre,
                    observaciones = observaciones,
                    estado = EstadoAlbum.BORRADOR.name
                )
            )
            _albumCreadoId.postValue(id)
        }
    }
}
