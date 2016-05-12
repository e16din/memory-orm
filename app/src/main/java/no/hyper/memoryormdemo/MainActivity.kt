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

        //val p1 = Profile(null, "Jean", 23, true, "jean@hyper.no", "NO", "Maridalsveien 87", "Oslo", "0461")
        val memory = Memory(this)

        var result = memory.deleteTable(Profile::class.java)
        logResult("delete table", result)

        result = memory.createTableFrom(Profile::class.java)
        logResult("create table", result)
    }

    private fun logResult(action: String, result: Int) {
        when (result) {
            Memory.SUCCESS -> Log.d(LOG_TAG, "$action: success")
            Memory.FAIL -> Log.d(LOG_TAG, "$action: fail")
            Memory.TABLE_ALREADY_EXIST -> Log.d(LOG_TAG, "$action: already exist")
            else -> Log.d(LOG_TAG, "$action: unknown code : $result")
        }
    }
}
