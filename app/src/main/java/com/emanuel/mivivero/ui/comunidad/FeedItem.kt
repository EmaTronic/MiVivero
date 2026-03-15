package com.emanuel.mivivero.ui.comunidad

import com.emanuel.mivivero.data.model.Publicacion

sealed class FeedItem {
    data class SectionHeader(
        val section: FeedSection,
        val expanded: Boolean
    ) : FeedItem()

    data class HorizontalCarousel(
        val section: FeedSection,
        val contentType: CarouselContentType,
        val items: List<HorizontalContentItem>
    ) : FeedItem()

    data class Filters(
        val section: FeedSection
    ) : FeedItem()
}

enum class FeedSection(val title: String) {
    DESTACADOS("Destacados"),
    MIS_PUBLICACIONES("Mis publicaciones"),
    ALBUMES("Álbumes"),
    COMUNIDAD("Comunidad")
}

enum class CarouselContentType {
    ARTICULO,
    PUBLICACION,
    ALBUM
}

sealed class HorizontalContentItem {
    abstract val stableId: String

    data class Articulo(
        override val stableId: String,
        val titulo: String,
        val descripcion: String,
        val imageResId: Int
    ) : HorizontalContentItem()

    data class PublicacionCard(
        override val stableId: String,
        val publicacion: Publicacion
    ) : HorizontalContentItem()

    data class AlbumCard(
        override val stableId: String,
        val titulo: String,
        val descripcion: String,
        val previewFotos: List<String>
    ) : HorizontalContentItem()
}
