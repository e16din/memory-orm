package no.hyper.memoryormdemo

import android.app.Application
import com.facebook.stetho.Stetho


/**
 * Created by jean on 10.11.2016.
 */
class MemoryDemo : Application() {

    override fun onCreate() {
        super.onCreate()

        val initializerBuilder = Stetho.newInitializerBuilder(this)
        initializerBuilder.enableWebKitInspector(Stetho.defaultInspectorModulesProvider(this))
        initializerBuilder.enableDumpapp(Stetho.defaultDumperPluginsProvider(this))

        val initializer = initializerBuilder.build()
        Stetho.initialize(initializer)
    }

}