package no.hyper.memoryormdemo.model

/**
 * Created by jean on 11.11.2016.
 */
data class Gear(val id: String, val type: Type, val name: String) {

    companion object {
        val SOME_COMPANION_GEAR_VAL = 1
    }

    enum class Type {
        SWORD{
            override fun isGolden() = false
        },

        ARMOR{
            override fun isGolden() = true
        };

        abstract fun isGolden() : Boolean
    }

}