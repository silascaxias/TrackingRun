package com.scaxias.enterprise.trackingrun.db.run.entities

import android.graphics.Bitmap
import android.os.Parcel
import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "run")
data class Run (
    var image: Bitmap? = null,
    var timestamp: Long = 0L,
    var avgSpeedInKMH: Float = 0f,
    var distanceInMeters: Int = 0,
    var timeInMillis: Long = 0L,
    var caloriesBurned: Int = 0
): Parcelable {
    @PrimaryKey(autoGenerate = true)
    var id: Int? = null

    constructor(parcel: Parcel) : this(
            parcel.readParcelable(Bitmap::class.java.classLoader),
            parcel.readLong(),
            parcel.readFloat(),
            parcel.readInt(),
            parcel.readLong(),
            parcel.readInt()) {
        id = parcel.readValue(Int::class.java.classLoader) as? Int
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel?, flags: Int) {
        dest?.writeLong(timestamp)
        dest?.writeFloat(avgSpeedInKMH)
        dest?.writeInt(distanceInMeters)
        dest?.writeLong(timeInMillis)
        dest?.writeInt(caloriesBurned)
        dest?.writeParcelable(image, flags)
    }

    companion object CREATOR : Parcelable.Creator<Run> {
        override fun createFromParcel(parcel: Parcel): Run {
            return Run(parcel)
        }

        override fun newArray(size: Int): Array<Run?> {
            return arrayOfNulls(size)
        }
    }
}