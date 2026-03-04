package com.emanuel.mivivero.ui.comunidad.adapter

import android.util.SparseIntArray
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.emanuel.mivivero.R
import com.emanuel.mivivero.data.model.Publicacion

class ComunidadFeedAdapter(
    private val misPublicaciones: List<Publicacion>,
    private val carruselesComunidad: List<List<Publicacion>>,
    private val onFiltroTodas: () -> Unit,
    private val onFiltroPendientes: () -> Unit,
    private val onFiltroIdentificadas: () -> Unit,
    private val onBuscar: (String) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {

        const val TYPE_ARTICULO = 0
        const val TYPE_MIS_PUBLICACIONES = 1
        const val TYPE_FILTROS = 2
        const val TYPE_CARRUSEL = 3
    }

    // pool compartido para todos los carruseles
    private val viewPool = RecyclerView.RecycledViewPool()

    // guardar posición de scroll de carruseles
    private val scrollStates = SparseIntArray()

    override fun getItemCount(): Int {

        return 3 + carruselesComunidad.size
    }

    override fun getItemViewType(position: Int): Int {

        return when (position) {

            0 -> TYPE_ARTICULO
            1 -> TYPE_MIS_PUBLICACIONES
            2 -> TYPE_FILTROS
            else -> TYPE_CARRUSEL
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RecyclerView.ViewHolder {

        val inflater = LayoutInflater.from(parent.context)

        return when (viewType) {

            TYPE_ARTICULO -> {

                val view = inflater.inflate(
                    R.layout.item_articulo_destacado,
                    parent,
                    false
                )

                ArticuloViewHolder(view)
            }

            TYPE_MIS_PUBLICACIONES -> {

                val view = inflater.inflate(
                    R.layout.item_seccion_mis_publicaciones,
                    parent,
                    false
                )

                MisPublicacionesViewHolder(view)
            }

            TYPE_FILTROS -> {

                val view = inflater.inflate(
                    R.layout.item_filtros_comunidad,
                    parent,
                    false
                )

                FiltrosViewHolder(view)
            }

            else -> {

                val view = inflater.inflate(
                    R.layout.item_carrusel_comunidad,
                    parent,
                    false
                )

                CarruselViewHolder(view)
            }
        }
    }

    override fun onBindViewHolder(


        holder: RecyclerView.ViewHolder,
        position: Int
    ) {

        when (holder) {

            is MisPublicacionesViewHolder -> {

                holder.recycler.layoutManager =
                    LinearLayoutManager(
                        holder.recycler.context,
                        LinearLayoutManager.HORIZONTAL,
                        false
                    )

                holder.recycler.setRecycledViewPool(viewPool)

                holder.recycler.adapter =
                    PublicacionesCarruselAdapter(misPublicaciones)
            }

            is FiltrosViewHolder -> {

                holder.btnTodas.setOnClickListener {

                    onFiltroTodas()
                }

                holder.btnPendientes.setOnClickListener {

                    onFiltroPendientes()
                }

                holder.btnIdentificadas.setOnClickListener {

                    onFiltroIdentificadas()
                }

                holder.btnBuscar.setOnClickListener {

                    val texto = holder.etBuscar.text.toString()

                    onBuscar(texto)
                }
            }

            is CarruselViewHolder -> {

                val lista = carruselesComunidad[position - 3]

                val layoutManager =
                    LinearLayoutManager(
                        holder.recycler.context,
                        LinearLayoutManager.HORIZONTAL,
                        false
                    )

                layoutManager.initialPrefetchItemCount = 4

                holder.recycler.layoutManager = layoutManager

                holder.recycler.setRecycledViewPool(viewPool)

                holder.recycler.adapter =
                    PublicacionesCarruselAdapter(lista)

                val scroll = scrollStates.get(position, 0)

                layoutManager.scrollToPosition(scroll)

                holder.recycler.addOnScrollListener(
                    object : RecyclerView.OnScrollListener() {

                        override fun onScrolled(
                            recyclerView: RecyclerView,
                            dx: Int,
                            dy: Int
                        ) {

                            val pos =
                                layoutManager.findFirstVisibleItemPosition()

                            scrollStates.put(position, pos)
                        }
                    }
                )
            }
        }
    }

    class ArticuloViewHolder(view: View) :
        RecyclerView.ViewHolder(view)

    class FiltrosViewHolder(view: View) :
        RecyclerView.ViewHolder(view) {

        val btnTodas: Button = view.findViewById(R.id.btnFiltroTodas)
        val btnPendientes: Button = view.findViewById(R.id.btnFiltroPendientes)
        val btnIdentificadas: Button = view.findViewById(R.id.btnFiltroIdentificadas)
        val btnBuscar: Button = view.findViewById(R.id.btnBuscar)
        val etBuscar: EditText = view.findViewById(R.id.etBuscarNombre)
    }



    class MisPublicacionesViewHolder(view: View) :
        RecyclerView.ViewHolder(view) {

        val recycler: RecyclerView =
            view.findViewById(R.id.recyclerMisPublicaciones)
    }

    class CarruselViewHolder(view: View) :
        RecyclerView.ViewHolder(view) {

        val recycler: RecyclerView =
            view.findViewById(R.id.recyclerCarruselComunidad)
    }
}