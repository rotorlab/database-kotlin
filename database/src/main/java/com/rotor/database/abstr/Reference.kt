package com.rotor.database.abstr

import android.support.annotation.NonNull
import android.support.annotation.Nullable

/**
 * Created by efraespada on 14/03/2018.
 */
abstract class Reference<T> {

    abstract fun onCreate()

    @Nullable
    abstract fun onUpdate() : T

    abstract fun onChanged(@NonNull ref: T)

    abstract fun progress(value: Int)

    inline fun <reified T : Any> clazz() : Class<T> {
        return T::class.java
    }

}