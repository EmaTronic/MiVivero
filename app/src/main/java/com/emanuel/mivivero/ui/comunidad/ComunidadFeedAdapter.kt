package com.emanuel.mivivero.ui.comunidad

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.emanuel.mivivero.R


class ComunidadFeedAdapter(
    private val onAlbumClick: (String) -> Unit,
    private val onPublicacionClick: (String) -> Unit,
    private val onToggleSection: (FeedSection) -> Unit,
    private val onFiltroTodas: () -> Unit,
    private val onFiltroPendientes: () -> Unit,
    private val onFiltroIdentificadas: () -> Unit,
    private val onBuscar: (String) -> Unit
) : ListAdapter<FeedItem, RecyclerView.ViewHolder>(FeedItemDiffCallback()) {

    private val sharedViewPool = RecyclerView.RecycledViewPool()

    companion object {
        private const val TYPE_HEADER = 0
        private const val TYPE_CAROUSEL = 1
        private const val TYPE_FILTERS = 2
    }

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is FeedItem.SectionHeader -> TYPE_HEADER
            is FeedItem.HorizontalCarousel -> TYPE_CAROUSEL
            is FeedItem.Filters -> TYPE_FILTERS
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        val inflater = LayoutInflater.from(parent.context)

        return when (viewType) {

            TYPE_HEADER -> SectionHeaderViewHolder(
                inflater.inflate(R.layout.item_feed_section_header, parent, false)
            )

            TYPE_CAROUSEL -> HorizontalCarouselViewHolder(
                inflater.inflate(R.layout.item_feed_horizontal_carousel, parent, false)
            )

            else -> FiltersViewHolder(
                inflater.inflate(R.layout.item_feed_filters, parent, false)
            )
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        when (val item = getItem(position)) {

            is FeedItem.SectionHeader ->
                (holder as SectionHeaderViewHolder).bind(item)

            is FeedItem.HorizontalCarousel ->
                (holder as HorizontalCarouselViewHolder).bind(item)

            is FeedItem.Filters ->
                (holder as FiltersViewHolder).bind()
        }
    }

    inner class SectionHeaderViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        private val tvTitle: TextView = view.findViewById(R.id.tvSectionTitle)
        private val tvIcon: TextView = view.findViewById(R.id.tvSectionIcon)

        fun bind(item: FeedItem.SectionHeader) {

            tvTitle.text = item.section.title
            tvIcon.text = if (item.expanded) "➖" else "➕"

            itemView.setOnClickListener { onToggleSection(item.section) }
            tvIcon.setOnClickListener { onToggleSection(item.section) }
        }
    }

    inner class HorizontalCarouselViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        private val recycler: RecyclerView = view.findViewById(R.id.recyclerHorizontal)
        private val tvEmpty: TextView = view.findViewById(R.id.tvCarouselEmpty)

        fun bind(item: FeedItem.HorizontalCarousel) {

            tvEmpty.isVisible = item.items.isEmpty()
            recycler.isVisible = item.items.isNotEmpty()

            if (item.items.isEmpty()) {
                tvEmpty.text = itemView.context.getString(R.string.comunidad_sin_elementos)
                return
            }

            val layoutManager =
                recycler.layoutManager as? LinearLayoutManager
                    ?: LinearLayoutManager(
                        itemView.context,
                        LinearLayoutManager.HORIZONTAL,
                        false
                    ).also { recycler.layoutManager = it }

            layoutManager.initialPrefetchItemCount = 4

            recycler.setRecycledViewPool(sharedViewPool)
            val adapter =
                recycler.adapter as? HorizontalContentAdapter
                    ?: HorizontalContentAdapter(onAlbumClick, onPublicacionClick)
                        .also { recycler.adapter = it }

            adapter.submitList(item.items)
        }
    }

    inner class FiltersViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        private val btnTodas: Button = view.findViewById(R.id.btnFiltroTodas)
        private val btnPendientes: Button = view.findViewById(R.id.btnFiltroPendientes)
        private val btnIdentificadas: Button = view.findViewById(R.id.btnFiltroIdentificadas)
        private val btnBuscar: Button = view.findViewById(R.id.btnBuscar)
        private val etBuscar: EditText = view.findViewById(R.id.etBuscarNombre)

        fun bind() {

            btnTodas.setOnClickListener { onFiltroTodas() }
            btnPendientes.setOnClickListener { onFiltroPendientes() }
            btnIdentificadas.setOnClickListener { onFiltroIdentificadas() }

            btnBuscar.setOnClickListener {
                onBuscar(etBuscar.text.toString())
            }
        }
    }
}

private class FeedItemDiffCallback : DiffUtil.ItemCallback<FeedItem>() {

    override fun areItemsTheSame(oldItem: FeedItem, newItem: FeedItem): Boolean {

        return when {

            oldItem is FeedItem.SectionHeader && newItem is FeedItem.SectionHeader ->
                oldItem.section == newItem.section

            oldItem is FeedItem.Filters && newItem is FeedItem.Filters ->
                oldItem.section == newItem.section

            oldItem is FeedItem.HorizontalCarousel && newItem is FeedItem.HorizontalCarousel ->
                oldItem.section == newItem.section &&
                        oldItem.contentType == newItem.contentType

            else -> false
        }
    }

    override fun areContentsTheSame(oldItem: FeedItem, newItem: FeedItem): Boolean {
        return oldItem == newItem
    }
}

