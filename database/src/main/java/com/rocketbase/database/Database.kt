package com.rocketbase.database

import com.flamebase.core.Flamebase

/**
 * Created by efrainespada on 12/03/2018.
 */
class Database  {

    companion object {
        fun hi() {

        }
    }

    inline fun Flamebase.database() : Database ? {
        return this@Database
    }

}