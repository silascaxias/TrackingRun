package com.scaxias.enterprise.trackingrun.ui.fragments

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.scaxias.enterprise.trackingrun.R
import kotlinx.android.synthetic.main.fragment_setup.*

class SetupFragment: Fragment(R.layout.fragment_setup) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        buttonContinue.setOnClickListener {
            findNavController().navigate(R.id.action_setupFragment_to_runFragment)
        }
    }
}