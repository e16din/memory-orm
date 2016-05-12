package no.hyper.memoryormdemo

/**
 * Created by Jean on 5/12/2016.
 */
data class Profile (
        val id: String?,
        val name: String,
        val age: Int,
        val active: Boolean,
        val email: String,
        val countryCode: String,
        val address: String,
        val city: String,
        val zipCode: String)