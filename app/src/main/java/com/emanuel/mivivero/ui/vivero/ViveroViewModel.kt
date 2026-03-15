package com.emanuel.mivivero.ui.vivero

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.emanuel.mivivero.data.local.AppDatabase
import com.emanuel.mivivero.data.local.Lugar
import com.emanuel.mivivero.data.local.LugarConConteo
import com.emanuel.mivivero.data.mapper.FotoMapper
import com.emanuel.mivivero.data.mapper.PlantaMapper
import com.emanuel.mivivero.data.model.FotoPlanta
import com.emanuel.mivivero.data.model.Planta
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class ViveroViewModel(application: Application) : AndroidViewModel(application) {

    var hayAlbumActivo: Boolean = false

    private val database = AppDatabase.getInstance(application)
    private val plantaDao = database.plantaDao()
    private val fotoDao = database.fotoDao()
    private val lugarDao = database.lugarDao()

    private val _plantas = MutableLiveData<List<Planta>>()
    val plantas: LiveData<List<Planta>> = _plantas

    private val _lugares = MutableLiveData<List<Lugar>>()
    val lugares: LiveData<List<Lugar>> = _lugares

    private val _lugaresConConteo = MutableLiveData<List<LugarConConteo>>()
    val lugaresConConteo: LiveData<List<LugarConConteo>> = _lugaresConConteo

    private val _mensajeLugares = MutableLiveData<String>()
    val mensajeLugares: LiveData<String> = _mensajeLugares

    init {
        observarLugares()
    }

    fun cargarPlantas() {
        viewModelScope.launch(Dispatchers.IO) {
            val lista = plantaDao.getAllConLugar().map {
                Planta(
                    id = it.id,
                    numeroPlanta = it.numeroPlanta,
                    familia = it.familia,
                    especie = it.especie,
                    lugar = it.lugar,
                    lugarId = it.lugarId,
                    lugarIcono = it.lugarIcono,
                    fechaIngreso = it.fechaIngreso,
                    cantidad = it.cantidad,
                    aLaVenta = it.aLaVenta,
                    observaciones = it.observaciones,
                    fotoRuta = it.fotoRuta,
                    fechaFoto = it.fechaFoto
                )
            }
            _plantas.postValue(lista)
        }
    }

    fun agregarPlanta(planta: Planta) {
        viewModelScope.launch(Dispatchers.IO) {
            val numeroFinal = if (planta.numeroPlanta == -1) obtenerProximoNumeroPlanta() else planta.numeroPlanta
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

    fun agregarFoto(plantaId: Long, ruta: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val fechaReal = obtenerFechaReal(ruta)
            val foto = FotoPlanta(
                id = System.currentTimeMillis(),
                plantaId = plantaId,
                ruta = ruta,
                fecha = fechaReal,
                observaciones = null
            )
            fotoDao.insert(FotoMapper.toEntity(foto))
        }
    }

    suspend fun obtenerFotos(plantaId: Long): List<FotoPlanta> {
        return fotoDao.getFotosPorPlanta(plantaId).map { FotoMapper.toModel(it) }
    }

    fun borrarPlanta(plantaId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            plantaDao.deleteById(plantaId)
            cargarPlantas()
        }
    }

    suspend fun agregarFotoExtra(plantaId: Long, ruta: String) {
        val fechaReal = obtenerFechaReal(ruta)
        val foto = FotoPlanta(
            id = System.currentTimeMillis(),
            plantaId = plantaId,
            ruta = ruta,
            fecha = fechaReal,
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
            val lista = plantaDao.obtenerPlantasDisponiblesParaAlbum(albumId).map { PlantaMapper.toModel(it) }
            _plantas.postValue(lista)
        }
    }

    private fun obtenerFechaReal(ruta: String): Long {
        return try {
            val context = getApplication<Application>()
            val uri = android.net.Uri.parse(ruta)
            val input = context.contentResolver.openInputStream(uri)
            val exif = androidx.exifinterface.media.ExifInterface(input!!)
            val fechaStr = exif.getAttribute(androidx.exifinterface.media.ExifInterface.TAG_DATETIME_ORIGINAL)

            if (fechaStr != null) {
                val formato = java.text.SimpleDateFormat("yyyy:MM:dd HH:mm:ss", java.util.Locale.getDefault())
                formato.parse(fechaStr)?.time ?: System.currentTimeMillis()
            } else {
                System.currentTimeMillis()
            }
        } catch (e: Exception) {
            System.currentTimeMillis()
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
        viewModelScope.launch(Dispatchers.IO) {
            Log.d("VIVERO_VM", "Guardar planta -> familia=$familia | venta=$aLaVenta | foto=$fotoRuta")

            val planta = Planta(
                id = 0L,
                numeroPlanta = 0,
                familia = familia,
                especie = especie,
                lugar = lugar,
                lugarId = null,
                fechaIngreso = System.currentTimeMillis(),
                cantidad = cantidad,
                aLaVenta = aLaVenta,
                observaciones = observaciones,
                fotoRuta = fotoRuta,
                fechaFoto = System.currentTimeMillis()
            )

            plantaDao.insert(PlantaMapper.toEntity(planta))
            Log.d("VIVERO_VM", "Planta guardada correctamente")
            cargarPlantas()
        }
    }

    private fun observarLugares() {
        viewModelScope.launch(Dispatchers.IO) {
            launch {
                lugarDao.observarLugares().collectLatest { _lugares.postValue(it) }
            }
            launch {
                lugarDao.observarLugaresConConteo().collectLatest { _lugaresConConteo.postValue(it) }
            }
        }
    }

    fun guardarLugar(nombre: String, icono: String, lugarId: Int? = null) {
        viewModelScope.launch(Dispatchers.IO) {
            val nombreNormalizado = nombre.trim()

            if (nombreNormalizado.isBlank()) {
                _mensajeLugares.postValue("El nombre no puede estar vacío")
                return@launch
            }

            val duplicado = lugarDao.obtenerPorNombre(nombreNormalizado)
            if (duplicado != null && duplicado.id != lugarId) {
                _mensajeLugares.postValue("Ya existe un lugar con ese nombre")
                return@launch
            }

            if (lugarId == null) {
                lugarDao.insertar(Lugar(nombre = nombreNormalizado, icono = icono))
            } else {
                lugarDao.actualizar(Lugar(id = lugarId, nombre = nombreNormalizado, icono = icono))
            }
        }
    }

    fun eliminarLugar(lugar: LugarConConteo) {
        viewModelScope.launch(Dispatchers.IO) {
            val totalPlantas = lugarDao.contarPlantasAsociadas(lugar.id)
            if (totalPlantas > 0) {
                _mensajeLugares.postValue("No puedes eliminar este lugar porque tiene plantas asignadas")
                return@launch
            }

            lugarDao.eliminar(Lugar(id = lugar.id, nombre = lugar.nombre, icono = lugar.icono))
        }
    }
}
