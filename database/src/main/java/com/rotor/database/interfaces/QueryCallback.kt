package com.rotor.database.interfaces

import com.google.gson.internal.LinkedTreeMap

interface QueryCallback {

    fun response(list: List<LinkedTreeMap<String, String>>)

}