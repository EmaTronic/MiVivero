package com.emanuel.mivivero.ui.utils

import android.content.Context
import android.graphics.*
import android.net.Uri
import com.emanuel.mivivero.data.model.PlantaAlbum
import java.io.File
import java.io.FileOutputStream

object AlbumPublisher {

    fun generarImagenesAlbum(
        context: Context,
        plantas: List<PlantaAlbum>,
        nombreVivero: String
    ): List<Uri> {

        val uris = mutableListOf<Uri>()

        plantas.forEachIndexed { index, planta ->

            val bitmapFinal =
                generarImagenCuadrada(
                    context,
                    planta,
                    nombreVivero
                )

            val archivo = File(
                context.cacheDir,
                "album_publicado_$index.jpg"
            )

            FileOutputStream(archivo).use {
                bitmapFinal.compress(
                    Bitmap.CompressFormat.JPEG,
                    95,
                    it
                )
            }

            val uri = androidx.core.content.FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                archivo
            )


            uris.add(uri)

        }

        return uris
    }

    private fun generarImagenCuadrada(
        context: Context,
        planta: PlantaAlbum,
        nombreVivero: String
    ): Bitmap {

        val size = 1080
        val bitmapFinal =
            Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)

        val canvas = Canvas(bitmapFinal)

        // Fondo negro
        canvas.drawColor(Color.BLACK)

        // === FOTO ORIGINAL (CORREGIDO) ===
        val original: Bitmap? = try {

            val uri = Uri.parse(planta.fotoRuta)

            val inputStream = context.contentResolver.openInputStream(uri)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()

            bitmap

        } catch (e: Exception) {
            null
        }

        if (original == null) {
            // fallback gris
            val fallback = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
            val canvasFallback = Canvas(fallback)
            canvasFallback.drawColor(Color.DKGRAY)
            return fallback
        }

        val scaled =
            escalarYRecortarACuadrado(original, size)

        canvas.drawBitmap(scaled, 0f, 0f, null)

        // === OVERLAY NEGRO INFERIOR ===
        val alturaOverlay = (size * 0.28f).toInt()

        val paintOverlay = Paint().apply {
            color = Color.parseColor("#CC000000")
        }

        canvas.drawRect(
            0f,
            size - alturaOverlay.toFloat(),
            size.toFloat(),
            size.toFloat(),
            paintOverlay
        )

        // === TEXTOS ===
        val padding = 60f
        var y = size - alturaOverlay + 90f

        val paintNombre = Paint().apply {
            color = Color.WHITE
            textSize = 50f
            isAntiAlias = true
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }

        val paintPrecio = Paint().apply {
            color = Color.WHITE
            textSize = 60f
            isAntiAlias = true
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }

        val paintCantidad = Paint().apply {
            color = Color.LTGRAY
            textSize = 40f
            isAntiAlias = true
        }

        canvas.drawText(nombreVivero, padding, y, paintNombre)

        y += 70f

        canvas.drawText("$${planta.precio}", padding, y, paintPrecio)

        y += 60f

        canvas.drawText("Disponible: ${planta.cantidad}", padding, y, paintCantidad)

        return bitmapFinal
    }



    private fun escalarYRecortarACuadrado(
        bitmap: Bitmap,
        size: Int
    ): Bitmap {

        val ratio =
            maxOf(
                size.toFloat() / bitmap.width,
                size.toFloat() / bitmap.height
            )

        val newWidth = (bitmap.width * ratio).toInt()
        val newHeight = (bitmap.height * ratio).toInt()

        val scaled =
            Bitmap.createScaledBitmap(
                bitmap,
                newWidth,
                newHeight,
                true
            )

        val x = (newWidth - size) / 2
        val y = (newHeight - size) / 2

        return Bitmap.createBitmap(
            scaled,
            x,
            y,
            size,
            size
        )
    }
}
