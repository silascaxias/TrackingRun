package com.scaxias.enterprise.trackingrun.ui.fragments

import android.Manifest
import android.content.SharedPreferences
import android.content.pm.ActivityInfo
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.MutableLiveData
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.scaxias.enterprise.trackingrun.R
import com.scaxias.enterprise.trackingrun.db.run.entities.Run
import com.scaxias.enterprise.trackingrun.extensions.*
import com.scaxias.enterprise.trackingrun.other.Constants
import com.scaxias.enterprise.trackingrun.other.Constants.REQUEST_CODE_LOCATION_PERMISSION
import com.scaxias.enterprise.trackingrun.other.RunsFilterType
import com.scaxias.enterprise.trackingrun.other.utils.ConfirmDialogFragment
import com.scaxias.enterprise.trackingrun.other.utils.TrackingUtils
import com.scaxias.enterprise.trackingrun.ui.adapters.RunAdapter
import com.scaxias.enterprise.trackingrun.ui.viewmodels.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_app.*
import kotlinx.android.synthetic.main.fragment_route.*
import pub.devrel.easypermissions.AppSettingsDialog
import pub.devrel.easypermissions.EasyPermissions
import javax.inject.Inject


@AndroidEntryPoint
class RouteFragment: Fragment(R.layout.fragment_route), EasyPermissions.PermissionCallbacks, RunAdapter.RunCellClickListener {

    private val viewModel: MainViewModel by viewModels()

    @Inject
    lateinit var sharedPreferences: SharedPreferences

    private lateinit var runAdapter: RunAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireActivity().requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_NOSENSOR

        init()
        requestPermissions()
        setupRecyclerView()
        setupListeners()
        subscribeToObservers()
    }

    private fun init() {
        val name = sharedPreferences.getString(Constants.KEY_NAME, "")
        val toolbarText = if(name?.isNotEmpty() == true) getString(R.string.lets_go_text, sharedPreferences.getString(Constants.KEY_NAME, ""))
        else getString(R.string.app_name)
        requireActivity().textViewToolbarTitle.text = toolbarText

        spinnerPage.gone()
        textViewPage.gone()
    }

    private fun setupListeners() {
        spinnerFilter.setSelection(viewModel.filterType.ordinal)
        spinnerFilter.onItemSelectedListener = object: AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(p0: AdapterView<*>?) {}
            override fun onItemSelected(adapterView: AdapterView<*>?, view: View?, position: Int, id: Long) {
                viewModel.applyFilter(RunsFilterType.values()[position])
                recyclerViewRuns.scrollToPosition(0)
            }
        }
        spinnerPage.onItemSelectedListener = object: AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {}
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                viewModel.selectedPage = position + 1
                viewModel.applyRunsPerPage()
            }
        }
        newRunButton.setOnClickListener { findNavController().navigate(R.id.action_runFragment_to_trackingFragment) }
        deleteRunButton.setOnClickListener { deleteSelectedRuns() }
    }

    private fun deleteSelectedRuns() = ConfirmDialogFragment(
            getString(R.string.delete_all_run_title),
            getString(R.string.delete_all_run_text),
            getString(R.string.yes_text),
            {
                viewModel.idsToDelete.value?.let { viewModel.deleteAllRuns(it.toTypedArray()) }
            },
            getString(R.string.no_text)
    ).show(parentFragmentManager, null)

    private fun subscribeToObservers() {
        viewModel.runs.observe(viewLifecycleOwner, { viewModel.applyRunsPerPage() })
        viewModel.runsPerPage.observe(viewLifecycleOwner, { runsPerPage ->
            textViewEmptyRuns.apply { if (runsPerPage.isEmpty()) visible() else gone() }
            runAdapter.submitList(runsPerPage)
        })
        viewModel.pages.observe(viewLifecycleOwner, {
            if (it.count() > 1) {
                spinnerPage.visible()
                textViewPage.visible()
                spinnerPage.apply { adapter = ArrayAdapter(requireActivity(), R.layout.support_simple_spinner_dropdown_item, it) }
                spinnerPage.setSelection(0)
            } else {
                spinnerPage.gone()
                textViewPage.gone()
            }
        })
        viewModel.idsToDelete.observe(viewLifecycleOwner, {
            it?.let {
                if(it.isEmpty()) deleteRunButton.goneFadeOut()
                else deleteRunButton.visibleFadeIn()
            }
        })
    }

    private fun setupRecyclerView() = recyclerViewRuns.apply {
        runAdapter = RunAdapter(this@RouteFragment)
        adapter = runAdapter
        layoutManager = LinearLayoutManager(requireContext())
    }

    private fun requestPermissions() {
        if(TrackingUtils.hasLocationPermissions(requireContext())) {
            return
        }
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            EasyPermissions.requestPermissions(
                    this,
                    getString(R.string.accept_permissions_text),
                    REQUEST_CODE_LOCATION_PERMISSION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION
            )
        } else {
            EasyPermissions.requestPermissions(
                    this,
                    getString(R.string.accept_permissions_text),
                    REQUEST_CODE_LOCATION_PERMISSION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_BACKGROUND_LOCATION
            )
        }
    }

    override fun onRunCellClickListener(currentRun: Run) {
        val action = RouteFragmentDirections.actionRunFragmentToRunDetailsFragment(currentRun)
        Navigation.findNavController(requireView()).navigate(action)
    }

    override fun onCheckedListener(id: Int, isChecked: Boolean) {
        viewModel.idsToDelete.postValue(viewModel.idsToDelete.value?.apply {
            if(this.contains(id).not()) add(id) else remove(id)
        })
    }

    override fun onPermissionsGranted(requestCode: Int, perms: MutableList<String>) = Unit

    override fun onPermissionsDenied(requestCode: Int, perms: MutableList<String>) =
        if(EasyPermissions.somePermissionPermanentlyDenied(this, perms))
            AppSettingsDialog.Builder(this).build().show()
        else requestPermissions()

    override fun onRequestPermissionsResult(
            requestCode: Int,
            permissions: Array<out String>,
            grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }
}