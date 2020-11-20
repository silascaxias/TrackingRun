package com.scaxias.enterprise.trackingrun.ui.fragments

import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.scaxias.enterprise.trackingrun.R
import com.scaxias.enterprise.trackingrun.extensions.gone
import com.scaxias.enterprise.trackingrun.extensions.visible
import com.scaxias.enterprise.trackingrun.other.Constants.KEY_FIRST_TIME_TOGGLE
import com.scaxias.enterprise.trackingrun.other.Constants.KEY_NAME
import com.scaxias.enterprise.trackingrun.other.Constants.KEY_WEIGHT
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_setup.*
import kotlinx.android.synthetic.main.fragment_setup.buttonContinue
import kotlinx.android.synthetic.main.fragment_setup.editTextName
import kotlinx.android.synthetic.main.fragment_setup.editTextWeight
import kotlinx.android.synthetic.main.fragment_setup.inputLayoutName
import kotlinx.android.synthetic.main.fragment_setup.textInputWeight
import javax.inject.Inject

@AndroidEntryPoint
class SetupFragment: Fragment(R.layout.fragment_setup) {

    @Inject
    lateinit var sharedPref: SharedPreferences

    @set:Inject
    var isFirstAppOpen = true

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        if(!isFirstAppOpen) {
            val navOptions = NavOptions.Builder()
                .setPopUpTo(R.id.setupFragment, true)
                .build()

            findNavController().navigate(
                R.id.action_setupFragment_to_runFragment,
                savedInstanceState,
                navOptions
            )
        }

        buttonContinue.setOnClickListener {
            if(saveData()) findNavController().navigate(R.id.action_setupFragment_to_runFragment)
        }
    }

    private fun saveData(): Boolean {
        val (name,weight) = Pair(editTextName.text.toString(), editTextWeight.text.toString())
        val (isEmptyName, isEmptyWeight) = Pair(name.isEmpty(), weight.isEmpty())

        inputLayoutName.error = if(isEmptyName) getString(R.string.name_require_text) else null
        separatorView1.apply { if(isEmptyName) gone() else visible() }
        textInputWeight.error = if(isEmptyWeight) getString(R.string.weight_require_text) else null
        separatorView2.apply { if(isEmptyWeight) gone() else visible() }

        if(isEmptyName || isEmptyWeight) return false

        sharedPref.edit()
            .putString(KEY_NAME, name)
            .putFloat(KEY_WEIGHT, weight.toFloat())
            .putBoolean(KEY_FIRST_TIME_TOGGLE, false)
            .apply()

        val toolbarText = getString(R.string.lets_go_text, name)
        requireActivity().textViewToolbarTitle.text = toolbarText

        return true
    }
}