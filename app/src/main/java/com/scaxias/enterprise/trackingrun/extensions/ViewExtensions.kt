package com.scaxias.enterprise.trackingrun.extensions

import android.view.View

fun View.visible() = run {
    this.visibility = View.VISIBLE
}

fun View.gone() = run {
    this.visibility = View.GONE
}

fun View.visibleFadeIn() = run {
    this.visibility = View.VISIBLE
    this.animate().alpha(1.0f).duration = 500
}

fun View.goneFadeOut()  {
    this.animate().alpha(0.0f).setDuration(500).withEndAction {
        this.visibility = View.GONE
    }
}
