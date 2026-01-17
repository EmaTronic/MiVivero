package com.emanuel.mivivero.data.repository

import com.emanuel.mivivero.data.db.dao.FotoDao
import com.emanuel.mivivero.data.db.dao.PlantaDao
import com.emanuel.mivivero.data.db.entity.FotoEntity
import com.emanuel.mivivero.data.db.entity.PlantaEntity
import com.emanuel.mivivero.data.model.PlantaConFoto
import kotlinx.coroutines.flow.Flow

class PlantaRepository(
    private val plantaDao: PlantaDao,
    private val fotoDao: FotoDao
) {

    // ===== VIVERO (lista principal con foto) =====
    fun getPlantasConFoto(): Flow<List<PlantaConFoto>> {
        return plantaDao.getPlantasConFoto()
    }

    // ===== DETALLE PLANTA =====
    suspend fun getPlantaById(id: Long): PlantaEntity? {
        return plantaDao.getPlantaById(id)
    }

    // ===== PLANTAS =====
    suspend fun insertarPlanta(planta: PlantaEntity) {
        plantaDao.insert(planta)
    }

    // ===== FOTOS =====
    suspend fun insertarFoto(foto: FotoEntity) {
        fotoDao.insert(foto)
    }

    suspend fun getFotosDePlanta(plantaId: Long): List<FotoEntity> {
        return fotoDao.getFotosDePlanta(plantaId)
    }

    suspend fun marcarFotoPrincipal(plantaId: Long, fotoId: Long) {
        fotoDao.limpiarPrincipal(plantaId)
        fotoDao.marcarPrincipal(fotoId)
    }

    suspend fun borrarFoto(foto: FotoEntity) {
        fotoDao.delete(foto)
    }
}
