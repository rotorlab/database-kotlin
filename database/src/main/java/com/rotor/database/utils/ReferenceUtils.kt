package com.rotor.database.utils

import android.content.ContentValues
import android.database.sqlite.SQLiteException
import com.rotor.core.Rotor
import com.rotor.database.Docker
import com.rotor.database.Docker.Companion.COLUMN_DATA
import com.rotor.database.Docker.Companion.COLUMN_ID
import com.rotor.database.interfaces.Server
import com.stringcare.library.SC
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.math.BigInteger

/**
 * Created by efraespada on 14/03/2018.
 */
class ReferenceUtils {

    companion object {

        var docker: Docker? = null
        private val VERSION = 1
        private val TABLE_NAME = "ref"

        fun addElement(path: String, info: String) {
            if (docker == null) {
                val name = "RealtimeDatabase.db"
                docker = Docker(Rotor.context!!, name, TABLE_NAME, VERSION)
            }
            try {
                val enId = SC.encryptString(path)
                val db = docker!!.getWritableDatabase()

                val values = ContentValues()
                values.put(COLUMN_ID, enId)
                values.put(COLUMN_DATA, SC.encryptString(info))

                if (exist(path)) {
                    val selection = COLUMN_ID + " = ?"
                    val selectionArgs = arrayOf<String>(enId)
                    db.update(docker!!.table, values, selection, selectionArgs).toLong()
                } else {
                    db.insert(docker!!.table, null, values)
                }
            } catch (e: SQLiteException) {
                e.printStackTrace()
            }

        }

        fun exist(path: String): Boolean {
            if (docker == null) {
                val name = "RealtimeDatabase.db"
                docker = Docker(Rotor.context!!, name, TABLE_NAME, VERSION)
            }
            val enPath = SC.encryptString(path)
            try {
                val db = docker!!.getReadableDatabase()

                // Define a projection that specifies which columns from the database
                // you will actually use after this query.
                val projection = arrayOf(COLUMN_ID, COLUMN_DATA)

                // Filter results WHERE "title" = hash
                val selection = "$COLUMN_ID = ?"
                val selectionArgs = arrayOf(enPath)

                val cursor = db.query(
                        docker!!.table, // The table to query
                        projection, // The columns to return
                        selection, // The columns for the WHERE clause
                        selectionArgs, null, null, null
                )// The values for the WHERE clause
                // don't group the rows

                val exists = cursor.getCount() > 0
                cursor.close()
                //database.close();

                return exists
            } catch (e: SQLiteException) {
                e.printStackTrace()
                return false
            }

        }

        fun removeElement(path: String) {
            if (docker == null) {
                val name = "RealtimeDatabase.db"
                docker = Docker(Rotor.context!!, name, TABLE_NAME, VERSION)
            }
            val enPath = SC.encryptString(path)
            try {
                val db = docker!!.getReadableDatabase()

                val selection = "$COLUMN_ID = ?"
                val selectionArgs = arrayOf(enPath)

                db.delete(
                        docker!!.table, // The table to query
                        selection, // The columns for the WHERE clause
                        selectionArgs                            // The values for the WHERE clause
                )

            } catch (e: SQLiteException) {
                e.printStackTrace()
            }

        }


        /**
         * returns stored object
         * @param path
         * @return String
         */
        fun getElement(path: String): String? {
            if (docker == null) {
                val name = "RealtimeDatabase.db"
                docker = Docker(Rotor.context!!, name, TABLE_NAME, VERSION)
            }
            val enPath = SC.encryptString(path)
            try {
                val db = docker!!.getReadableDatabase()
                val projection = arrayOf(COLUMN_ID, COLUMN_DATA)

                val selection = "$COLUMN_ID = ?"
                val selectionArgs = arrayOf(enPath)

                val cursor = db.query(
                        docker!!.table, // The table to query
                        projection, // The columns to return
                        selection, // The columns for the WHERE clause
                        selectionArgs, null, null, null// The sort order
                )// The values for the WHERE clause
                // don't group the rows
                // don't filter by row groups

                var info: String? = null
                while (cursor.moveToNext()) {
                    info = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DATA))
                }
                cursor.close()

                return if (info != null) {
                    SC.decryptString(info)
                } else {
                    null
                }
            } catch (e: SQLiteException) {
                return null
            }

        }

        private fun string2Hex(data: ByteArray): String {
            return BigInteger(1, data).toString(16)
        }

        fun hex2String(value: String): String {
            return String(BigInteger(value, 16).toByteArray())
        }

        fun service(url: String): Server {
            val retrofit = Retrofit.Builder()
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                    .addConverterFactory(GsonConverterFactory.create())
                    .baseUrl(url)
                    .build()

            return retrofit.create(Server::class.java)
        }

    }

}