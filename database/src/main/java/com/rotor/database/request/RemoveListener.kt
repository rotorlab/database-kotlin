package com.rotor.database.request

import com.google.gson.annotations.SerializedName

/**
 * Created by efraespada on 11/03/2018.
 */
data class RemoveListener(@SerializedName("method") val method: String,
                          @SerializedName("path") val path: String,
                          @SerializedName("token") val token: String)