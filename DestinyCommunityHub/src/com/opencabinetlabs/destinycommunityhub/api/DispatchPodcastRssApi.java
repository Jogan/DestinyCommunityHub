package com.opencabinetlabs.destinycommunityhub.api;

import retrofit.Callback;
import retrofit.http.GET;

import com.opencabinetlabs.destinycommunityhub.model.PodcastFeed;

public interface DispatchPodcastRssApi {
	 
	 @GET("/column/ddpodcast/feed/")
	    public PodcastFeed getHDDispatchFeed(); 
	 
	 @GET("/column/ddpodcast/feed/")
	    void  getHDDispatchFeedCallback(Callback<PodcastFeed> callback); 

}
