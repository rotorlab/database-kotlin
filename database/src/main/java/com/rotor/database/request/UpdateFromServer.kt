package com.rotor.database.request

/**
 * Created by efraespada on 11/03/2018.
 */

data class UpdateFromServer( val method: String,
                        val path: String,
                        val token: String,
                        val os: String,
                        val content: String,
                        val len: Int)