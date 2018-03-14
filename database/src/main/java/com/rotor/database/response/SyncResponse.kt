package com.rotor.database.request

import com.google.gson.JsonObject
import com.google.gson.annotations.SerializedName

/**
 * Created by efraespada on 11/03/2018.
 */

data class SyncResponse(@SerializedName("status")  val status: String,
                        @SerializedName("data") val data: JsonObject,
                        @SerializedName("error") val error: String)