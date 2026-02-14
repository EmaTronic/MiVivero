package com.emanuel.mivivero.ui.auth

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.emanuel.mivivero.data.local.AppDatabase
import com.emanuel.mivivero.data.db.entity.UsuarioEntity

class RegistroViewModel(application: Application)
    : AndroidViewModel(application) {

    private val usuarioDao =
        AppDatabase.getInstance(application).usuarioDao()

    // ==========================
    // OBTENER USUARIO
    // ==========================
    suspend fun obtenerUsuario(): UsuarioEntity? {
        return usuarioDao.obtenerUsuario()
    }

    // ==========================
    // VERIFICAR REGISTRO
    // ==========================
    suspend fun usuarioRegistrado(): Boolean {
        return usuarioDao.obtenerUsuario() != null
    }

    // ==========================
    // VERIFICAR BLOQUEO
    // ==========================
    suspend fun usuarioEstaBloqueado(): Boolean {
        return usuarioDao.obtenerUsuario()?.usuarioBloqueado == true
    }

    // ==========================
    // INSERTAR
    // ==========================
    suspend fun insertarUsuario(usuario: UsuarioEntity) {
        usuarioDao.insertarUsuario(usuario)
    }

    // ==========================
    // ACTUALIZAR
    // ==========================
    suspend fun actualizarUsuario(usuario: UsuarioEntity) {
        usuarioDao.actualizarUsuario(usuario)
    }
}
