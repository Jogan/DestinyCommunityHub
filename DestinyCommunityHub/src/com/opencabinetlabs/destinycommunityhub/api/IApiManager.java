package com.opencabinetlabs.destinycommunityhub.api;

import java.util.List;

import org.json.JSONObject;

import retrofit.Callback;

import com.opencabinetlabs.destinycommunityhub.model.NewsFeedData;
import com.opencabinetlabs.destinycommunityhub.model.PodcastFeedData;
import com.opencabinetlabs.destinycommunityhub.model.TwitterListResult;
import com.opencabinetlabs.destinycommunityhub.model.TwitterListResult.Status;
import com.opencabinetlabs.destinycommunityhub.model.YoutubeSearchResponse;


/**
 * Responsible for making raw Api calls and persisting any results
 */
public interface IApiManager {

    public NewsFeedData getCommunityNewsFeed(int fromResult);

	public List<Status> getTimelineList(String list_id, int include_rts);

	public PodcastFeedData getGuardianPodcastFeed();

	public PodcastFeedData getDispatchPodcastFeed();
	

	//public NewsFeedData getBungieNewsFeed(int pageNum);

	/*public YoutubeSearchResponse getYoutubeChannelVideos(String channelId,
			String nextPageToken);*/

}
