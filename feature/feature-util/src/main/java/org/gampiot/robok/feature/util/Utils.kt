package org.gampiot.robok.feature.util

import android.view.View

import androidx.core.graphics.Insets
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.children

import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.appbar.CollapsingToolbarLayout

fun View.enableEdgeToEdgePaddingListener(
    ime: Boolean = false, 
    top: Boolean = false,
    extra: ((Insets) -> Unit)? = null
) {
    if (fitsSystemWindows) throw IllegalArgumentException("fitsSystemWindows must be disabled")
    
    if (this is AppBarLayout) {
        if (ime) throw IllegalArgumentException("AppBarLayout cannot have ime flag enabled")

        val collapsingToolbarLayout = children.find { it is CollapsingToolbarLayout } as? CollapsingToolbarLayout

        collapsingToolbarLayout?.let {
            ViewCompat.setOnApplyWindowInsetsListener(it) { _, insets ->
                insets
            }
        }

        val expandedTitleMarginStart = collapsingToolbarLayout?.expandedTitleMarginStart
        val expandedTitleMarginEnd = collapsingToolbarLayout?.expandedTitleMarginEnd

        ViewCompat.setOnApplyWindowInsetsListener(this) { view, insets ->
            val cutoutAndBars = insets.getInsets(
                WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout()
            )

            (view as AppBarLayout).children.forEach { child ->
                if (child is CollapsingToolbarLayout) {
                    val leftInset = if (child.layoutDirection == View.LAYOUT_DIRECTION_LTR) cutoutAndBars.left else cutoutAndBars.right
                    val rightInset = if (child.layoutDirection == View.LAYOUT_DIRECTION_RTL) cutoutAndBars.left else cutoutAndBars.right

                    if (expandedTitleMarginStart != null && expandedTitleMarginStart != (child.expandedTitleMarginStart + leftInset)) {
                        child.expandedTitleMarginStart = expandedTitleMarginStart + leftInset
                    }
                    if (expandedTitleMarginEnd != null && expandedTitleMarginEnd != (child.expandedTitleMarginEnd + rightInset)) {
                        child.expandedTitleMarginEnd = expandedTitleMarginEnd + rightInset
                    }
                }
                child.setPadding(cutoutAndBars.left, 0, cutoutAndBars.right, 0)
            }
            view.setPadding(0, cutoutAndBars.top, 0, 0)

            val insetsIgnoringVisibility = insets.getInsetsIgnoringVisibility(
                WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout()
            )

            extra?.invoke(cutoutAndBars)
            WindowInsetsCompat.Builder(insets)
                .setInsets(WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout(), 
                    Insets.of(cutoutAndBars.left, 0, cutoutAndBars.right, cutoutAndBars.bottom))
                .setInsetsIgnoringVisibility(WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout(), 
                    Insets.of(insetsIgnoringVisibility.left, 0, insetsIgnoringVisibility.right, insetsIgnoringVisibility.bottom))
                .build()
        }
    } else {
        val initialPaddingLeft = paddingLeft
        val initialPaddingTop = paddingTop
        val initialPaddingRight = paddingRight
        val initialPaddingBottom = paddingBottom

        ViewCompat.setOnApplyWindowInsetsListener(this) { view, insets ->
            val mask = WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout() or 
                if (ime) WindowInsetsCompat.Type.ime() else 0

            val insetsValue = insets.getInsets(mask)
            view.setPadding(
                initialPaddingLeft + insetsValue.left, 
                initialPaddingTop + (if (top) insetsValue.top else 0), 
                initialPaddingRight + insetsValue.right, 
                initialPaddingBottom + insetsValue.bottom
            )

            extra?.invoke(insetsValue)
            WindowInsetsCompat.Builder(insets)
                .setInsets(mask, Insets.NONE)
                .setInsetsIgnoringVisibility(mask, Insets.NONE)
                .build()
        }
    }
}
