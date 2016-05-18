# MemoryORMDemo

This library is an ORM for android developed in Java with kotlin usage in mind. With Memory you will be able to use your classic POJO Java classes but also the `data class` from Kotlin.

# Gradle
```
compile 'no.hyper.memoryorm:memoryorm:0.1'
```

# Usage

The following exemples are code in Kotlin 

## Model
As said before, Memory was made in the first place to use kotlin model class : 
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
```
But you could also use classic POJO objects.

## Create and delete tables
Tables are created based on the reflection of a variable type. Here, the `Profile` class has a custom parameter, Memory will create a seperate table if it does not exist.
Calling the method `createTableFrom` with the second parameter equals to `true`, the `id` field in your db will be an `AUTOINCREMENT` field.
```
val memory = Memory(this)
memory.deleteTable(Profile::class.java)
memory.createTableFrom(Profile::class.java, true)
```
## Save in database
You don't need to create yourself the table before saving an object. Memory checks for you if the corresponding table exists or creates it otherwise.
```
val sword = Gear("finn sword", true)
val profiles = listOf(Profile("Finn", 13, true, sword), Profile("Jake", 28, false))
val insert = memory.save(profiles)  // "save" method checks if the table need to be create before inserting
```
## Fetch rows from a table
```
memory.fetchAll(Profile::class.java)
```

## Fetch the row value of a table
```
memory.fetchFirst(Profile::class.java)
```

## Fetch a row from the table by id
```
memory.fetchById(Profile::class.java, "2")
```

## Update a row
```
profile.gear = Gear("super power", true)
val update = memory.update(profile)
```

## Save or update a row
if you don't know wether a value exists or not in your database, you can use the method `saveOrUpdate`, it will check if the object has an id and if it exist inside your database. Yes => update, No => insert.
```
profile = Profile("Marceline", 1003, false, Gear("guitar", false))
val saveOrUpdate = memory.saveOrUpdate(profile)
Log.d(LOG_TAG, "saveOrUpdate: $saveOrUpdate - ${profile.toString()}")
```
