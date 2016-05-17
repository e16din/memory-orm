# MemoryORMDemo

This library is an ORM for android developed in Java with kotlin usage in mind. With Memory you will be able to use your classic POJO Java classes but also the `data class` from Kotlin.

# Usage
``` kotlin
data class Profile (
    var name : String,
    var age : Int,
    var human : Boolean,
    var gear : Gear? = null,
    var id : String? = null
)
        
data class Gear (
    val name : String,
    val magical : Boolean,
    val id : String? = null
)
    
val memory = Memory(this)
memory.deleteTable(Profile::class.java)             //
memory.createTableFrom(Profile::class.java, true)   // this two calls are not mandatory

val sword = Gear("finn sword", true)
val profiles = listOf(Profile("Finn", 13, true, sword), Profile("Jake", 28, false))
val insert = memory.save(profiles)  // "save" method checks if the table need to be create before inserting
Log.d(LOG_TAG, "insert: $insert")

var profile = memory.fetchFirst(Profile::class.java)
Log.d(LOG_TAG, "fetch first: ${profile.toString()}")

profile = memory.fetchById(Profile::class.java, "2")
Log.d(LOG_TAG, "fetch by id: ${profile.toString()}")

profile.gear = Gear("super power", true);
val update = memory.update(profile);
Log.d(LOG_TAG, "update: $update - ${profile.toString()}")

profile = Profile("Marceline", 1003, false, Gear("guitar", false))
val saveOrUpdate = memory.saveOrUpdate(profile)
Log.d(LOG_TAG, "saveOrUpdate: $saveOrUpdate - ${profile.toString()}")

val list = memory.fetchAll(Profile::class.java)
Log.d(LOG_TAG, list.toString())
```

Tables are created based on the reflection of a variable type. Here, the `Profile` class has a custom parameter, Memory will create a seperate table if it not exists.
Calling the method `createTableFrom` with the second parameter equals to `true`, the `id` field in your db will be an `AUTOINCREMENT` field.
