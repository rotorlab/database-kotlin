package com.rotor.sample.model

import com.google.gson.annotations.SerializedName

/**
 * Created by efraespada on 15/03/2018.
 */
data class Chat(@SerializedName("name") var name: String,
                @SerializedName("messages") val messages: HashMap<String, Message>)