package com.opencabinetlabs.destinycommunityhub.api;

import com.opencabinetlabs.destinycommunityhub.model.NewsFeed;

import retrofit.http.GET;
import retrofit.http.Query;

public interface CommunityRssApi {
	
	public static final int INITIAL_PAGE = 0;
	
	 /**
     * The maximum number of results to return
     */
    public static final int NUM_RESULTS = 50;

    @GET("/newsfeed")
    public NewsFeed getCommunityNewsFeed(@Query("entries") int numEntries,
                                         @Query("from") int fromResult);

}
