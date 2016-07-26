package no.hyper.memoryormdemo

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import no.hyper.memoryorm.Memory
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader

class MainActivity : AppCompatActivity() {

    val LOG_TAG = this.javaClass.simpleName

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        this.deleteDatabase((this.packageName))
        val memory = Memory(this, getDatabase())

        memory.createTables()

        val finnGear = listOf(Gear("sword", true, "sword1"), Gear("shield", false, "shield1"))
        val finnHouse = House("tree-house", "unknown", "finnTreeHouse")
        val finn = Profile("finn", 13, true, finnGear, finnHouse, "profile0")

        memory.save(finn)

        val check = memory.fetchAll(Profile::class.java)
        Log.e(LOG_TAG, check.toString())
    }

    fun getDatabase() : String {
        val reader = BufferedReader(InputStreamReader(assets.open("schema/database.json")));
        val sb = StringBuilder()

        try {
            do {
                var line = reader.readLine()
                if (line != null) sb.append(line)
            } while (line != null)
        } catch (e: IOException) {
            e.stackTrace
        }

        reader.close();
        return sb.toString()
    }

}
