package no.hyper.memoryormdemo

import android.os.Parcel

/**
 * Created by jean on 31.08.2016.
 */

fun <T> Parcel.customReadList(classLoader: ClassLoader) : List<T> {
    val list = mutableListOf<T>()
    readList(list, classLoader)
    return list
}

fun <T> Parcel.customReadParcelableArray(classLoader: ClassLoader) : List<T> {
    val array = readParcelableArray(classLoader)
    return array.toList() as List<T>
}