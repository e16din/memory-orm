# MemoryORM

This library is an ORM for android developed in Java with Kotlin usage in mind.
With Memory you will be able to use your classic POJO Java classes but also the
`data class` from Kotlin.

# Gradle

```
compile 'no.hyper.memoryorm:memoryorm:0.1'
```

# Usage

The following examples are written in Kotlin.

## Model

As said before, Memory was made in the first place to use Kotlin model class : 

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

## Creating and deleting tables

Tables are created based on the reflection of a variable type. Here, the
`Profile` class has a custom parameter, Memory will create a new table if one
does not exist. Calling the method `createTableFrom` with `true` as the second
parameter will make the `id` field in your database into an `AUTOINCREMENT`
field.

```kotlin
val memory = Memory(this)

memory.deleteTable(Profile::class.java)
memory.createTableFrom(Profile::class.java, true)
```

## Save in database

You don't actually need to create yourself the table before saving an object.
Memory checks for you if the corresponding table exists or creates it otherwise.

```kotlin
val sword = Gear("Finn's sword", true)
val profiles = listOf(
    Profile("Finn", 13, true, sword),
    Profile("Jake", 28, false)
)

val inserted = memory.save(profiles) // will create the table if it doesn't exist already
```

`save` will return the number of rows inserted `-1` if the insertion failed.

## Fetch rows from a table

```kotlin
val profiles = memory.fetchAll(Profile::class.java)
```

It returns a generic list of all the rows in the table, in this example a
`List<Profile>`.

## Fetch the row value of a table

```kotlin
val profile = memory.fetchFirst(Profile::class.java)
```

It returns the first row of the table. The object returned is the same type than
the one you passed as parameter, in this example a `Profile`.

## Fetch a row from the table by id

```kotlin
memory.fetchById(Profile::class.java, "2")
```

It returns the row of the table with the same id you passed in the second
parameter. The object returned is the same type than the one you passed as first
parameter.

## Update a row

```kotlin
profile.gear = Gear("super power", true)
memory.update(profile)
```

'update' returns the row number in the table or `-1` if the update failed.

## Save or update a row

If you don't know whether or not a value exists in your database, you can use
the method `saveOrUpdate`, which will check if the object has an id and if it
exist inside your database. If it exists, it'll update the existing record;
otherwise it will create a new one.

```kotlin
val profile = Profile("Marceline", 1003, false, Gear("guitar", false))
val savedOrUpdated = memory.saveOrUpdate(profile)

Log.d(LOG_TAG, "saveOrUpdate: $savedOrUpdated - ${profile.toString()}")
```

It returns the row number in the table or `-1` if the insert/update failed.
