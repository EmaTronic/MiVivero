package com.emanuel.mivivero.data.firebase

import android.content.Context
import android.net.Uri
import android.util.Log
import com.emanuel.mivivero.utils.ImageUtils
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.tasks.await
import java.util.*

object AlbumRepository {

    private val db = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance().reference

    suspend fun publicarAlbum(
        context: Context,
        titulo: String,
        categoria: String,
        pais: String,
        provincia: String,
        ciudad: String,
        lat: Double?,
        lng: Double?,
        imagenes: List<Uri>
    ){

        val uid = FirebaseAuth.getInstance().currentUser?.uid
            ?: throw Exception("Usuario no autenticado")

        val albumId = db.collection("albumsFeed").document().id

        if (imagenes.isEmpty()) {
            throw Exception("Sin imágenes")
        }


        // =========================
        // 1. Subir imágenes
        // =========================

        val urls = mutableListOf<String>()

        val uploadedRefs = mutableListOf<StorageReference>()

        imagenes.forEachIndexed { index, uri ->

            val ref = storage
                .child("albumsFeed")
                .child(uid)
                .child(albumId)
                .child("${System.currentTimeMillis()}_$index.jpg")

            Log.d("STORAGE_PATH", ref.path)

            val bytes = ImageUtils.compressImage(context, uri)

            try {
                ref.putBytes(bytes).await()
            } catch (e: Exception) {
                Log.e("UPLOAD", "Error subiendo imagen $index", e)
                throw Exception("Falló la subida de imágenes")
            }

            uploadedRefs.add(ref)

            val url = ref.downloadUrl.await().toString()
            urls.add(url)
        }


        if (urls.isEmpty()) {
            throw Exception("No se pudo subir ninguna imagen")
        }

        Log.d("ALBUM_UPLOAD", "imagenes subidas=${urls.size}")

        val portada = urls.first()


        Log.d("ALBUM_UPLOAD", "urls = $urls")
        // =========================
        // 2. albumsFeed (liviano)
        // =========================

        val feed = hashMapOf(
            "albumId" to albumId,
            "uidAutor" to uid,
            "titulo" to titulo,
            "categoria" to categoria,

            "pais" to pais,
            "provincia" to provincia,
            "ciudad" to ciudad,

            "paisLower" to pais.lowercase(Locale.getDefault()),
            "provinciaLower" to provincia.lowercase(Locale.getDefault()),
            "ciudadLower" to ciudad.lowercase(Locale.getDefault()),

            "lat" to lat,
            "lng" to lng,

            "portadaUrl" to portada,

            "previewFotos" to urls.take(4),

            "cantidadPlantas" to (urls.size - 1),
            "comentariosCount" to 0,

            "fechaPublicacion" to FieldValue.serverTimestamp()
        )

        // =========================
        // 3. albumsDetalle (fotos)
        // =========================

        val fotos = urls.drop(1).mapIndexed { index, url ->
            hashMapOf(
                "url" to url,
                "plantaIndex" to index + 1
            )
        }

        val detalle = hashMapOf(
            "albumId" to albumId,
            "uidAutor" to uid,
            "fotos" to fotos
        )


        val feedRef = db.collection("albumsFeed").document(albumId)
        val detalleRef = db.collection("albumsDetalle").document(albumId)

        val batch = db.batch()

        batch.set(feedRef, feed)
        batch.set(detalleRef, detalle)

        try {
            batch.commit().await()
        } catch (e: Exception) {

            Log.e("FIRESTORE", "Error guardando álbum", e)

            // 🔴 rollback
            uploadedRefs.forEach {
                try { it.delete() } catch (_: Exception) {}
            }

            throw Exception("Error guardando álbum")
        }
    }
}