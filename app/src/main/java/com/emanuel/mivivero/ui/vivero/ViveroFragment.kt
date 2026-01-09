package com.emanuel.mivivero.ui.vivero

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.emanuel.mivivero.R

class ViveroFragment : Fragment(R.layout.fragment_vivero) {

    private val viewModel: ViveroViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerPlantas)

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = PlantaAdapter(viewModel.getPlantas())
    }
}
