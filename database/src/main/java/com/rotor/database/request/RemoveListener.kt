package com.rotor.database.request

/**
 * Created by efraespada on 11/03/2018.
 */
data class RemoveListener(val method: String,
                          val database: String,
                          val path: String,
                          val token: String)