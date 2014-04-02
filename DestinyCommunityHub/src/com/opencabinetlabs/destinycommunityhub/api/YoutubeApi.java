package com.opencabinetlabs.destinycommunityhub.api;

import com.opencabinetlabs.destinycommunityhub.model.YoutubeChannelImage;
import com.opencabinetlabs.destinycommunityhub.model.YoutubeSearchResponse;

import retrofit.Callback;
import retrofit.http.GET;
import retrofit.http.Query;



public interface YoutubeApi {

	public static final String YOUTUBE_VIDEO_ENDPOINT="http://www.youtube.com/watch?v=";
	public static int YOUTUBE_DEFAULT_PAGE_SIZE = 10;
	public static final String YOUTUBE_DEFAULT_SEARCH_TERM = "Destiny";
	public static final String YOUTUBE_PART = "snippet";
	public static final String YOUTUBE_SORT_BY = "date";

	@GET("/search")
	void getYoutubeChannelVideos(@Query("part") String YOUTUBE_PART,
								 @Query("channelId") String channelId,
								 @Query("maxResults") int YOUTUBE_DEFAULT_PAGE_SIZE,
								 @Query("order") String YOUTUBE_SORT_BY,
								 @Query("q") String YOUTUBE_DEFAULT_SEARCH_TERM,
								 @Query("key") String YOUTUBE_API_KEY, Callback<YoutubeSearchResponse> callback);
	
	@GET("/search")
	void getYoutubeChannelVideosWithPage(@Query("part") String YOUTUBE_PART,
										 @Query("channelId") String channelId,
										 @Query("maxResults") int YOUTUBE_DEFAULT_PAGE_SIZE,
										 @Query("order") String YOUTUBE_SORT_BY,
										 @Query("pageToken") String nextPageToken,
										 @Query("q") String YOUTUBE_DEFAULT_SEARCH_TERM,
										 @Query("key") String YOUTUBE_API_KEY, Callback<YoutubeSearchResponse> callback);
	
							
	@GET("/channels")
	public YoutubeChannelImage getYoutubeChannelImage(@Query("part") String YOUTUBE_PART_BRANDING,
													  @Query("id") String channelId,
													  @Query("fields") String YOUTUBE_BANNER_FIELDS,
													  @Query("key") String YOUTUBE_API_KEY);
																					

}
