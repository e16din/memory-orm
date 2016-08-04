package no.hyper.memoryormdemo

import com.google.gson.annotations.SerializedName

/**
 * Created by Jean on 8/4/2016.
 */
data class OpeningHour(
        @SerializedName("name") val name : String,
        @SerializedName("hours") val hours : String
)