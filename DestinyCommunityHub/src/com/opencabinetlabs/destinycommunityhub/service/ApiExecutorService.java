package com.opencabinetlabs.destinycommunityhub.service;

import java.util.List;

import javax.inject.Inject;

import timber.log.Timber;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.opencabinetlabs.destinycommunityhub.CommunityHubApp;
import com.opencabinetlabs.destinycommunityhub.api.IApiManager;
import com.opencabinetlabs.destinycommunityhub.api.IPersistenceManager;
import com.opencabinetlabs.destinycommunityhub.api.TwitterApi;
import com.opencabinetlabs.destinycommunityhub.data.DestinyCommunityHubContentProvider;
import com.opencabinetlabs.destinycommunityhub.model.NewsFeed;
import com.opencabinetlabs.destinycommunityhub.model.NewsFeedData;
import com.opencabinetlabs.destinycommunityhub.model.PodcastFeedData;
import com.opencabinetlabs.destinycommunityhub.model.TwitterListResult;
import com.opencabinetlabs.destinycommunityhub.model.TwitterListResult.Status;
import com.opencabinetlabs.destinycommunityhub.modules.ApiModule;

/**
 * Dispatches API calls on a background thread
 */
public class ApiExecutorService extends BaseApiService {
	private static final String EXTRA_COMMUNITY_RSS_FROM_PAGE = "fromResult";
	private static final String EXTRA_OFFICIAL_RSS_PAGE = "pageNum";
	private static final String EXTRA_TWITTER_TOKEN = "token";

	@Inject
	IApiManager mApiManager;

	@Inject
	IPersistenceManager mPersistenceManager;

	@Override
	public void onCreate() {
		super.onCreate();

		CommunityHubApp app = (CommunityHubApp) getApplication();
		app.inject(this, new ApiModule());
	}

	@Override
	protected void handleApiRequest(final String token, final Bundle extras) {
		if(token.equalsIgnoreCase(AsyncRequest.ACTION_GET_COMMUNITY_NEWS_FEED)){
			final int fromResult = extras == null ? null : extras.getInt(EXTRA_COMMUNITY_RSS_FROM_PAGE, 0);

			NewsFeedData newsFeed = mApiManager.getCommunityNewsFeed(fromResult);

			if (newsFeed != null && !newsFeed.getNews().isEmpty()) {
				Timber.d("Got %d newsFeed", newsFeed.getNews().size());
				mPersistenceManager.persistCommunityNewsFeedItems(newsFeed.getNews());
				getContentResolver().notifyChange(DestinyCommunityHubContentProvider.COMMUNITY_NEWS_FEED_LOADER_URI, null, false);
			}
		}else if(token.equalsIgnoreCase(AsyncRequest.ACTION_GET_TWITTER_FEED)){
			final String twitterToken = extras == null ? null : extras.getString(EXTRA_TWITTER_TOKEN, null);

			List<Status> result = mApiManager.getTimelineList(
					TwitterApi.LIST_ID, 
					TwitterApi.INCLUDE_RTS
					);

			if (result != null && !result.isEmpty()) {
				Timber.d("Got %d twitterResponse", result.size());
				mPersistenceManager.persistTwitterFeedItems(result);
				getContentResolver().notifyChange(DestinyCommunityHubContentProvider.TWITTER_FEED_LOADER_URI, null, false);
			}
		}else if(token.equalsIgnoreCase(AsyncRequest.ACTION_GET_DISPATCH_FEED)){
			PodcastFeedData podcastFeed = mApiManager.getDispatchPodcastFeed();

			if (podcastFeed != null && !podcastFeed.getPodcasts().isEmpty()) {
				Timber.d("Got %d dispatch feed", podcastFeed.getPodcasts().size());
				mPersistenceManager.persistPodcastItems(podcastFeed.getPodcasts(),podcastFeed.getTitle());
				getContentResolver().notifyChange(DestinyCommunityHubContentProvider.PODCAST_FEED_LOADER_URI, null, false);
			}
		}else if(token.equalsIgnoreCase(AsyncRequest.ACTION_GET_GUARDIAN_FEED)){
			PodcastFeedData podcastFeed = mApiManager.getGuardianPodcastFeed();

			if (podcastFeed != null && !podcastFeed.getPodcasts().isEmpty()) {
				Timber.d("Got %d guardian feed", podcastFeed.getPodcasts().size());
				mPersistenceManager.persistPodcastItems(podcastFeed.getPodcasts(),podcastFeed.getTitle());
				getContentResolver().notifyChange(DestinyCommunityHubContentProvider.PODCAST_FEED_LOADER_URI, null, false);
			}
		}
	}

	/**
	 * Fires off asynchronous requests for API calls
	 */
	public static class AsyncRequest {

		public static final String ACTION_GET_COMMUNITY_NEWS_FEED = "get_community_news_feed";
		public static final String ACTION_GET_OFFICIAL_NEWS_FEED = "get_official_news_feed";
		public static final String ACTION_GET_TWITTER_FEED = "get_twitter_feed";
		public static final String ACTION_GET_DISPATCH_FEED = "get_dispatch_feed";
		public static final String ACTION_GET_GUARDIAN_FEED = "get_guardian_feed";

		/**
		 * Asynchronously Retrieve community news feed items from <code>fromResult</code>
		 *
		 * @param context
		 * @param fromResult 
		 * @return A token which can be used with an {@link ApiBroadcastReceiver}
		 * to listen for results
		 */
		public static String getCommunityNewsFeed(Context context, int fromResult) {
			final Intent intent = new Intent(context, ApiExecutorService.class);
			intent.setAction(ACTION_GET_COMMUNITY_NEWS_FEED);

			intent.putExtra(ApiExecutorService.EXTRA_COMMUNITY_RSS_FROM_PAGE, fromResult);

			context.startService(intent);
			return intent.getAction();
		}
		public static String getOfficialNewsFeed(Context context, int pageNum) {
			final Intent intent = new Intent(context, ApiExecutorService.class);
			intent.setAction(ACTION_GET_OFFICIAL_NEWS_FEED);

			intent.putExtra(ApiExecutorService.EXTRA_OFFICIAL_RSS_PAGE, pageNum);

			context.startService(intent);
			return intent.getAction();
		}
		public static String getTwitterFeed(Context context) {
			final Intent intent = new Intent(context, ApiExecutorService.class);

			intent.setAction(ACTION_GET_TWITTER_FEED);

			context.startService(intent);
			return intent.getAction();
		}
		public static String getDispatchPodcastFeed(Context context) {
			final Intent intent = new Intent(context, ApiExecutorService.class);
			intent.setAction(ACTION_GET_DISPATCH_FEED);
			context.startService(intent);
			return intent.getAction();
		}
		public static String getGuardianPodcastFeed(Context context) {
			final Intent intent = new Intent(context, ApiExecutorService.class);
			intent.setAction(ACTION_GET_GUARDIAN_FEED);
			context.startService(intent);
			return intent.getAction();
		}

	}


}
