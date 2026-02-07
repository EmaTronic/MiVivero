package com.emanuel.mivivero.ui.albumes

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.emanuel.mivivero.R
import com.emanuel.mivivero.databinding.FragmentAlbumDetalleBinding

class AlbumDetalleFragment : Fragment(R.layout.fragment_album_detalle) {

    private var _binding: FragmentAlbumDetalleBinding? = null
    private val binding get() = _binding!!

    private val viewModel: EditarAlbumViewModel by viewModels()

    private var albumId: Long = -1L

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentAlbumDetalleBinding.bind(view)

        albumId = arguments?.getLong("albumId") ?: -1L
        if (albumId == -1L) {
            requireActivity().onBackPressedDispatcher.onBackPressed()
            return
        }

        binding.recyclerPlantasAlbum.layoutManager =
            LinearLayoutManager(requireContext())

        viewModel.obtenerAlbum(albumId)
            .observe(viewLifecycleOwner) { album ->
                binding.txtNombreAlbum.text = album?.nombre
                binding.txtEstadoAlbum.text = album?.estado
            }

        viewModel.obtenerPlantasDelAlbum(albumId)
            .observe(viewLifecycleOwner) { lista ->
                binding.recyclerPlantasAlbum.adapter =
                    PlantasAlbumAdapter(lista) {}
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
