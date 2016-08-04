package no.hyper.memoryormdemo

import com.google.gson.annotations.SerializedName

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
) {
    fun getFilter() : String {
        return "$name $address $city $phone $url $zipCode $lng $lat"
    }
}