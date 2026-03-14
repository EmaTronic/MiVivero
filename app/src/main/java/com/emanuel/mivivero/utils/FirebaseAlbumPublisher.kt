package com.emanuel.mivivero.ui.utils

import android.net.Uri
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

object FirebaseAlbumPublisher {

    private val db = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()

    fun publicarAlbum(
        albumId: String,
        titulo: String,
        portadaUri: Uri,
        fotos: List<Uri>
    ) {

        android.util.Log.d("AUTH_CHECK", FirebaseAuth.getInstance().currentUser?.uid ?: "USER_NULL")
        val uid = FirebaseAuth.getInstance().uid ?: return

        android.util.Log.d(
            "AUTH_DEBUG",
            FirebaseAuth.getInstance().currentUser?.uid ?: "USER_NULL"
        )

        val portadaRef =
            storage.reference.child("albumes/$uid/$albumId/portada.jpg")

        portadaRef.putFile(portadaUri)
            .continueWithTask { portadaRef.downloadUrl }
            .addOnSuccessListener { portadaUrl ->

                val fotosUrls = mutableListOf<String>()

                fotos.forEachIndexed { index, uri ->

                    val ref = storage.reference.child(
                        "albumes/$uid/$albumId/planta_${index + 1}.jpg"
                    )

                    ref.putFile(uri)
                        .continueWithTask { ref.downloadUrl }
                        .addOnSuccessListener { url ->

                            fotosUrls.add(url.toString())

                            if (fotosUrls.size == fotos.size) {

                                crearDocumentosFirestore(
                                    albumId,
                                    uid,
                                    titulo,
                                    portadaUrl.toString(),
                                    fotosUrls
                                )
                            }
                        }
                }
            }
    }

    private fun crearDocumentosFirestore(
        albumId: String,
        uid: String,
        titulo: String,
        portadaUrl: String,
        fotos: List<String>
    ) {

        val feed = hashMapOf(

            "albumId" to albumId,
            "uidAutor" to uid,
            "titulo" to titulo,

            "portadaUrl" to portadaUrl,

            "cantidadPlantas" to fotos.size,

            "comentariosCount" to 0,
            "reservasCount" to 0,
            "likesCount" to 0,

            "score" to 0.0,

            "fechaPublicacion" to FieldValue.serverTimestamp(),

            "estado" to "activo"
        )

        db.collection("albumsFeed")
            .document(albumId)
            .set(feed)

        val fotosMap = fotos.mapIndexed { index, url ->

            mapOf(
                "url" to url,
                "plantaIndex" to (index + 1)
            )
        }

        val detalle = hashMapOf(
            "albumId" to albumId,
            "uidAutor" to uid,
            "fotos" to fotosMap
        )

        db.collection("albumsDetalle")
            .document(albumId)
            .set(detalle)
    }
}