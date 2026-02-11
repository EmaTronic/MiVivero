package com.emanuel.mivivero.ui.albumes

import android.app.Application
import android.util.Log
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

    private val db = AppDatabase.getInstance(application)
    private val albumDao = db.albumDao()
    private val albumPlantaDao = db.albumPlantaDao()

    val albumes: LiveData<List<AlbumEntity>> =
        albumDao.getAlbumes()

    // 🔥 ESTE ES EL ESTADO QUE FALTABA SETEAR
    var albumActualId: Long? = null

    fun agregarPlantaAlAlbum(
        planta: Planta,
        cantidad: Int,
        precio: Double,
        onResultado: (String?) -> Unit
    ) {
        val albumId = albumActualId ?: return

        if (cantidad <= 0) {
            onResultado("La cantidad debe ser mayor a 0")
            return
        }

        if (precio <= 0.0) {
            onResultado("El precio debe ser mayor a 0")
            return
        }

        viewModelScope.launch {
            val existe =
                albumPlantaDao.existePlantaEnAlbum(albumId, planta.id) > 0

            if (existe) {
                onResultado("La planta ya está en el álbum")
                return@launch
            }

            albumPlantaDao.insert(
                AlbumPlantaEntity(
                    albumId = albumId,
                    plantaId = planta.id,
                    cantidad = cantidad,
                    precio = precio
                )
            )

            onResultado(null) // éxito
        }
    }



    fun crearAlbum(nombre: String, observaciones: String?) {
        viewModelScope.launch {
            albumDao.insert(
                AlbumEntity(
                    nombre = nombre,
                    observaciones = observaciones,
                    estado = EstadoAlbum.BORRADOR.name
                )
            )
        }
    }
}
