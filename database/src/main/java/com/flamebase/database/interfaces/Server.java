package com.flamebase.database.interfaces;

import com.flamebase.core.request.CreateListener;
import com.flamebase.core.request.RemoveListener;
import com.flamebase.core.request.UpdateFromServer;
import com.flamebase.core.request.UpdateToServer;
import com.flamebase.core.response.SyncResponse;

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