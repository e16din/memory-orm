package no.hyper.memoryormdemo

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import no.hyper.memoryorm.Memory
import no.hyper.memoryormdemo.model.*
import java.util.*

class MainActivity : AppCompatActivity() {

    val LOG_TAG = "MainActivity_LOG"

    private val memory by lazy { Memory(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        memory.deleteDatabase()
        memory.createDatabase()

        val animal = Animal(UUID.randomUUID().toString(), "jake", true)
        val profile = Profile(UUID.randomUUID().toString(), "finn", 13, true, null, null)
        memory.saveOrUpdate(profile)

        val fetch = memory.fetchFirst(Profile::class.java)
        Log.d(LOG_TAG, "fetch: $fetch")

        var number = memory.getTableCount(Profile::class.java.simpleName)
        Log.d(LOG_TAG, "number : $number")
        
        memory.deleteById(Profile::class.java.simpleName, profile.id)

        number = memory.getTableCount(Profile::class.java.simpleName)
        Log.d(LOG_TAG, "number : $number")
    }

}
