package no.hyper.memoryormdemo

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import no.hyper.memoryorm.Memory
import no.hyper.memoryormdemo.model.OpeningHour
import no.hyper.memoryormdemo.model.Store

class MainActivity : AppCompatActivity() {

    val LOG_TAG = this.javaClass.simpleName

    private val memory by lazy { Memory(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        this.deleteDatabase((this.packageName))

        memory.createTables()

        val openingHours = listOf(OpeningHour("monday-saturday", "9-17"), OpeningHour("sunday", "closed"))
        val departments = listOf("dep1", "dep2")
        var store = Store("id1", "store1", "address1", "city1", "phone1", "url1", "zip1", departments, openingHours,
                "code1", "name1", "lgn1", "lat1")

        memory.save(store)
        val fetch = memory.fetchFirst(Store::class.java)
        Log.d(LOG_TAG, fetch.toString())
    }

}
