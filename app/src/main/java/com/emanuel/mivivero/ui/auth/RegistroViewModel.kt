package com.emanuel.mivivero.ui.auth

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.emanuel.mivivero.data.local.AppDatabase
import com.emanuel.mivivero.data.db.entity.UsuarioEntity


class RegistroViewModel(application: Application) :
    AndroidViewModel(application) {

    private val usuarioDao =
        AppDatabase.getInstance(application).usuarioDao()

    suspend fun obtenerUsuario(): UsuarioEntity? {
        return usuarioDao.obtenerUsuario()
    }

    suspend fun usuarioRegistrado(): Boolean {
        return usuarioDao.obtenerUsuario() != null
    }

    suspend fun usuarioEstaBloqueado(): Boolean {
        return usuarioDao.obtenerUsuario()?.usuarioBloqueado == true
    }

    suspend fun guardarUsuario(usuario: UsuarioEntity) {
        val existente = usuarioDao.obtenerUsuario()

        if (existente == null) {
            usuarioDao.insertarUsuario(usuario)
        } else {
            usuarioDao.actualizarUsuario(usuario)
        }
    }


}
