package no.hyper.memoryormdemo.model

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import no.hyper.memoryorm.annotation.MemoryIgnore
import java.util.*

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
        @SerializedName("opening-hours") val openingHours: List<OpeningHour>,
        @SerializedName("departments") val departments: List<String>,
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

    constructor(parcel: Parcel) : this (
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            mutableListOf<OpeningHour>(),
            mutableListOf<String>(),
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readString()) {
        parcel.readTypedList(openingHours, OpeningHour.CREATOR)
        parcel.readList(departments,String::class.java.classLoader)
    }

    fun getFilter() : String {
        return "$name $address $city $phone $url $zipCode $lng $lat"
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeString(name)
        parcel.writeString(address)
        parcel.writeString(city)
        parcel.writeString(phone)
        parcel.writeString(url)
        parcel.writeString(zipCode)
        parcel.writeTypedList(openingHours)
        parcel.writeList(departments)
        parcel.writeString(zipCode)
        parcel.writeString(zipCode)
        parcel.writeString(countryCode)
        parcel.writeString(countryName)
        parcel.writeString(lng)
        parcel.writeString(lat)
    }

    override fun describeContents() = 0
}