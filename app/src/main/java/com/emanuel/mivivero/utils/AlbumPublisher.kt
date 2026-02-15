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

            val uriFoto = Uri.parse(planta.fotoRuta)

            val original = try {
                val inputStream = context.contentResolver.openInputStream(uriFoto)
                BitmapFactory.decodeStream(inputStream)
            } catch (e: Exception) {
                null
            }

            if (original == null) return@forEach

            // 🔥 ===== CORRECCIÓN ROTACIÓN EXIF =====
            val rotatedBitmap = try {

                val inputStream = context.contentResolver.openInputStream(uriFoto)
                val exif = androidx.exifinterface.media.ExifInterface(inputStream!!)

                val orientation = exif.getAttributeInt(
                    androidx.exifinterface.media.ExifInterface.TAG_ORIENTATION,
                    androidx.exifinterface.media.ExifInterface.ORIENTATION_NORMAL
                )

                val matrix = Matrix()

                when (orientation) {
                    androidx.exifinterface.media.ExifInterface.ORIENTATION_ROTATE_90 ->
                        matrix.postRotate(90f)

                    androidx.exifinterface.media.ExifInterface.ORIENTATION_ROTATE_180 ->
                        matrix.postRotate(180f)

                    androidx.exifinterface.media.ExifInterface.ORIENTATION_ROTATE_270 ->
                        matrix.postRotate(270f)
                }

                Bitmap.createBitmap(
                    original,
                    0,
                    0,
                    original.width,
                    original.height,
                    matrix,
                    true
                )

            } catch (e: Exception) {
                original
            }

            val mutable = rotatedBitmap.copy(Bitmap.Config.ARGB_8888, true)
            val canvas = Canvas(mutable)

            // ===============================
            // 🔧 ZONA DE AJUSTES VISUALES
            // ===============================

            val padding = 90f
            // 🔧 padding = distancia horizontal del texto al borde izquierdo

            val barHeight = 550f
            // 🔧 barHeight = altura del zócalo negro (arriba y abajo)
            //    ↑ subir este valor agranda la barra negra

            val paintTitulo = Paint().apply {
                color = Color.WHITE
                textSize = 200f
                // 🔧 textSize TÍTULO = tamaño del nombre del álbum
                isFakeBoldText = true
            }

            val paintTexto = Paint().apply {
                color = Color.WHITE
                textSize = 120f
                // 🔧 textSize TEXTO = tamaño planta / cantidad / precio
            }

            val paintBackground = Paint().apply {
                color = Color.parseColor("#CC000000")
                // 🔧 Cambiar transparencia:
                // "#AA000000" → más claro
                // "#FF000000" → negro sólido
            }

            // ===============================
            // 🔳 BARRA SUPERIOR
            // ===============================

            canvas.drawRect(
                0f,
                0f,
                mutable.width.toFloat(),
                barHeight,
                paintBackground
            )

            // ===============================
            // 🔳 BARRA INFERIOR
            // ===============================

            canvas.drawRect(
                0f,
                mutable.height - barHeight,
                mutable.width.toFloat(),
                mutable.height.toFloat(),
                paintBackground
            )

            // ===============================
            // 📝 TEXTO SUPERIOR (ÁLBUM)
            // ===============================

            canvas.drawText(
                nombreAlbum,
                padding,
                250f,
                paintTitulo
            )
            // 🔧 250f = altura vertical del nombre del álbum
            //    subir número → baja el texto
            //    bajar número → lo sube

            // ===============================
            // 📝 TEXTO INFERIOR
            // ===============================

            var y = mutable.height - 380f
            // 🔧 180f = distancia desde abajo donde arranca el texto inferior
            //    subir número → texto más arriba
            //    bajar número → texto más abajo

            canvas.drawText(
                "Planta: ${planta.nombre}",
                padding,
                y,
                paintTexto
            )

            y += 150f
            // 🔧 separación vertical entre líneas

            canvas.drawText(
                "Disponible: ${planta.cantidad}",
                padding,
                y,
                paintTexto
            )

            y += 150f
            // 🔧 separación vertical entre cantidad y precio

            canvas.drawText(
                "Precio: $${planta.precio}",
                padding,
                y,
                paintTexto
            )

            // ===============================
            // 💾 GUARDAR ARCHIVO
            // ===============================

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
