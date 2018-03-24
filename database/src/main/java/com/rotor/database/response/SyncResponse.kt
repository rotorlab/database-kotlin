package com.rotor.database.request

import org.json.JSONObject

/**
 * Created by efraespada on 11/03/2018.
 */
data class SyncResponse(val status: String,
                   val data: JSONObject,
                   val error: String)