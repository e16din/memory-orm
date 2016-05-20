package no.hyper.memoryormdemo

/**
 * Created by Jean on 5/12/2016.
 */
data class Profile (
        var name : String,
        var age : Int,
        var human : Boolean,
        var gear : List<Gear>? = null,
        var house : House? = null,
        var id : String? = null)