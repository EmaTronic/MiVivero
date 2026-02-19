package com.emanuel.mivivero.ui.utils

import android.content.Context
import android.graphics.*
import android.net.Uri
import androidx.core.content.FileProvider
import com.emanuel.mivivero.R
import com.emanuel.mivivero.data.model.PlantaAlbum
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

object AlbumPublisher {

    // =========================================================
    // GENERAR PORTADA
    // =========================================================

    fun generarPortadaAlbum(
        context: Context,
        albumId: Long,
        nombreAlbum: String,
        fondoSeleccionado: Int, // 1 cactus | 2 tropical | 3 suculentas
        nombreVivero: String?,
        fechaHasta: Long?,
        modosPago: String?,
        mediosEnvio: String?,
        retiroEn: String?,
        observaciones: String?
    ): Uri {

        val size = 1080
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // =============================
        // FONDO SEGÚN SELECCIÓN
        // =============================

        val fondoRes = when (fondoSeleccionado) {
            1 -> R.drawable.bg_portada_cactus_1080
            2 -> R.drawable.bg_portada_tropical_1080
            else -> R.drawable.bg_portada_suculentas_1080_v2
        }

        val fondo = BitmapFactory.decodeResource(context.resources, fondoRes)
        val fondoEscalado = Bitmap.createScaledBitmap(fondo, size, size, true)
        canvas.drawBitmap(fondoEscalado, 0f, 0f, null)

        val centerX = size / 2f
        var y = 220f

        // =============================
        // LOGO APP
        // =============================

        val logo = BitmapFactory.decodeResource(
            context.resources,
            R.drawable.logo_mi_vivero_256
        )

        val logoSize = 180
        val logoEscalado = Bitmap.createScaledBitmap(logo, logoSize, logoSize, true)

        canvas.drawBitmap(
            logoEscalado,
            centerX - logoSize / 2f,
            40f,
            null
        )

        // =============================
        // TEXTOS
        // =============================

        val paintTitulo = Paint().apply {
            color = Color.parseColor("#2E4E3F")
            textSize = 80f
            isFakeBoldText = true
            textAlign = Paint.Align.CENTER
        }

        val paintTexto = Paint().apply {
            color = Color.parseColor("#333333")
            textSize = 55f
            textAlign = Paint.Align.CENTER
        }

        // Nombre vivero
        if (!nombreVivero.isNullOrBlank()) {
            canvas.drawText(nombreVivero, centerX, y, paintTexto)
            y += 80f
        }

        // Nombre álbum (siempre)
        canvas.drawText(nombreAlbum, centerX, y, paintTitulo)
        y += 110f

        // Línea separadora
        val linea = Paint().apply {
            color = Color.parseColor("#88444444")
            strokeWidth = 4f
        }

        canvas.drawLine(200f, y, size - 200f, y, linea)
        y += 90f

        fun drawCampo(label: String, valor: String?) {
            if (!valor.isNullOrBlank()) {
                canvas.drawText("$label: $valor", centerX, y, paintTexto)
                y += 80f
            }
        }

        drawCampo("Modos de pago", modosPago)
        drawCampo("Medios de envío", mediosEnvio)
        drawCampo("Retiro en", retiroEn)
        drawCampo("Observaciones", observaciones)

        if (fechaHasta != null) {
            val formato = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            drawCampo("Disponible hasta", formato.format(Date(fechaHasta)))
        }

        // =============================
        // GUARDAR ARCHIVO
        // =============================

        val file = File(
            context.cacheDir,
            "portada_album_${albumId}_${System.currentTimeMillis()}.jpg"
        )

        FileOutputStream(file).use { fos ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 95, fos)
        }

        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
    }

    // =========================================================
    // GENERAR IMÁGENES DE PLANTAS (TU FUNCIÓN ORIGINAL)
    // =========================================================

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

            // BLOQUE SUPERIOR
            canvas.drawRect(0f, 0f, width.toFloat(), 180f, fondo)

            canvas.drawText(
                nombreAlbum,
                padding,
                120f,
                paintTitulo
            )

            // BLOQUE INFERIOR
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

            // GUARDAR
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
