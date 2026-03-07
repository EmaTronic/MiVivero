package com.emanuel.mivivero.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface LugarDao {

    @Query("SELECT * FROM lugares ORDER BY nombre COLLATE NOCASE ASC")
    fun observarLugares(): Flow<List<Lugar>>

    @Query(
        """
        SELECT lugares.id, lugares.nombre, lugares.icono, COUNT(plantas.id) as cantidad
        FROM lugares
        LEFT JOIN plantas ON plantas.lugarId = lugares.id
        GROUP BY lugares.id
        ORDER BY lugares.nombre COLLATE NOCASE ASC
        """
    )
    fun observarLugaresConConteo(): Flow<List<LugarConConteo>>

    @Query("SELECT * FROM lugares WHERE LOWER(nombre) = LOWER(:nombre) LIMIT 1")
    suspend fun obtenerPorNombre(nombre: String): Lugar?

    @Query("SELECT * FROM lugares WHERE id = :id LIMIT 1")
    suspend fun obtenerPorId(id: Int): Lugar?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertar(lugar: Lugar): Long

    @Update
    suspend fun actualizar(lugar: Lugar)

    @Delete
    suspend fun eliminar(lugar: Lugar)

    @Query("SELECT COUNT(*) FROM plantas WHERE lugarId = :lugarId")
    suspend fun contarPlantasAsociadas(lugarId: Int): Int
}
