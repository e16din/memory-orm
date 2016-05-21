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

        val axe = Gear("axe", true)
        val shield = Gear("shield", false)
        val hytta = House("hytta", "Scandinavia")
        val ragnar = Profile("ragnar", 30, true, listOf(axe, shield), hytta)
        memory.save(ragnar)

        val sword = Gear("sword", true)
        val treeHouse = House("TreeHouse", "candy Kingdom")
        val finn = Profile("fin", 13, true, listOf(sword), treeHouse)
        memory.save(finn)

        val jake = Profile("jake", 28, false)
        memory.save(jake)

        val result = memory.fetchById(Profile::class.java, 3)
        Log.d(LOG_TAG, result.toString())
    }

}
