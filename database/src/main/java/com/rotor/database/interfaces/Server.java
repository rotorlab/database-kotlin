package com.rotor.database.interfaces;

import com.rotor.database.request.CreateListener;
import com.rotor.database.request.RemoveListener;
import com.rotor.database.request.SyncResponse;
import com.rotor.database.request.UpdateFromServer;
import com.rotor.database.request.UpdateToServer;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

/**
 * Created by efraespada on 08/07/2017.
 */

public interface Server {

    @Headers("Content-Type: application/json")
    @POST("/")
    Call<SyncResponse> createReference(@Body CreateListener createListener);

    @Headers("Content-Type: application/json")
    @POST("/")
    Call<SyncResponse> removeListener(@Body RemoveListener removeListener);

    @Headers("Content-Type: application/json")
    @POST("/")
    Call<SyncResponse> refreshFromServer(@Body UpdateFromServer updateFromServer);

    @Headers("Content-Type: application/json")
    @POST("/")
    Call<SyncResponse> refreshToServer(@Body UpdateToServer updateToServer);
}