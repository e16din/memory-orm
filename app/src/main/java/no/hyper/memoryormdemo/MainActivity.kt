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

        val sword = Gear("axe", true)
        val shield = Gear("shield", false)
        val profile = Profile("ragnar", 30, true, listOf(sword, shield))
        val idProfile = memory.save(profile)


    }

}
