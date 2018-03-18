package com.rotor.database.request

/**
 * Created by efraespada on 11/03/2018.
 */

data class CreateListener(val method: String,
                          val path: String,
                          val token: String,
                          val os: String,
                          val sha1: String,
                          val len: Int)