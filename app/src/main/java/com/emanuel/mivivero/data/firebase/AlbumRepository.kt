package com.emanuel.mivivero.data.firebase

import android.content.Context
import android.net.Uri
import android.util.Log
import com.emanuel.mivivero.utils.ImageUtils
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
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



        imagenes.forEachIndexed { index, uri ->

            val ref = storage
                .child("albumsFeed")
                .child(uid)                 // 🔴 CLAVE
                .child(albumId)
                .child("img_$index.jpg")


            Log.d("STORAGE_PATH", ref.path)

            val bytes = ImageUtils.compressImage(context, uri)

            ref.putBytes(bytes).await()

            val url = ref.downloadUrl.await().toString()

            urls.add(url)
        }

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


        val batch = db.batch()

        val feedRef = db.collection("albumsFeed").document(albumId)
        val detalleRef = db.collection("albumsDetalle").document(albumId)

        batch.set(feedRef, feed)
        batch.set(detalleRef, detalle)

        batch.commit().await()
    }
}