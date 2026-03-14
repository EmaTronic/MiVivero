package com.emanuel.mivivero.ui.identificar

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import com.emanuel.mivivero.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.io.File
import com.google.android.material.snackbar.Snackbar
import androidx.navigation.fragment.findNavController

class IdentificarFragment : Fragment() {

    private lateinit var galleryLauncher: ActivityResultLauncher<String>
    private lateinit var etObservacion: EditText
    private lateinit var btnEnviar: Button
    private lateinit var permissionLauncher: ActivityResultLauncher<String>
    private lateinit var imgPreview: ImageView
    private lateinit var btnCamara: Button
    private lateinit var btnGaleria: Button
    private lateinit var tvResultado: TextView

    private var photoUri: Uri? = null
    private lateinit var cameraLauncher: ActivityResultLauncher<Uri>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        permissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { granted ->
            if (granted) {
                abrirCamara()
            } else {
                tvResultado.text = "Permiso de cámara denegado"
            }
        }

        cameraLauncher = registerForActivityResult(
            ActivityResultContracts.TakePicture()
        ) { success ->
            if (success) {
                photoUri?.let {
                    imgPreview.setImageURI(it)
                    tvResultado.text = "Foto capturada correctamente"
                }
            } else {
                tvResultado.text = "No se pudo capturar la imagen"
            }
        }

        galleryLauncher = registerForActivityResult(
            ActivityResultContracts.GetContent()
        ) { uri: Uri? ->
            uri?.let {
                photoUri = it
                imgPreview.setImageURI(it)
                tvResultado.text = "Imagen seleccionada de galería"
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val view = inflater.inflate(R.layout.fragment_identificar, container, false)

        imgPreview = view.findViewById(R.id.imgPreview)
        btnCamara = view.findViewById(R.id.btnCamara)
        btnGaleria = view.findViewById(R.id.btnGaleria)
        tvResultado = view.findViewById(R.id.tvResultado)
        etObservacion = view.findViewById(R.id.etObservacion)
        btnEnviar = view.findViewById(R.id.btnEnviar)

        btnCamara.setOnClickListener {
            permissionLauncher.launch(android.Manifest.permission.CAMERA)
        }

        btnGaleria.setOnClickListener {
            galleryLauncher.launch("image/*")
        }

        btnEnviar.setOnClickListener {

            val auth = FirebaseAuth.getInstance()
            val user = auth.currentUser

            if (user == null) {

                Snackbar.make(
                    requireView(),
                    "Debes iniciar sesión para publicar",
                    Snackbar.LENGTH_LONG
                ).setAction("Ingresar") {
                    findNavController().navigate(R.id.authFragment)
                }.show()

                return@setOnClickListener
            }

            if (photoUri == null) {
                tvResultado.text = "Tomá una foto primero"
                return@setOnClickListener
            }

            val observacion = etObservacion.text.toString()

            if (observacion.isBlank()) {
                tvResultado.text = "Agregá una observación"
                return@setOnClickListener
            }

            user.reload().addOnSuccessListener {

                if (!user.isEmailVerified) {

                    Snackbar.make(
                        requireView(),
                        "Debes verificar tu correo antes de publicar",
                        Snackbar.LENGTH_LONG
                    ).setAction("Reenviar") {
                        user.sendEmailVerification()
                    }.show()

                    return@addOnSuccessListener
                }

                btnEnviar.isEnabled = false

                subirPublicacion(observacion, user.uid, user.email)
            }
        }

        return view
    }

    private fun abrirCamara() {
        val uri = crearArchivoTemporal()
        cameraLauncher.launch(uri)
    }

    private fun crearArchivoTemporal(): Uri {

        val file = File(
            requireContext().cacheDir,
            "foto_${System.currentTimeMillis()}.jpg"
        )

        photoUri = FileProvider.getUriForFile(
            requireContext(),
            "${requireContext().packageName}.fileprovider",
            file
        )

        return photoUri!!
    }

    private fun subirPublicacion(
        observacion: String,
        uid: String,
        email: String?
    ) {

        tvResultado.text = "Iniciando subida..."

        val uri = photoUri ?: return

        val storage = FirebaseStorage.getInstance()
        val storageRef = storage.reference

        val fileName = "publicaciones/$uid/${System.currentTimeMillis()}.jpg"
        val fileRef = storageRef.child(fileName)

        val compressedUri = comprimirImagen(uri)

        fileRef.putFile(compressedUri)

            .addOnSuccessListener {

                tvResultado.text = "Imagen subida correctamente"

                fileRef.downloadUrl.addOnSuccessListener { downloadUri ->

                    tvResultado.text = "URL obtenida"

                    val db = FirebaseFirestore.getInstance()

                    val publicacion = hashMapOf(
                        "uidAutor" to uid,
                        "imageUrl" to downloadUri.toString(),
                        "observacion" to observacion,
                        "fecha" to FieldValue.serverTimestamp()
                    )

                    db.collection("publicaciones")
                        .add(publicacion)
                        .addOnSuccessListener {

                            tvResultado.text = "PUBLICACIÓN CREADA"

                        }
                        .addOnFailureListener { e ->

                            tvResultado.text = "ERROR FIRESTORE: ${e.message}"

                        }

                }

            }

            .addOnFailureListener { e ->

                tvResultado.text = "ERROR STORAGE: ${e.message}"

            }
    }

    private fun comprimirImagen(uri: Uri): Uri {

        val inputStream = requireContext().contentResolver.openInputStream(uri)
        val bitmap = android.graphics.BitmapFactory.decodeStream(inputStream)

        inputStream?.close()

        val maxWidth = 1080
        val ratio = bitmap.width.toFloat() / bitmap.height.toFloat()

        val newWidth = maxWidth
        val newHeight = (maxWidth / ratio).toInt()

        val resizedBitmap = android.graphics.Bitmap.createScaledBitmap(
            bitmap,
            newWidth,
            newHeight,
            true
        )

        val file = File(
            requireContext().cacheDir,
            "compressed_${System.currentTimeMillis()}.jpg"
        )

        val outputStream = file.outputStream()

        resizedBitmap.compress(
            android.graphics.Bitmap.CompressFormat.JPEG,
            70,
            outputStream
        )

        outputStream.flush()
        outputStream.close()

        return FileProvider.getUriForFile(
            requireContext(),
            "${requireContext().packageName}.fileprovider",
            file
        )
    }
}