package com.emanuel.mivivero.ui.utils

import android.content.Context
import android.graphics.*
import android.net.Uri
import androidx.core.content.FileProvider
import com.emanuel.mivivero.data.model.PlantaAlbum
import java.io.File
import java.io.FileOutputStream

object AlbumPublisher {

    fun generarImagenesAlbum(
        context: Context,
        plantas: List<PlantaAlbum>,
        nombreAlbum: String
    ): List<Uri> {

        val uris = mutableListOf<Uri>()

        plantas.forEach { planta ->

            val original = try {
                val inputStream = context.contentResolver.openInputStream(
                    Uri.parse(planta.fotoRuta)
                )
                BitmapFactory.decodeStream(inputStream)
            } catch (e: Exception) {
                null
            }

            if (original == null) return@forEach

            val mutable = original.copy(Bitmap.Config.ARGB_8888, true)
            val canvas = Canvas(mutable)

            val width = mutable.width
            val height = mutable.height

            val padding = 40f

            val paintTitulo = Paint().apply {
                color = Color.WHITE
                textSize = 110f
                isFakeBoldText = true
                setShadowLayer(8f, 0f, 0f, Color.BLACK)
            }

            val paintTexto = Paint().apply {
                color = Color.WHITE
                textSize = 85f
                setShadowLayer(6f, 0f, 0f, Color.BLACK)
            }

            val fondo = Paint().apply {
                color = Color.parseColor("#AA000000")
            }

            // =============================
            // 🔥 BLOQUE SUPERIOR (ÁLBUM)
            // =============================

            canvas.drawRect(
                0f,
                0f,
                width.toFloat(),
                180f,
                fondo
            )

            canvas.drawText(
                nombreAlbum,
                padding,
                120f,
                paintTitulo
            )

            // =============================
            // 🔥 BLOQUE INFERIOR (DATOS)
            // =============================

            val bloqueAltura = 350f
            val topBloque = height - bloqueAltura

            canvas.drawRect(
                0f,
                topBloque,
                width.toFloat(),
                height.toFloat(),
                fondo
            )

            var y = topBloque + 120f

            canvas.drawText(planta.nombre, padding, y, paintTexto)

            y += 95f

            canvas.drawText("Disponible: ${planta.cantidad}", padding, y, paintTexto)

            y += 95f

            canvas.drawText("Precio: $${planta.precio}", padding, y, paintTexto)

            // =============================
            // GUARDAR ARCHIVO
            // =============================

            val file = File(
                context.cacheDir,
                "album_${planta.plantaId}_${System.currentTimeMillis()}.jpg"
            )

            FileOutputStream(file).use { fos ->
                mutable.compress(Bitmap.CompressFormat.JPEG, 95, fos)
            }

            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            uris.add(uri)
        }

        return uris
    }

}
