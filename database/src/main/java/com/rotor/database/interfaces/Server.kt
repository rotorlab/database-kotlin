package com.rotor.database.interfaces

import com.rotor.database.request.*
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

/**
 * Created by efraespada on 14/03/2018.
 */
abstract class Server {

    @Headers("Content-Type: application/json")
    @POST("/")
    abstract fun createReference(@Body createListener: CreateListener): Call<SyncResponse>

    @Headers("Content-Type: application/json")
    @POST("/")
    abstract fun removeListener(@Body removeListener: RemoveListener): Call<SyncResponse>

    @Headers("Content-Type: application/json")
    @POST("/")
    abstract fun refreshFromServer(@Body updateFromServer: UpdateFromServer): Call<SyncResponse>

    @Headers("Content-Type: application/json")
    @POST("/")
    abstract fun refreshToServer(@Body updateToServer: UpdateToServer): Call<SyncResponse>

}