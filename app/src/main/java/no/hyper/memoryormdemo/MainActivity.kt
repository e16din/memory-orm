package no.hyper.memoryormdemo

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import no.hyper.memoryorm.Memory

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val p1 = Profile(null, "Jean", 23, true, "jean@hyper.no", "NO", "Maridalsveien 87", "Oslo", "0461")

        val memory = Memory(this)
        memory.createTableFrom(Profile::class.java)
    }
}
