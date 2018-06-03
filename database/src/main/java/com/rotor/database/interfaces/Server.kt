package com.rotor.database.interfaces

import com.google.gson.internal.LinkedTreeMap
import com.rotor.database.request.*
import io.reactivex.Observable
import org.json.JSONArray
import retrofit2.http.*

/**
 * Created by efraespada on 14/03/2018.
 */
interface Server {

    @Headers("Content-Type: application/json")
    @POST("/")
    fun createReference(@Body createListener: CreateListener) : Observable<SyncResponse>

    @Headers("Content-Type: application/json")
    @POST("/")
    fun removeListener(@Body removeListener: RemoveListener) : Observable<SyncResponse>

    @Headers("Content-Type: application/json")
    @POST("/")
    fun refreshFromServer(@Body updateFromServer: UpdateFromServer) : Observable<SyncResponse>

    @Headers("Content-Type: application/json")
    @POST("/")
    fun refreshToServer(@Body updateToServer: UpdateToServer) : Observable<SyncResponse>

    @Headers("Content-Type: application/json")
    @POST("/")
    fun removeReference(@Body removeReference: RemoveReference) : Observable<SyncResponse>

    @Headers("Content-Type: application/json")
    @GET("/")
    fun query(@Query("token") token: String, @Query("database") database: String, @Query("path") path: String, @Query("query") query: String, @Query("mask") mask: String) : Observable<List<LinkedTreeMap<String, String>>>

}