# memory-orm

This library is an ORM for Android developed in Java with Kotlin usage in mind.
With Memory you will be able to use your classic POJO Java classes but also the
`data class` from Kotlin.

# Gradle

```
compile 'no.hyper.memoryorm:memoryorm:0.6.7'
```

# Usage

The following examples are written in Kotlin.

## Model

The library relies on a json description of your database to build it. You need to follow this example to create yours : 

``` json
{
  "tables": [{
    "name": "Profile",
    "columns": [{
      "label": "id",
      "type": "text",
      "primary": true
    }, {
      "label": "name",
      "type": "text"
    }, {
      "label": "age",
      "type": "integer"
    }, {
      "label": "human",
      "type": "integer"
    }, {
      "label": "gear",
      "type": "Gear",
      "custom": true,
      "list": true
    }]
  }, {
    "name": "Gear",
    "columns": [{
      "label": "id",
      "type": "text",
      "primary": true
    }, {
      "label": "type",
      "type": "text",
      "enum": true
    }, {
      "label": "name",
      "type": "text"
    }, {
      "label": "id_Profile",
      "type": "integer",
      "foreign_key": true
    }]
  }, {
    "name": "Animal",
    "columns": [{
      "label": "id",
      "type": "text",
      "primary": true
    }, {
      "label": "name",
      "type": "text"
    }, {
      "label": "magic",
      "type": "integer"
    }, {
      "label": "id_Profile",
      "type": "integer",
      "foreign_key": true
    }]
  }]
}
```

The file HAS TO be placed like this `asset/schema/database.json`. Of course, you also need to have the corresponding POJO or `data class` object that go with each table described in your json. The "type" parameter accept two options : "text" or "integer" based on the type supported by SQLite. So if you have a boolean parameter, the library will use 0 or 1 in the database and convert to false or true accordingly. 

``` kotlin
data class Animal(val id: String, val name: String, val magic: Boolean)

data class Gear(val id: String, val type: Type, val name: String) {

    companion object {
        val SOME_COMPANION_GEAR_VAL = 1
    }

    enum class Type {
        SWORD{
            override fun isGolden() = false
        },

        ARMOR{
            override fun isGolden() = true
        };

        abstract fun isGolden() : Boolean
    }

}

data class Profile (
        var id : String?,
        var name : String,
        var age : Int,
        var human : Boolean,
        var gear : MutableList<Gear>,
        var animal : Animal
) {

    companion object {
        val SOME_COMPANION_PROFILE_VAL = 1
    }

}
```
Note 1 : If your tables have relations, you need to specify the foreigns key in the correct table and follow this convention for its name `id_NameOfTheTable`.

Note 2 : An `id` field is not mandatory, Memory uses the `ROWID` field of each row to maintain the relations through the database.

Note 3 : The `Serialisable` interface is supported, you class/data class can implement it.

## Creating and deleting tables

The first thing you need to do is instantiating the only object you need to use the library by passing the context and our database description as a String:

``` kotlin
val memory = Memory(context)
memory.deleteDatabase() // not necessary
memory.createDatabase()
```

## Save in database

Memory makes it really easy, it also supports nested oject and list : 

``` kotlin
val sword = Gear(UUID.randomUUID().toString(), Gear.Type.SWORD, "grass sword")
val armor = Gear(UUID.randomUUID().toString(), Gear.Type.ARMOR, "armor of zeldron")
val gears = mutableListOf(sword, armor)
val animal = Animal(UUID.randomUUID().toString(), "jake", true)
val profile = Profile(UUID.randomUUID().toString(), "finn", 13, true, gears, animal)
memory.save(profile)
```

The function `save` returns you the rowId value of the row created by the insertion. You can also pass a list parameter, the function will return you the list of corresponding row id.

## Fetch rows from a table

It's as simple as saving:

``` kotlin
val profiles = memory.fetchAll(Profile::class.java)
profiles.forEach() {
    //do some stuff...
}
```

Three other methods are available : `fetchFirst`, `fetchById` and `fetchByRowId`

## Update a row

```kotlin
val profile = memory.fetchFirst(Profile::class.java)
profile.name = "new name"

memory.update(name)
```

Finally, in the case you don't know if an item does not exists in your DB, you can use the `saveOrUpdate` method that check if an item with the same id exists. (Your table need to declare explicitely an id field for this to work)

# Proguard
The library needs to be able to read your model class names in order to save and fetch values in your database. I advice you to add this to your proguard file : 

```
# Application classes that will be serialized/deserialized over Gson
-keep class no.hyper.memoryormdemo.model.** { *; }
```
