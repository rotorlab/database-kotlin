package com.rotor.sample.model

import com.google.gson.annotations.SerializedName

/**
 * Created by efraespada on 15/03/2018.
 */
data class Message(@SerializedName("author") var author: String,
                   @SerializedName("text") var text: String)