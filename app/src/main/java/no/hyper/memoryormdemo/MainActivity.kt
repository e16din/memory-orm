package no.hyper.memoryormdemo

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Parcelable
import android.util.Log
import no.hyper.memoryorm.Memory
import no.hyper.memoryorm.broadcastReceiver.FetchListener
import no.hyper.memoryorm.broadcastReceiver.WriteListener
import no.hyper.memoryormdemo.model.OpeningHour
import no.hyper.memoryormdemo.model.Store
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader

class MainActivity : AppCompatActivity(), FetchListener, WriteListener {

    val LOG_TAG = this.javaClass.simpleName

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        this.deleteDatabase((this.packageName))
        val memory = Memory(this, getDatabase())

        memory.createTables()

        val openingHours = listOf(OpeningHour("monday-saturday", "9-17"), OpeningHour("sunday", "closed"))
        val departments = listOf("dep1", "dep2")
        var store = Store("id1", "store1", "address1", "city1", "phone1", "url1", "zip1", openingHours, departments,
                "code1", "countryname1", "lng1", "lat1")

        memory.saveInBackground(this, store)
        memory.fetchAllInBackground(this, Store::class.java.name, null)
    }

    override fun onFetched(className: String, isList: Boolean, data: Any) {
        Log.d(LOG_TAG, className)
        Log.d(LOG_TAG, isList.toString())
        if (isList) {
            Log.d(LOG_TAG, (data as Array<Parcelable>).toString())
        } else {
            Log.d(LOG_TAG, (data as Parcelable).toString())
        }
    }

    override fun onWrote(className: String?, isList: Boolean, data: Any?) {
        Log.d(LOG_TAG, className)
        Log.d(LOG_TAG, isList.toString())
        if (isList) {
            Log.d(LOG_TAG, (data as Array<Parcelable>).toString())
        } else {
            Log.d(LOG_TAG, (data as Parcelable).toString())
        }
    }

    private fun getDatabase() : String {
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
