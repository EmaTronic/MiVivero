package com.emanuel.mivivero.data.model

data class AlbumBackup(
    val album: com.emanuel.mivivero.data.local.entity.AlbumEntity,
    val plantas: List<com.emanuel.mivivero.data.local.entity.AlbumPlantaEntity>
)
