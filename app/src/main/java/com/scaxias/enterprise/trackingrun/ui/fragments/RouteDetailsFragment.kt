package com.scaxias.enterprise.trackingrun.ui.fragments

import android.app.Dialog
import android.content.Intent
import android.content.pm.ActivityInfo
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.FileProvider
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.scaxias.enterprise.trackingrun.BuildConfig
import com.scaxias.enterprise.trackingrun.R
import com.scaxias.enterprise.trackingrun.other.Constants.TRACKING_SHARE_FILE_FOLDER
import com.scaxias.enterprise.trackingrun.other.Constants.TRACKING_SHARE_FILE_NAME
import com.scaxias.enterprise.trackingrun.other.utils.ConfirmDialogFragment
import com.scaxias.enterprise.trackingrun.other.utils.TrackingUtils
import com.scaxias.enterprise.trackingrun.ui.viewmodels.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_route_details.*
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

@AndroidEntryPoint
class RouteDetailsFragment : BottomSheetDialogFragment() {

    private val args: RouteDetailsFragmentArgs by navArgs()

    private val viewModel: MainViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View =
        inflater.inflate(R.layout.fragment_route_details, container, false)

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog  = super.onCreateDialog(savedInstanceState).apply {

        setOnShowListener {
            window!!.setLayout(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
            )
            window!!.setBackgroundDrawableResource(android.R.color.transparent)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireActivity().requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_NOSENSOR

        val run = args.currentRun

        Glide.with(this).load(run.image).into(imageViewRun)

        val calendar = Calendar.getInstance().apply {
            timeInMillis = run.timestamp
        }
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        textViewDate.text = dateFormat.format(calendar.time)

        val avgSpeed = "${run.avgSpeedInKMH}${resources.getString(R.string.km_h_text)}"
        textViewAvgSpeed.text = avgSpeed

        val distanceInKm = "${run.distanceInMeters / 1000f}${resources.getString(R.string.km_text)}"
        textViewDistance.text = distanceInKm

        textViewTime.text = TrackingUtils.getFormattedStopWatchTime(run.timeInMillis)

        val caloriesBurned = "${run.caloriesBurned}${resources.getString(R.string.zero_kcal_text)}"
        textViewCalories.text = caloriesBurned

        buttonShare.setOnClickListener { shareRun() }
        buttonDelete.setOnClickListener {
            ConfirmDialogFragment(
                    getString(R.string.delete_run_title),
                    getString(R.string.delete_run_text),
                    getString(R.string.yes_text), {
                        viewModel.deleteRun(run)
                        ConfirmDialogFragment(
                                getString(R.string.delete_run_success_text),
                                null,
                                getString(R.string.ok_text),
                                {findNavController().popBackStack() },
                                null)
                                .show(parentFragmentManager, null)
                    },
                    getString(R.string.no_text)
            ).show(parentFragmentManager, null)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.clear()
    }

    private fun shareRun() {
        val fileName = TRACKING_SHARE_FILE_NAME
        val bitmap = TrackingUtils.loadBitmapFromView(cardViewScreenShot)
        val cachePath = File(requireContext().cacheDir, TRACKING_SHARE_FILE_FOLDER)
        cachePath.mkdirs()
        val fileDir =  cachePath.toString() + File.separator + fileName
        try {
            FileOutputStream(fileDir).use { out ->
                bitmap?.compress(Bitmap.CompressFormat.PNG, 100, out)
                out.close()
            }

            File(fileDir).let { file ->
                if(file.exists()) {
                    val contentUri: Uri = FileProvider.getUriForFile(requireContext(), "${BuildConfig.APPLICATION_ID}.provider", file)
                    val intent = Intent(Intent.ACTION_SEND)
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    intent.type = "image/*"
                    intent.putExtra(Intent.EXTRA_STREAM, contentUri)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    startActivity(intent)
                    findNavController().popBackStack()
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
}