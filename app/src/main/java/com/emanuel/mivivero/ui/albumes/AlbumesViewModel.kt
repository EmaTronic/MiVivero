package com.emanuel.mivivero.ui.albumes

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.emanuel.mivivero.data.local.AppDatabase
import com.emanuel.mivivero.data.local.entity.AlbumEntity
import com.emanuel.mivivero.data.local.entity.AlbumPlantaEntity
import com.emanuel.mivivero.data.model.AlbumBackup
import com.emanuel.mivivero.data.model.EstadoAlbum
import com.emanuel.mivivero.data.model.Planta
import kotlinx.coroutines.launch
import com.emanuel.mivivero.data.model.AlbumConCantidad
import com.emanuel.mivivero.data.model.PlantaAlbum


class AlbumesViewModel(application: Application)
    : AndroidViewModel(application) {

    private val db = AppDatabase.getInstance(application)
    private val albumDao = db.albumDao()
    private val albumPlantaDao = db.albumPlantaDao()

    val albumes: LiveData<List<AlbumConCantidad>> =
        albumDao.getAlbumesConCantidad()


    // 🔥 ESTE ES EL ESTADO QUE FALTABA SETEAR
    var albumActualId: Long? = null

    fun agregarPlantaAlAlbum(
        planta: Planta,
        cantidad: Int,
        precio: Double,
        onResultado: (String?) -> Unit
    ) {

        val albumId = albumActualId
        if (albumId == null) {
            onResultado("No hay álbum activo")
            return
        }

        // Validaciones básicas
        if (cantidad <= 0) {
            onResultado("La cantidad debe ser mayor a 0")
            return
        }

        if (precio <= 0.0) {
            onResultado("El precio debe ser mayor a 0")
            return
        }

        // 🔥 VALIDACIÓN DE STOCK
        if (cantidad > planta.cantidad) {
            onResultado("Stock insuficiente. Disponibles: ${planta.cantidad}")
            return
        }

        viewModelScope.launch {

            // Verificar que no esté ya en el álbum
            val existe =
                albumPlantaDao.existePlantaEnAlbum(albumId, planta.id) > 0

            if (existe) {
                onResultado("La planta ya está en el álbum")
                return@launch
            }

            // Insertar en el álbum
            albumPlantaDao.insert(
                AlbumPlantaEntity(
                    albumId = albumId,
                    plantaId = planta.id,
                    cantidad = cantidad,
                    precio = precio
                )
            )

            onResultado(null) // Éxito
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

    fun eliminarAlbum(albumId: Long) {
        viewModelScope.launch {
            albumDao.deleteById(albumId)
        }
    }

    private var ultimoAlbumEliminado: AlbumConCantidad? = null

    private var ultimoBackup: AlbumBackup? = null

    fun eliminarAlbumConUndo(albumId: Long) {

        viewModelScope.launch {

            val album = albumDao.obtenerAlbumRaw(albumId) ?: return@launch
            val plantas = albumPlantaDao.obtenerPlantasDelAlbumRaw(albumId)

            ultimoBackup = AlbumBackup(album, plantas)

            albumDao.deleteById(albumId)
        }
    }


    fun restaurarUltimoAlbum() {

        val backup = ultimoBackup ?: return

        viewModelScope.launch {

            albumDao.insert(backup.album)

            backup.plantas.forEach {
                albumPlantaDao.insert(it)
            }

            ultimoBackup = null
        }
    }

    fun obtenerPlantasDelAlbumRaw(albumId: Long): LiveData<List<PlantaAlbum>> {
        return albumPlantaDao.obtenerPlantasDelAlbum(albumId)
    }






}
