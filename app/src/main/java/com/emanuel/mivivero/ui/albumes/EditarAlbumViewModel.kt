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

class EditarAlbumViewModel(application: Application) :
    AndroidViewModel(application) {

    private val db = AppDatabase.getInstance(application)

    private val albumDao = db.albumDao()
    private val albumPlantaDao = db.albumPlantaDao()

    /* =====================
       ALBUM
       ===================== */

    fun obtenerAlbum(albumId: Long): LiveData<AlbumEntity?> {
        return albumDao.obtenerPorId(albumId)
    }

    /* =====================
       PLANTAS DEL ALBUM
       ===================== */

    fun obtenerPlantasDelAlbum(albumId: Long): LiveData<List<PlantaAlbum>> {
        return albumPlantaDao.obtenerPlantasDelAlbum(albumId)
    }

    /* =====================
       FINALIZAR
       ===================== */

    fun finalizarAlbum(albumId: Long) {
        viewModelScope.launch {
            albumDao.actualizarEstado(albumId, EstadoAlbum.FINALIZADO.name)
        }
    }
}
