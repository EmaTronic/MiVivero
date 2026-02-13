package com.emanuel.mivivero.ui.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.emanuel.mivivero.data.local.AppDatabase
import com.emanuel.mivivero.data.mapper.FotoMapper
import com.emanuel.mivivero.data.mapper.PlantaMapper
import com.emanuel.mivivero.data.model.FotoPlanta
import com.emanuel.mivivero.data.model.Planta
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ViveroViewModel(application: Application) : AndroidViewModel(application) {

    // =========================
    // DATABASE & DAOs
    // =========================

    var hayAlbumActivo: Boolean = false

    private val database = AppDatabase.getInstance(application)

    private val plantaDao = database.plantaDao()
    private val fotoDao = database.fotoDao()   // 🔥 ESTE ERA EL PROBLEMA

    // =========================
    // PLANTAS
    // =========================

    private val _plantas = MutableLiveData<List<Planta>>()
    val plantas: LiveData<List<Planta>> = _plantas

    fun cargarPlantas() {
        viewModelScope.launch(Dispatchers.IO) {
            val lista = plantaDao.getAll()
                .map { PlantaMapper.toModel(it) }
                .sortedBy { it.numeroPlanta }

            _plantas.postValue(lista)
        }
    }

    fun agregarPlanta(planta: Planta) {
        viewModelScope.launch(Dispatchers.IO) {

            val numeroFinal =
                if (planta.numeroPlanta == -1) {
                    obtenerProximoNumeroPlanta()
                } else {
                    planta.numeroPlanta
                }

            val plantaFinal = planta.copy(numeroPlanta = numeroFinal)

            plantaDao.insert(PlantaMapper.toEntity(plantaFinal))
            cargarPlantas()
        }
    }

    private suspend fun obtenerProximoNumeroPlanta(): Int {
        val max = plantaDao.getMaxNumeroPlanta()
        return (max ?: 0) + 1
    }

    fun obtenerPlantaPorId(id: Long): Planta? {
        return _plantas.value?.find { it.id == id }
    }

    // =========================
    // FOTOS
    // =========================

    fun agregarFoto(plantaId: Long, ruta: String) {
        viewModelScope.launch(Dispatchers.IO) {

            val foto = FotoPlanta(
                id = System.currentTimeMillis(),
                plantaId = plantaId,
                ruta = ruta,
                fecha = System.currentTimeMillis(),
                observaciones = null
            )

            fotoDao.insert(FotoMapper.toEntity(foto))
        }
    }

    suspend fun obtenerFotos(plantaId: Long): List<FotoPlanta> {
        return fotoDao.getFotosPorPlanta(plantaId)
            .map { FotoMapper.toModel(it) }
    }

    fun borrarPlanta(plantaId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            plantaDao.deleteById(plantaId)
            cargarPlantas()
        }
    }

    suspend fun agregarFotoExtra(plantaId: Long, ruta: String) {
        val foto = FotoPlanta(
            id = System.currentTimeMillis(),
            plantaId = plantaId,
            ruta = ruta,
            fecha = System.currentTimeMillis(),
            observaciones = null
        )
        fotoDao.insert(FotoMapper.toEntity(foto))
    }

    fun actualizarPlanta(planta: Planta) {
        viewModelScope.launch(Dispatchers.IO) {
            plantaDao.update(PlantaMapper.toEntity(planta))
            cargarPlantas()
        }
    }

    fun borrarFoto(fotoId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            fotoDao.deleteById(fotoId)
        }
    }

    fun eliminarFoto(foto: FotoPlanta) {
        viewModelScope.launch(Dispatchers.IO) {
            fotoDao.delete(FotoMapper.toEntity(foto))
        }
    }


    fun cargarPlantasParaAlbum(albumId: Long) {
        viewModelScope.launch(Dispatchers.IO) {

            val lista = plantaDao
                .obtenerPlantasDisponiblesParaAlbum(albumId)
                .map { PlantaMapper.toModel(it) }

            _plantas.postValue(lista)
        }
    }





    fun guardarPlanta(
        familia: String,
        especie: String?,
        lugar: String,
        cantidad: Int,
        aLaVenta: Boolean,
        observaciones: String?,
        fotoRuta: String?
    ) {
        viewModelScope.launch {

            Log.d(
                "VIVERO_VM",
                "Guardar planta -> familia=$familia | venta=$aLaVenta | foto=$fotoRuta"
            )





            val planta = Planta(
                id = 0L,
                numeroPlanta = 0, // si lo generás automático luego, se ajusta
                familia = familia,
                especie = especie,
                lugar = lugar,
                fechaIngreso = System.currentTimeMillis(),
                cantidad = cantidad,
                aLaVenta = aLaVenta,
                observaciones = observaciones,
                fotoRuta = fotoRuta,
                fechaFoto = System.currentTimeMillis()
            )

            plantaDao.insert(
                PlantaMapper.toEntity(planta)
            )

            Log.d("VIVERO_VM", "Planta guardada correctamente")
        }
    }








}
