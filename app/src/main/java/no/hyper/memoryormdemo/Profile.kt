package no.hyper.memoryormdemo

/**
 * Created by Jean on 5/12/2016.
 */
data class Profile (
        val name: String,
        val age: Int,
        val human: Boolean,
        val id: String? = null)