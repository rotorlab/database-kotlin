package com.rotor.database

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteException
import android.database.sqlite.SQLiteOpenHelper

/**
 * Created by efraespada on 14/03/2018.
 */
class Docker(context: Context, name: String, table: String, version: Int) : SQLiteOpenHelper(context, name, null, version) {

    companion object {
        val COLUMN_ID = "id_hash"
        val COLUMN_DATA = "data"
    }

    var dbName: String
    var table: String

    init {
        this.dbName = name
        this.table = table
    }

    override fun onCreate(db: SQLiteDatabase?) {
        val SQL_CREATE_ENTRIES = "CREATE TABLE " + table + " (" +
                COLUMN_ID + " TEXT PRIMARY KEY, " +
                COLUMN_DATA + " TEXT);"
        db?.execSQL(SQL_CREATE_ENTRIES)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        try {
            val SQL_DELETE_ENTRIES = "DROP TABLE IF EXISTS $table"
            db?.execSQL(SQL_DELETE_ENTRIES)
        } catch (e: SQLiteException) {
            e.printStackTrace()
        } finally {
            onCreate(db)
        }
    }

    override fun onDowngrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        onUpgrade(db, oldVersion, newVersion)
    }

}