package com.emanuel.mivivero.data.model

import androidx.room.Embedded
import androidx.room.Relation
import com.emanuel.mivivero.data.db.entity.FotoEntity
import com.emanuel.mivivero.data.db.entity.PlantaEntity

data class PlantaConFoto(
    @Embedded val planta: PlantaEntity,

    @Relation(
        parentColumn = "id",
        entityColumn = "plantaId",
        entity = FotoEntity::class
    )
    val fotos: List<FotoEntity>
) {
    val fotoPrincipal: FotoEntity?
        get() = fotos.firstOrNull { it.esPrincipal }
}
