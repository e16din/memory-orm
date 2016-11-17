package no.hyper.memoryormdemo

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import no.hyper.memoryorm.Memory
import no.hyper.memoryormdemo.model.*
import java.util.*

class MainActivity : AppCompatActivity() {

    val LOG_TAG = this.javaClass.simpleName

    private val memory by lazy { Memory(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        memory.deleteDatabase()
        memory.createDatabase()

        val sword = Gear(UUID.randomUUID().toString(), Gear.Type.SWORD, "grass sword")
        val armor = Gear(UUID.randomUUID().toString(), Gear.Type.ARMOR, "armor of zeldron")
        val gears = mutableListOf(sword, armor)
        val animal = Animal(UUID.randomUUID().toString(), "jake", true)
        val profile = Profile(UUID.randomUUID().toString(), "finn", 13, true, gears, animal)
        memory.save(profile)

        val fetch = memory.fetchFirst(Profile::class.java)
        Log.d(LOG_TAG, fetch.toString())
    }

}
