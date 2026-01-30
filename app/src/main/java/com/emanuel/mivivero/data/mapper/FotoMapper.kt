package com.emanuel.mivivero.data.mapper

import com.emanuel.mivivero.data.local.entity.FotoEntity
import com.emanuel.mivivero.data.model.FotoPlanta

object FotoMapper {

    fun toEntity(model: FotoPlanta): FotoEntity =
        FotoEntity(
            id = model.id,
            plantaId = model.plantaId,
            ruta = model.ruta,
            fecha = model.fecha,
            observaciones = model.observaciones
        )

    fun toModel(entity: FotoEntity): FotoPlanta =
        FotoPlanta(
            id = entity.id,
            plantaId = entity.plantaId,
            ruta = entity.ruta,
            fecha = entity.fecha,
            observaciones = entity.observaciones
        )
}
