package com.emanuel.mivivero.ui.albumes

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.emanuel.mivivero.data.local.AppDatabase
import com.emanuel.mivivero.data.local.entity.AlbumEntity
import com.emanuel.mivivero.data.local.entity.AlbumPlantaEntity
import com.emanuel.mivivero.data.model.EstadoAlbum
import com.emanuel.mivivero.data.model.Planta
import kotlinx.coroutines.launch

class AlbumesViewModel(application: Application)
    : AndroidViewModel(application) {

    // 🔥 BASE DE DATOS
    private val db = AppDatabase.getInstance(application)

    // 🔥 DAOs NECESARIOS
    private val albumPlantaDao = db.albumPlantaDao()

    // 🔥 DAO
    private val albumDao = db.albumDao()

    // 🔥 ESTA PROPIEDAD TIENE QUE EXISTIR
    val albumes: LiveData<List<AlbumEntity>> =
        albumDao.getAlbumes()

    // Id del álbum seleccionado
    var albumActualId: Long? = null

    fun agregarPlantaAlAlbum(
        planta: Planta,
        cantidad: Int,
        precio: Double
    ) {
        val albumId = albumActualId ?: return

        viewModelScope.launch {
            albumPlantaDao.insert(
                AlbumPlantaEntity(
                    albumId = albumId,
                    plantaId = planta.id,
                    cantidad = cantidad,
                    precio = precio
                )
            )
        }
    }

    fun crearAlbum(nombre: String, observaciones: String?) {
        viewModelScope.launch {
            albumDao.insert(
                AlbumEntity(
                    nombre = nombre,
                    observaciones = observaciones,
                    estado = EstadoAlbum.BORRADOR
                )
            )
        }
    }


}