private class HorizontalContentAdapter(
    private val onAlbumClick: (String) -> Unit,
    private val onPublicacionClick: (String) -> Unit
) :

    ListAdapter<HorizontalContentItem, RecyclerView.ViewHolder>(HorizontalContentDiffCallback()) {

    companion object {
        private const val TYPE_ARTICULO = 0
        private const val TYPE_PUBLICACION = 1
        private const val TYPE_ALBUM = 2
    }

    override fun getItemViewType(position: Int): Int {

        return when (getItem(position)) {

            is HorizontalContentItem.Articulo -> TYPE_ARTICULO
            is HorizontalContentItem.PublicacionCard -> TYPE_PUBLICACION
            is HorizontalContentItem.AlbumCard -> TYPE_ALBUM
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        val inflater = LayoutInflater.from(parent.context)

        return when (viewType) {

            TYPE_ARTICULO -> ArticuloViewHolder(
                inflater.inflate(R.layout.item_horizontal_articulo, parent, false)
            )

            TYPE_ALBUM -> AlbumViewHolder(
                inflater.inflate(R.layout.item_horizontal_album, parent, false),
                onAlbumClick
            )

            TYPE_PUBLICACION -> PublicacionViewHolder(
                inflater.inflate(R.layout.item_publicacion_card, parent, false),
                onPublicacionClick
            )
            else -> throw IllegalArgumentException("Tipo desconocido: $viewType")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        when (val item = getItem(position)) {

            is HorizontalContentItem.Articulo ->
                (holder as ArticuloViewHolder).bind(item)

            is HorizontalContentItem.PublicacionCard ->
                (holder as PublicacionViewHolder).bind(item)

            is HorizontalContentItem.AlbumCard ->
                (holder as AlbumViewHolder).bind(item)
        }
    }

    class ArticuloViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        private val img: ImageView = view.findViewById(R.id.imgArticulo)
        private val tvTitulo: TextView = view.findViewById(R.id.tvArticuloTitulo)
        private val tvDescripcion: TextView = view.findViewById(R.id.tvArticuloDescripcion)

        fun bind(item: HorizontalContentItem.Articulo) {

            img.setImageResource(item.imageResId)
            tvTitulo.text = item.titulo
            tvDescripcion.text = item.descripcion
        }
    }

    class PublicacionViewHolder(
        view: View,
        private val onPublicacionClick: (String) -> Unit
    ) : RecyclerView.ViewHolder(view) {

        private val imgPlanta: ImageView = view.findViewById(R.id.imgPlanta)
        private val tvNombre: TextView = view.findViewById(R.id.tvNombreComun)
        private val tvEstado: TextView = view.findViewById(R.id.tvEstado)
        private val tvAutor: TextView = view.findViewById(R.id.tvAutor)

        fun bind(item: HorizontalContentItem.PublicacionCard) {

            val publicacion = item.publicacion


            Log.d("DEBUG_PUBLICACION", publicacion.toString())

            tvNombre.text = publicacion.nombreComun ?: "Planta"
            tvEstado.text = publicacion.estado ?: "pendiente"

            tvAutor.text =
                itemView.context.getString(
                    R.string.comunidad_autor,
                    publicacion.nickAutor ?: "usuario"
                )

            Log.d("GLIDE_URL", publicacion.imageUrl ?: "URL NULL")

            Glide.with(itemView)
                .load(publicacion.imageUrl)
                .placeholder(R.drawable.ic_planta_placeholder)
                .error(R.drawable.ic_planta_placeholder)
                .diskCacheStrategy(com.bumptech.glide.load.engine.DiskCacheStrategy.ALL)
                .into(imgPlanta)


            itemView.setOnClickListener {
                onPublicacionClick(publicacion.id)
            }
        }
    }

    class AlbumViewHolder(
        view: View,
        private val onAlbumClick: (String) -> Unit
    ) : RecyclerView.ViewHolder(view) {

        private val img1: ImageView = view.findViewById(R.id.img1)
        private val img2: ImageView = view.findViewById(R.id.img2)
        private val img3: ImageView = view.findViewById(R.id.img3)
        private val img4: ImageView = view.findViewById(R.id.img4)


        private val tvTitulo: TextView = view.findViewById(R.id.tvAlbumTitulo)
        private val tvUsuario: TextView = view.findViewById(R.id.tvAlbumUsuario)
        private val tvCantidad: TextView = view.findViewById(R.id.tvAlbumCantidad)
        private val tvFecha: TextView = view.findViewById(R.id.tvAlbumFecha)

        fun bind(item: HorizontalContentItem.AlbumCard) {

            tvTitulo.text = item.titulo

            val imgs = listOf(img1, img2, img3, img4)

            for (i in imgs.indices) {

                val url = item.previewFotos.getOrNull(i)

                Glide.with(itemView)
                    .load(url)
                    .placeholder(R.drawable.bg_album_placeholder)
                    .error(R.drawable.bg_album_placeholder)
                    .centerCrop()
                    .into(imgs[i])
            }

            itemView.setOnClickListener {
                onAlbumClick(item.stableId)
            }
        }
    }
}

private class HorizontalContentDiffCallback :
    DiffUtil.ItemCallback<HorizontalContentItem>() {

    override fun areItemsTheSame(
        oldItem: HorizontalContentItem,
        newItem: HorizontalContentItem
    ): Boolean {
        return oldItem.stableId == newItem.stableId
    }

    override fun areContentsTheSame(
        oldItem: HorizontalContentItem,
        newItem: HorizontalContentItem
    ): Boolean {
        return oldItem == newItem
    }
}