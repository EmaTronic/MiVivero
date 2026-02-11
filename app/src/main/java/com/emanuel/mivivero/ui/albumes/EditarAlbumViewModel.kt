package com.emanuel.mivivero.ui.albumes

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.emanuel.mivivero.data.local.AppDatabase
import com.emanuel.mivivero.data.local.entity.AlbumEntity
import com.emanuel.mivivero.data.model.EstadoAlbum
import com.emanuel.mivivero.data.model.PlantaAlbum
import kotlinx.coroutines.launch

class EditarAlbumViewModel(application: Application)
    : AndroidViewModel(application) {

    private val db = AppDatabase.getInstance(application)
    private val albumDao = db.albumDao()
    private val albumPlantaDao = db.albumPlantaDao()

    fun obtenerAlbum(albumId: Long): LiveData<AlbumEntity?> =
        albumDao.obtenerPorId(albumId)

    fun obtenerPlantasDelAlbum(albumId: Long): LiveData<List<PlantaAlbum>> =
        albumPlantaDao.obtenerPlantasDelAlbum(albumId)

    fun finalizarAlbum(
        albumId: Long,
        onResultado: (ResultadoValidacion) -> Unit
    ) {
        viewModelScope.launch {

            val resultado = validarAlbumParaFinalizar(albumId)

            if (!resultado.esValido) {
                onResultado(resultado)
                return@launch
            }

            albumDao.actualizarEstado(
                albumId,
                EstadoAlbum.FINALIZADO.name
            )

            onResultado(ResultadoValidacion(true))
        }
    }


    fun eliminarPlantaDelAlbum(
        albumId: Long,
        plantaId: Long
    ) {
        viewModelScope.launch {
            albumPlantaDao.eliminarPlantaDelAlbum(
                albumId = albumId,
                plantaId = plantaId
            )
        }
    }

    fun actualizarPlanta(
        albumId: Long,
        plantaId: Long,
        cantidad: Int,
        precio: Double,
        onResultado: (String?) -> Unit
    ) {
        if (cantidad <= 0) {
            onResultado("La cantidad debe ser mayor a 0")
            return
        }

        if (precio <= 0.0) {
            onResultado("El precio debe ser mayor a 0")
            return
        }

        viewModelScope.launch {
            albumPlantaDao.actualizarPlantaAlbum(
                albumId = albumId,
                plantaId = plantaId,
                cantidad = cantidad,
                precio = precio
            )
            onResultado(null)
        }
    }


    data class ResultadoValidacion(
        val esValido: Boolean,
        val mensaje: String? = null
    )


    suspend fun validarAlbumParaFinalizar(albumId: Long): ResultadoValidacion {

        val plantas = albumPlantaDao.obtenerPlantasDelAlbumSuspend(albumId)

        if (plantas.isEmpty()) {
            return ResultadoValidacion(false, "El álbum no tiene plantas")
        }

        if (plantas.size > 40) {
            return ResultadoValidacion(false, "Máximo 40 plantas por álbum")
        }

        val plantaInvalida = plantas.firstOrNull {
            it.cantidad <= 0 || it.precio <= 0.0
        }

        if (plantaInvalida != null) {
            return ResultadoValidacion(
                false,
                "Hay plantas con cantidad o precio inválido"
            )
        }

        return ResultadoValidacion(true)
    }

    fun reabrirAlbum(albumId: Long) {
        viewModelScope.launch {
            albumDao.actualizarEstado(
                albumId,
                EstadoAlbum.BORRADOR.name
            )
        }
    }





}
