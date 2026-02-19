package com.emanuel.mivivero.ui.utils

import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.ImageView
import androidx.transition.AutoTransition
import androidx.transition.TransitionManager

object AccordionHelper {

    fun setupSingleAccordion(
        parent: ViewGroup,
        sections: List<Section>,
        initiallyOpenIndex: Int = 0
    ) {

        sections.forEachIndexed { index, section ->

            val isOpen = index == initiallyOpenIndex
            section.content.visibility = if (isOpen) View.VISIBLE else View.GONE
            section.arrow.rotation = if (isOpen) 180f else 0f

            section.header.setOnClickListener {

                if (section.content.visibility == View.VISIBLE) return@setOnClickListener

                val transition = AutoTransition().apply {
                    duration = 250
                    interpolator = AccelerateDecelerateInterpolator()
                }

                TransitionManager.beginDelayedTransition(parent, transition)

                sections.forEach {
                    it.content.visibility = View.GONE
                    it.arrow.rotation = 0f
                }

                section.content.visibility = View.VISIBLE
                section.arrow.rotation = 180f
            }
        }
    }

    data class Section(
        val header: View,
        val content: View,
        val arrow: ImageView
    )
}
