package com.opencabinetlabs.destinycommunityhub.api;

import java.util.List;

import retrofit.Callback;
import retrofit.http.Field;
import retrofit.http.FormUrlEncoded;
import retrofit.http.GET;
import retrofit.http.Header;
import retrofit.http.POST;
import retrofit.http.Query;

import com.opencabinetlabs.destinycommunityhub.model.TwitterListResult.Status;

public interface TwitterApi {
    public static final String LIST_ID = "101223077";
    public static final int INCLUDE_RTS = 0;


    @GET("/streamfeed")
    public List<Status> getTimeLineList(@Query("list_id") String list_id, @Query("include_rts") int include_rts);

}
