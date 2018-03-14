package com.rotor.database.request

import com.google.gson.annotations.SerializedName

/**
 * Created by efraespada on 11/03/2018.
 */

data class CreateListener(@SerializedName("method") val method: String,
                          @SerializedName("path") val path: String,
                          @SerializedName("token") val token: String,
                          @SerializedName("os") val os: String,
                          @SerializedName("sha1") val sha1: String,
                          @SerializedName("len") val len: Int)