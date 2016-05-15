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

        var result = memory.deleteTable(Profile::class.java)
        logResult("delete table Profile", result)

        result = memory.createTableFrom(Profile::class.java, true)
        logResult("create table Profile", result)

        val profiles = listOf(Profile("Finn", 13, true), Profile("Jake", 28, false))
        val insert = memory.save(profiles)
        Log.d(LOG_TAG, "insert: $insert")

        val list = memory.fetchAll(Profile::class.java)
        Log.d(LOG_TAG, list.toString())

        var profile = memory.fetchFirst(Profile::class.java)
        Log.d(LOG_TAG, "fetch first: ${profile.toString()}")

        profile = memory.fetchById(Profile::class.java, "2")
        Log.d(LOG_TAG, "fetch by id: ${profile.toString()}")
    }

    private fun logResult(action: String, result: Number) {
        when (result) {
            1 -> Log.d(LOG_TAG, "$action: success")
            -1 -> Log.d(LOG_TAG, "$action: fail")
            else -> Log.d(LOG_TAG, "$action: unknown code : $result")
        }
    }
}
