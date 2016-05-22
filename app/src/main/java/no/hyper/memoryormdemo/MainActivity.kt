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
        data class Feed(val item: Any, val position: Int, val id: Int? = null)
        memory.deleteTable(Feed::class.java)
        memory.createTableFrom(Feed::class.java)

        val feeds = mutableListOf<Feed>()
        feeds.add(Feed(Profile("jean", 23, true), 1))
        feeds.add(Feed(Profile("jean2", 24, true), 2))
        feeds.add(Feed(Profile("jean3", 25, true), 3))
        memory.save(feeds);

        val result = memory.fetchAll(Feed::class.java)
        Log.d(LOG_TAG, result.toString())
    }

}
