package no.hyper.memoryormdemo.model

/**
 * Created by jean on 11.11.2016.
 */
data class Gear(val type: Type, val name: String) {

    enum class Type { SWORD, ARMOR }

}