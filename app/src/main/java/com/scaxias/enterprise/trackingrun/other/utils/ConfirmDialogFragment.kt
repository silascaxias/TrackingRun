package com.scaxias.enterprise.trackingrun.other.utils

import android.app.Dialog
import android.os.Bundle
import android.view.Window
import androidx.fragment.app.DialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.scaxias.enterprise.trackingrun.R

class ConfirmDialogFragment(
        private val title: String,
        private val message: String?,
        private val positiveText: String,
        private var positiveListener: () -> Unit,
        private var negativeText: String?
): DialogFragment() {

    fun setPositiveListener(positiveListener: () -> Unit) { this.positiveListener = positiveListener }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
       val dialog = MaterialAlertDialogBuilder(requireContext(), R.style.AlertDialogTheme)
            .setTitle(title)
            .setIcon(R.drawable.ic_delete)
            .setPositiveButton(positiveText) { _, _ ->
                positiveListener()
            }
            .setCancelable(false)

       message?.let{ dialog.setMessage(message) }
       negativeText?.let {
           dialog.setNegativeButton(negativeText) { dialogInterface, _ ->
               dialogInterface.cancel()
           }
       }

        return dialog.create()
    }
}