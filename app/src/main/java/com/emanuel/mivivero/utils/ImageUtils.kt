package com.emanuel.mivivero.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import java.io.ByteArrayOutputStream

object ImageUtils {

    fun compressImage(context: Context, uri: Uri): ByteArray {

        val inputStream = context.contentResolver.openInputStream(uri)
        val bitmap = BitmapFactory.decodeStream(inputStream)

        val resized = Bitmap.createScaledBitmap(
            bitmap,
            1080,
            (bitmap.height * (1080f / bitmap.width)).toInt(),
            true
        )

        val output = ByteArrayOutputStream()

        resized.compress(Bitmap.CompressFormat.JPEG, 75, output)

        return output.toByteArray()
    }
}