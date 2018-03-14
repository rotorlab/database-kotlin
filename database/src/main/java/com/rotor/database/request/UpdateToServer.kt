package com.rotor.database.request

import com.google.gson.annotations.SerializedName

/**
 * Created by efraespada on 11/03/2018.
 */

data class UpdateToServer(@SerializedName("method") val method: String,
                          @SerializedName("path") val path: String,
                          @SerializedName("token") val token: String,
                          @SerializedName("os") val os: String,
                          @SerializedName("differences") val differences: String,
                          @SerializedName("len") val len: Int,
                          @SerializedName("clean") val clean: Boolean)