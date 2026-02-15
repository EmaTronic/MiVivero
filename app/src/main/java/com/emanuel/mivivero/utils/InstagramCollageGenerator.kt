package com.emanuel.mivivero.utils


import android.content.Context
import android.graphics.*
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream

object InstagramCollageGenerator {

    fun generarCollageVertical(
        context: Context,
        uris: List<Uri>,
        nombreVivero: String
    ): Uri {

        val bitmaps = uris.map {
            context.contentResolver.openInputStream(it).use { input ->
                BitmapFactory.decodeStream(input)
            }
        }

        val ancho = 1080
        val altoFinal = 1350

        val collage = Bitmap.createBitmap(ancho, altoFinal, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(collage)

        canvas.drawColor(Color.WHITE)

        val paintTitulo = Paint().apply {
            color = Color.BLACK
            textSize = 60f
            typeface = Typeface.DEFAULT_BOLD
            isAntiAlias = true
        }

        canvas.drawText(nombreVivero, 60f, 100f, paintTitulo)

        val espacioDisponible = altoFinal - 200
        val altoPorImagen = espacioDisponible / bitmaps.size

        var yActual = 150

        bitmaps.forEach { bmp ->

            val scaled = Bitmap.createScaledBitmap(
                bmp,
                ancho - 120,
                altoPorImagen - 20,
                true
            )

            canvas.drawBitmap(scaled, 60f, yActual.toFloat(), null)

            yActual += altoPorImagen
        }

        val archivo = File(
            context.cacheDir,
            "instagram_collage_${System.currentTimeMillis()}.jpg"
        )

        FileOutputStream(archivo).use {
            collage.compress(Bitmap.CompressFormat.JPEG, 95, it)
        }

        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            archivo
        )
    }
}
