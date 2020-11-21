package com.scaxias.enterprise.trackingrun.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.scaxias.enterprise.trackingrun.R
import com.scaxias.enterprise.trackingrun.db.run.entities.Run
import com.scaxias.enterprise.trackingrun.other.utils.TrackingUtils
import kotlinx.android.synthetic.main.item_route.view.*
import java.text.SimpleDateFormat
import java.util.*

class RunAdapter(var runCellClickListener: RunCellClickListener): RecyclerView.Adapter<RunAdapter.RunViewHolder>() {

    inner class RunViewHolder(itemView: View): RecyclerView.ViewHolder(itemView)

    private val diffCallback = object: DiffUtil.ItemCallback<Run>() {
        override fun areItemsTheSame(oldItem: Run, newItem: Run): Boolean
                = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Run, newItem: Run): Boolean
                = oldItem.hashCode() == newItem.hashCode()
    }

    private val differ = AsyncListDiffer(this, diffCallback)

    fun submitList(list: List<Run>) = differ.submitList(list)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RunViewHolder {
        return RunViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.item_route,
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: RunViewHolder, position: Int) {
        val run = differ.currentList[position]
        holder.itemView.apply {
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

            setOnClickListener { runCellClickListener.onRunCellClickListener(run) }
        }
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    interface RunCellClickListener {
        fun onRunCellClickListener(currentRun: Run)
    }
}
