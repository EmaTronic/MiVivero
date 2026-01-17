package com.emanuel.mivivero.ui.vivero

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.emanuel.mivivero.data.db.AppDatabase
import com.emanuel.mivivero.data.repository.PlantaRepository

class PlantaDetalleViewModelFactory(
    private val context: Context
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PlantaDetalleViewModel::class.java)) {

            val db = AppDatabase.getInstance(context)
            val repository = PlantaRepository(
                plantaDao = db.plantaDao(),
                fotoDao = db.fotoDao()
            )

            @Suppress("UNCHECKED_CAST")
            return PlantaDetalleViewModel(repository) as T
        }

        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
