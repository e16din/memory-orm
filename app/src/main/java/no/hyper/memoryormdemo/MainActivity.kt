package no.hyper.memoryormdemo

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import no.hyper.memoryorm.Memory
import no.hyper.memoryormdemo.model.Profile

class MainActivity : AppCompatActivity() {

    val LOG_TAG = this.javaClass.simpleName

    private val memory by lazy { Memory(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        memory.deleteDatabase()
        memory.createDatabase()

        //val profile = Profile("afvhabsflakvabørv", "jake", 13, true)
        //memory.save(profile)

        //val fetch = memory.fetchFirst(Profile::class.java)
        //Log.d(LOG_TAG, fetch.toString())
    }

}
