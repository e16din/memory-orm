package no.hyper.memoryormdemo.model

/**
 * Created by Jean on 5/12/2016.
 */
data class Profile (
        var id : String?,
        var name : String,
        var age : Int,
        var human : Boolean,
        var gear : MutableList<Gear>,
        var animal : Animal
) {

    companion object {
        val SOME_COMPANION_PROFILE_VAL = 1
    }

}