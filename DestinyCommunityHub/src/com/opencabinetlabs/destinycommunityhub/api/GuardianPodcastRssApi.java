package com.opencabinetlabs.destinycommunityhub.api;

import retrofit.Callback;
import retrofit.http.GET;

import com.opencabinetlabs.destinycommunityhub.model.PodcastFeed;

public interface GuardianPodcastRssApi {
	
	 @GET("/gameinsiderpodcast/Podcasts/guardianradio.xml")
	    public PodcastFeed getGuardianRadioFeed();  
	 
	 @GET("/gameinsiderpodcast/Podcasts/guardianradio.xml")
	    void  getGuardianRadioFeedCallback(Callback<PodcastFeed> callback); 

}
