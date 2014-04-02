package com.opencabinetlabs.destinycommunityhub.api;

import java.util.List;

import com.opencabinetlabs.destinycommunityhub.model.NewsFeedItem;
import com.opencabinetlabs.destinycommunityhub.model.PodcastFeedItem;
import com.opencabinetlabs.destinycommunityhub.model.TwitterListResult;

/**
 * Responsible for persisting the results of any API calls
 */
public interface IPersistenceManager {

	/**
	 * @param newsFeedItems List of community news feed items to persist
	 */
	public void persistCommunityNewsFeedItems(List<NewsFeedItem> newsFeedItems);

	public void persistTwitterFeedItems(List<TwitterListResult.Status> twitterFeedItems);

	public void persistPodcastItems(List<PodcastFeedItem> podcasts, String author);

}
