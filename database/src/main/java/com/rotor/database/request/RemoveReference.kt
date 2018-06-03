package com.rotor.database.request

/**
 * Created by efraespada on 11/03/2018.
 */

data class RemoveReference(val method: String,
                           val database: String,
                           val path: String,
                           val token: String)