package no.hyper.memoryormdemo.model

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import no.hyper.memoryorm.annotation.MemoryIgnore
import no.hyper.memoryormdemo.customReadList
import no.hyper.memoryormdemo.customReadParcelableArray

/**
 * Created by Jean on 8/4/2016.
 */
data class Store (
        var id: String,
        @SerializedName("name") val name: String,
        @SerializedName("address") val address: String,
        @SerializedName("city") val city: String,
        @SerializedName("phone") val phone: String,
        @SerializedName("url") val url: String,
        @SerializedName("zip-code") val zipCode: String,
        @SerializedName("departments") val departments: List<String>,
        @SerializedName("opening-hour") val openingHours: List<OpeningHour>,
        @SerializedName("country-code") val countryCode: String,
        @SerializedName("country-name") val countryName: String,
        @SerializedName("lng") val lng: String,
        @SerializedName("lat") val lat: String
) : Parcelable {

    companion object {
        @MemoryIgnore
        @JvmField
        val CREATOR: Parcelable.Creator<Store> = object : Parcelable.Creator<Store> {
            override fun createFromParcel(parcel: Parcel) = Store(parcel)

            override fun newArray(size: Int): Array<Store?> {
                return arrayOfNulls(size)
            }
        }
    }

    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.customReadList<String>(String::class.java.classLoader),
        parcel.customReadParcelableArray(OpeningHour::class.java.classLoader),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString())

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeString(name)
        parcel.writeString(address)
        parcel.writeString(city)
        parcel.writeString(phone)
        parcel.writeString(url)
        parcel.writeString(zipCode)
        parcel.writeList(departments)

        val array : Array<OpeningHour> = openingHours.toTypedArray()
        parcel.writeParcelableArray(array, 0)
        parcel.writeString(countryCode)
        parcel.writeString(countryName)
        parcel.writeString(lng)
        parcel.writeString(lat)
    }

    override fun describeContents() = 0
}