package no.hyper.memoryormdemo.model

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import no.hyper.memoryorm.annotation.MemoryIgnore

/**
 * Created by Jean on 8/4/2016.
 */
data class OpeningHour(
        @SerializedName("name") val name : String,
        @SerializedName("hours") val hours : String
) : Parcelable {

    companion object {
        @MemoryIgnore
        @JvmField
        val CREATOR: Parcelable.Creator<OpeningHour> = object : Parcelable.Creator<OpeningHour> {
            override fun createFromParcel(parcel: Parcel) = OpeningHour(parcel)

            override fun newArray(size: Int): Array<OpeningHour?> {
                return arrayOfNulls(size)
            }
        }
    }

    constructor(parcel: Parcel) : this (
            parcel.readString(),
            parcel.readString())

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(name)
        parcel.writeString(hours)
    }

    override fun describeContents() = 0

}