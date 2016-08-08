# MemoryORM

This library is an ORM for Android developed in Java with Kotlin usage in mind.
With Memory you will be able to use your classic POJO Java classes but also the
`data class` from Kotlin.

# Gradle

```
compile 'no.hyper.memoryorm:memoryorm:0.2.7'
```

# Usage

The following examples are written in Kotlin.

## Model

The library relies on a json description of your database to build it. You need to follow this example to create yours : 

``` json
{
  "tables": [{
    "name": "Gear",
    "columns": [{
      "label": "id",
      "type": "text",
      "primary": true
    }, {
      "label": "name",
      "type": "text"
    }, {
      "label": "magical",
      "type": "integer"
    }, {
      "label": "id_Profile",
      "type": "integer",
      "foreign_key": true
    }]
  }, {
    "name": "House",
    "columns": [{
      "label": "id",
      "type": "text",
      "primary": true
    }, {
      "label": "name",
      "type": "text"
    }, {
      "label": "place",
      "type": "text"
    }, {
      "label": "id_Profile",
      "type": "integer",
      "foreign_key": true
    }]
  }, {
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
      "list": true
    }, {
      "label": "house",
      "type": "House"
    }]
  }]
}
```

The file HAS TO be placed like this `asset/schema/database.json`. Of course, you also need to have the corresponding POJO or `data class` object that go with each table described in your json.

``` kotlin
data class Gear (
    var name : String,
    var magical : Boolean,
    val id : String? = null)

data class House (
    var name : String,
    var place : String,
    val id : String? = null)

data class Profile (
        var name : String,
        var age : Int,
        var human : Boolean,
        var gear : List<Gear>? = null,
        var house : House? = null,
        var id : String? = null)
```
Note 1 : If your tables have relations, you need to specify the foreigns key in the correct table and follow this convention for its name `id_NameOfTheTable`.

Note 2 : An `id` field is not mandatory, Memory uses the `ROWID` field of each row to maintain the relations through the database.

## Creating and deleting tables

The first thing you need to do is instantiating the only object you need to use the library by passing the context and our database description as a String:

``` kotlin
val memory = Memory(this, jsonDatabase)
memory.createTables()
```

## Save in database

Memory makes it really easy, it also supports nested oject and list : 

``` kotlin
val finnGear = listOf(Gear("sword", true, "sword1"), Gear("shield", false, "shield1"))
val finnHouse = House("tree-house", "unknown", "finnTreeHouse")
val finn = Profile("finn", 13, true, finnGear, finnHouse, "profile0")

memory.save(finn)
```

The function `save` returns you the rowId value of the row created by the insertion.

## Fetch rows from a table

It's as simple as saving:

``` kotlin
val profiles = memory.fetchAll(Profile::class.java)
for (profile in profiles) {
    //do some stuff...
}
```

Two other methods are available : `fetchFirst`and `fetchById`

## Update a row

```kotlin
val profile = memory.fetchFirst(Profile::class.java)
profile.name = "new name"

memory.update(name)
```

Finally, in the case you don't know if an item does not exists in your DB, you can use the `saveOrUpdate` method that check if an item with the same id exists. (Your table need to declare explicitely an id field for this to work)
