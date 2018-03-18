package com.rotor.database.abstr

import android.support.annotation.NonNull
import android.support.annotation.Nullable

/**
 * Created by efraespada on 14/03/2018.
 */
abstract class Reference<T>(clazz: Class<T>) {

    val clazz: Class<T>

    init {
        this.clazz = clazz
    }

    abstract fun onCreate()

    @Nullable
    abstract fun onUpdate() : T ?

    abstract fun onChanged(@NonNull ref: T)

    abstract fun progress(value: Int)

    fun clazz() : Class<T> {
        return clazz
    }

}