package no.hyper.memoryormdemo

import android.database.Cursor
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

        val sword = Gear("axe", true)
        val shield = Gear("shield", false)
        val profile = Profile("ragnar", 30, true, listOf(sword, shield))
        val idProfile = memory.save(profile)
        Log.d(LOG_TAG, "profile id: $idProfile")

        var list = memory.fetchAll(Profile::class.java)
        list.forEach { Log.d(LOG_TAG, it.toString()) }

        var list2 = memory.fetchAll(Gear::class.java)
        list2.forEach { Log.d(LOG_TAG, it.toString()) }

        //val result = memory.fetchAll(Profile::class.java)
        //Log.d(LOG_TAG, "profiles: $result")*/
    }

}
