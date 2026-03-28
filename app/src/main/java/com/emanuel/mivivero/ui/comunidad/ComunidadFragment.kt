package com.emanuel.mivivero.ui.comunidad

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.emanuel.mivivero.R
import com.emanuel.mivivero.data.model.Publicacion
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class ComunidadFragment : Fragment(R.layout.fragment_comunidad) {

    private val db = FirebaseFirestore.getInstance()

    private lateinit var recyclerFeed: RecyclerView
    private lateinit var feedAdapter: ComunidadFeedAdapter

    private val expandedSections = mutableMapOf(
        FeedSection.DESTACADOS to false,
        FeedSection.MIS_PUBLICACIONES to false,
        FeedSection.ALBUMES to false,
        FeedSection.COMUNIDAD to false,
        FeedSection.COMUNIDAD to true
    )

    private val destacados: List<HorizontalContentItem.Articulo> by lazy {
        listOf(
            HorizontalContentItem.Articulo(
                "art-1",
                "Riego inteligente",
                "Cuándo regar según sustrato",
                R.drawable.bg_portada_cactus_1080
            ),
            HorizontalContentItem.Articulo(
                "art-2",
                "Luz ideal",
                "Cómo ubicar tus plantas en interior",
                R.drawable.bg_portada_tropical_1080
            ),
            HorizontalContentItem.Articulo(
                "art-3",
                "Hojas sanas",
                "Detecta plagas temprano",
                R.drawable.bg_portada_suculentas_1080_v2
            ),
            HorizontalContentItem.Articulo(
                "art-4",
                "Propagación",
                "Esquejes paso a paso",
                R.drawable.ic_img_bienvenida
            ),
            HorizontalContentItem.Articulo(
                "art-5",
                "Sustratos",
                "Mezclas recomendadas por especie",
                R.drawable.bg_album_placeholder
            )
        )
    }

    private var listaMisPublicaciones: List<Publicacion> = emptyList()
    private var listaComunidadCompleta: List<Publicacion> = emptyList()
    private var listaAlbumes: List<HorizontalContentItem.AlbumCard> = emptyList()

    private var filtroEstadoActual: String? = null
    private var textoBusquedaActual: String = ""

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerFeed = view.findViewById(R.id.recyclerFeedComunidad)
        recyclerFeed.layoutManager = LinearLayoutManager(requireContext())

        feedAdapter = ComunidadFeedAdapter(



            onAlbumClick = { albumId ->

                findNavController().navigate(
                    R.id.albumComunidadFragment,
                    Bundle().apply {
                        putString("albumId", albumId)
                    }
                )
            },

            onPublicacionClick = { publicacionId ->

                findNavController().navigate(
                    R.id.detallePublicacionFragment,
                    Bundle().apply {
                        putString("publicacionId", publicacionId)
                    }
                )
            },

            onToggleSection = { section ->
                expandedSections[section] = !(expandedSections[section] ?: false)
                reconstruirFeed()
            },

            onFiltroTodas = {
                filtroEstadoActual = null
                aplicarFiltros()
            },

            onFiltroPendientes = {
                filtroEstadoActual = "pendiente"
                aplicarFiltros()
            },

            onFiltroIdentificadas = {
                filtroEstadoActual = "identificada"
                aplicarFiltros()
            },

            onBuscar = { texto ->
                textoBusquedaActual = texto
                aplicarFiltros()
            }
        )

        recyclerFeed.adapter = feedAdapter

        reconstruirFeed()

        cargarMisPublicaciones()
        cargarComunidad()
        cargarAlbumes()


        val user = FirebaseAuth.getInstance().currentUser ?: return

        val uid = user.uid




    }

    private fun cargarMisPublicaciones() {

        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return

        db.collection("publicaciones")
            .whereEqualTo("uidAutor", uid)
            .get()
            .addOnSuccessListener { result ->

                listaMisPublicaciones = result.map { doc ->
                    doc.toObject(Publicacion::class.java).copy(id = doc.id)
                }

                reconstruirFeed()
            }
    }

    private fun cargarComunidad() {

        Log.d("COMUNIDAD_DEBUG", "🔥 INICIO cargarComunidad()")

        db.collection("publicaciones")
            .orderBy("prioridadEstado")
            .orderBy("fecha", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { result ->

                Log.d("COMUNIDAD_DEBUG", "✅ Firestore SUCCESS")
                Log.d("COMUNIDAD_DEBUG", "📦 Cantidad docs: ${result.size()}")

                if (result.isEmpty) {
                    Log.w("COMUNIDAD_DEBUG", "⚠️ RESULT VACÍO (Firestore respondió pero no hay datos)")
                }

                result.documents.forEachIndexed { index, doc ->

                    Log.d("COMUNIDAD_DEBUG", "📄 Doc[$index] ID: ${doc.id}")
                    Log.d("COMUNIDAD_DEBUG", "📄 Data: ${doc.data}")

                    val estado = doc.getString("estado")
                    val nombre = doc.getString("nombreComun")
                    val image = doc.getString("imageUrl")

                    Log.d("COMUNIDAD_DEBUG", "   → estado=$estado | nombre=$nombre | image=$image")
                }

                listaComunidadCompleta = result.mapNotNull { doc ->
                    try {
                        val pub = doc.toObject(Publicacion::class.java)?.copy(id = doc.id)

                        if (pub == null) {
                            Log.e("COMUNIDAD_DEBUG", "❌ MAP NULL en doc ${doc.id}")
                        }

                        pub

                    } catch (e: Exception) {
                        Log.e("COMUNIDAD_DEBUG", "❌ ERROR MAP doc ${doc.id}", e)
                        null
                    }
                }

                Log.d("COMUNIDAD_DEBUG", "📊 Lista mapeada size: ${listaComunidadCompleta.size}")

                aplicarFiltros()
            }
            .addOnFailureListener { e ->
                Log.e("COMUNIDAD_DEBUG", "❌ FIRESTORE ERROR", e)
            }
    }

    private fun cargarAlbumes() {


        db.collection("albumsFeed")
            .orderBy("fechaPublicacion", Query.Direction.DESCENDING)
            .limit(20)
            .get()
            .addOnSuccessListener { result ->

                Log.d("ALBUMES_FEED", "cantidad=${result.size()}")

                listaAlbumes = result.mapIndexed { index, doc ->

                    Log.d("ALBUM_DOC", doc.data.toString())

                    val raw = doc.get("previewFotos")
                    Log.d("ALBUM_DOC", "previewFotos raw = $raw  type=${raw?.javaClass}")

                    val preview = (raw as? List<*>)?.mapNotNull { it as? String }
                        ?: listOfNotNull(doc.getString("portadaUrl"))

                    Log.d("ALBUM_DOC", "previewFotos parsed = $preview")

                    HorizontalContentItem.AlbumCard(
                        stableId = doc.id,
                        titulo = doc.getString("titulo") ?: "Álbum ${index + 1}",
                        descripcion = "${doc.getLong("cantidadPlantas") ?: 0} plantas",
                        previewFotos = preview
                    )
                }
                reconstruirFeed()
            }
    }

    private fun aplicarFiltros() {

        Log.d("COMUNIDAD_DEBUG", "🎯 aplicarFiltros() input size: ${listaComunidadCompleta.size}")
        Log.d("COMUNIDAD_DEBUG", "🎯 filtroEstadoActual=$filtroEstadoActual textoBusqueda=$textoBusquedaActual")

        val listaFiltrada = listaComunidadCompleta
            .asSequence()

            .filter { publicacion ->
                filtroEstadoActual?.let { estado ->
                    publicacion.estado == estado
                } ?: true
            }

            .filter { publicacion ->
                textoBusquedaActual.isBlank() ||
                        publicacion.nombreComun?.contains(
                            textoBusquedaActual,
                            ignoreCase = true
                        ) == true
            }

            .toList()


        Log.d("COMUNIDAD_DEBUG", "🎯 listaFiltrada size: ${listaFiltrada.size}")

        reconstruirFeed(listaFiltrada)
    }

    private fun reconstruirFeed(
        publicacionesComunidad: List<Publicacion> = listaComunidadCompleta
    ) {

        val visibleItems = mutableListOf<FeedItem>()

        FeedSection.values().forEach { section ->

            val expanded = expandedSections[section] ?: false

            visibleItems += FeedItem.SectionHeader(section, expanded)

            if (!expanded) return@forEach

            when (section) {

                FeedSection.DESTACADOS -> {

                    visibleItems += FeedItem.HorizontalCarousel(
                        section = section,
                        contentType = CarouselContentType.ARTICULO,
                        items = destacados
                    )
                }

                FeedSection.MIS_PUBLICACIONES -> {

                    visibleItems += FeedItem.HorizontalCarousel(
                        section = section,
                        contentType = CarouselContentType.PUBLICACION,
                        items = listaMisPublicaciones.map {
                            HorizontalContentItem.PublicacionCard(it.id, it)
                        }
                    )
                }

                FeedSection.ALBUMES -> {

                    visibleItems += FeedItem.HorizontalCarousel(
                        section = section,
                        contentType = CarouselContentType.ALBUM,
                        items = listaAlbumes
                    )
                }

                FeedSection.COMUNIDAD -> {

                    visibleItems += FeedItem.Filters(section)

                    publicacionesComunidad
                        .chunked(8)
                        .forEachIndexed { index, grupo ->

                            visibleItems += FeedItem.HorizontalCarousel(
                                section = section,
                                contentType = CarouselContentType.PUBLICACION,
                                items = grupo.map { pub ->

                                    HorizontalContentItem.PublicacionCard(
                                        stableId = "${pub.id.ifBlank { "pub" }}-$index",
                                        publicacion = pub
                                    )
                                }
                            )
                        }
                }
            }
        }

        feedAdapter.submitList(visibleItems)
    }
}