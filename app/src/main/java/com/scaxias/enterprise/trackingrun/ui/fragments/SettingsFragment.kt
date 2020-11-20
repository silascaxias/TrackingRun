package com.scaxias.enterprise.trackingrun.ui.fragments

import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar
import com.scaxias.enterprise.trackingrun.R
import com.scaxias.enterprise.trackingrun.extensions.gone
import com.scaxias.enterprise.trackingrun.extensions.visible
import com.scaxias.enterprise.trackingrun.other.Constants.KEY_NAME
import com.scaxias.enterprise.trackingrun.other.Constants.KEY_WEIGHT
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_settings.*
import javax.inject.Inject
import kotlinx.android.synthetic.main.fragment_settings.editTextName
import kotlinx.android.synthetic.main.fragment_settings.editTextWeight
import kotlinx.android.synthetic.main.fragment_settings.inputLayoutName
import kotlinx.android.synthetic.main.fragment_settings.separatorView1
import kotlinx.android.synthetic.main.fragment_settings.separatorView2
import kotlinx.android.synthetic.main.fragment_settings.textInputWeight

@AndroidEntryPoint
class SettingsFragment: Fragment(R.layout.fragment_settings) {

    @Inject
    lateinit var sharedPreferences: SharedPreferences

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        loadFields()
        buttonApply.setOnClickListener {
            if(applyChanges()) Snackbar.make(view, getString(R.string.saved_changes_text), Snackbar.LENGTH_LONG).show()
        }
    }

    private fun loadFields() {
        editTextName.setText(sharedPreferences.getString(KEY_NAME, ""))
        editTextWeight.setText(sharedPreferences.getFloat(KEY_WEIGHT, 80f).toString())
    }

    private fun applyChanges(): Boolean {
        val (name,weight) = Pair(editTextName.text.toString(), editTextWeight.text.toString())
        val (isEmptyName, isEmptyWeight) = Pair(name.isEmpty(), weight.isEmpty())

        inputLayoutName.error = if(isEmptyName) getString(R.string.name_require_text) else null
        separatorView1.apply { if(isEmptyName) gone() else visible() }
        textInputWeight.error = if(isEmptyWeight) getString(R.string.weight_require_text) else null
        separatorView2.apply { if(isEmptyWeight) gone() else visible() }

        if(isEmptyName || isEmptyWeight) return false

        sharedPreferences.edit()
            .putString(KEY_NAME, name)
            .putFloat(KEY_WEIGHT, weight.toFloat())
            .apply()

        val toolbarText = getString(R.string.lets_go_text, name)
        requireActivity().textViewToolbarTitle.text = toolbarText

        return true
    }
}