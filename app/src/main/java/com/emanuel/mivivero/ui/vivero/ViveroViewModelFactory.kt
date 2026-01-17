package com.emanuel.mivivero.ui.vivero

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.emanuel.mivivero.data.db.AppDatabase
import com.emanuel.mivivero.data.repository.PlantaRepository

class ViveroViewModelFactory(
    private val context: Context
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ViveroViewModel::class.java)) {

            val db = AppDatabase.getInstance(context)
            val repository = PlantaRepository(
                plantaDao = db.plantaDao(),
                fotoDao = db.fotoDao()
            )

            @Suppress("UNCHECKED_CAST")
            return ViveroViewModel(repository) as T
        }

        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
