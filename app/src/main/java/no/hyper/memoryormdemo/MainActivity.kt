package no.hyper.memoryormdemo

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import no.hyper.memoryorm.Memory

class MainActivity : AppCompatActivity() {

    val LOG_TAG = this.javaClass.simpleName

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val memory = Memory(this)
        memory.deleteTable(Profile::class.java)
        memory.createTableFrom(Profile::class.java, true)

        val sword = Gear("finn sword", true)
        val profiles = listOf(Profile("Finn", 13, true, sword), Profile("Jake", 28, false))
        val insert = memory.save(profiles)
        Log.d(LOG_TAG, "insert: $insert")

        val list = memory.fetchAll(Profile::class.java)
        Log.d(LOG_TAG, list.toString())

        var profile = memory.fetchFirst(Profile::class.java)
        Log.d(LOG_TAG, "fetch first: ${profile.toString()}")

        profile = memory.fetchById(Profile::class.java, "2")
        Log.d(LOG_TAG, "fetch by id: ${profile.toString()}")

        profile.gear = Gear("super power", true);
        val result = memory.update(profile);
        Log.d(LOG_TAG, "update: $result - ${profile.toString()}")
    }

}
