package com.emanuel.mivivero.data.db.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.emanuel.mivivero.data.db.entity.UsuarioEntity

@Dao
interface UsuarioDao {

    @Query("SELECT * FROM usuario WHERE id = 1 LIMIT 1")
    suspend fun obtenerUsuario(): UsuarioEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarUsuario(usuario: UsuarioEntity)

    @Update
    suspend fun actualizarUsuario(usuario: UsuarioEntity)
}
