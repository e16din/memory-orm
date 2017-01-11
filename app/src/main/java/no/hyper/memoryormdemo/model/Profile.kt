package no.hyper.memoryormdemo.model

/**
 * Created by Jean on 5/12/2016.
 */
data class Profile (
        var id : String? = null,
        var name : String? = null,
        var age : Int? = null,
        var human : Boolean? = null,
        var gear : MutableList<Gear>? = null,
        var animal : Animal? = null
) {

    companion object {
        val SOME_COMPANION_PROFILE_VAL = 1
    }

}