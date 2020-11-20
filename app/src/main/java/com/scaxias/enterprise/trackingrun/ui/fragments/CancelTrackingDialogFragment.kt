package com.scaxias.enterprise.trackingrun.ui.fragments

import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.scaxias.enterprise.trackingrun.R

class CancelTrackingDialogFragment: DialogFragment() {

    private var positiveListener: (() -> Unit)? = null

    fun setPositiveListener(listener: () -> Unit) {
        positiveListener = listener
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
       return MaterialAlertDialogBuilder(requireContext(), R.style.AlertDialogTheme)
            .setTitle(R.string.cancel_run_text)
            .setMessage(getString(R.string.cancel_run_text_confirm))
            .setIcon(R.drawable.ic_delete)
            .setPositiveButton(getString(R.string.yes_text)) { _, _ ->
                positiveListener?.let { it() }
            }
            .setNegativeButton(getString(R.string.no_text)) { dialogInterface, _ ->
                dialogInterface.cancel()
            }
            .create()

    }
}