package com.opencabinetlabs.destinycommunityhub.api;

import java.util.List;

import com.opencabinetlabs.destinycommunityhub.data.Db;
import com.opencabinetlabs.destinycommunityhub.data.DestinyCommunityHubContentProvider;
import com.opencabinetlabs.destinycommunityhub.model.NewsFeedItem;
import com.opencabinetlabs.destinycommunityhub.model.PodcastFeedItem;
import com.opencabinetlabs.destinycommunityhub.model.TwitterListResult.Status;
import com.opencabinetlabs.destinycommunityhub.util.DaoUtils;

import android.content.Context;


public class PersistenceManager implements IPersistenceManager {

	private final Context mContext;

    private int LIMIT = 100;

	public PersistenceManager(Context context) {
		mContext = context.getApplicationContext();
	}

	@Override
	public void persistCommunityNewsFeedItems(List<NewsFeedItem> newsFeedItems) {
		if (newsFeedItems != null && !newsFeedItems.isEmpty()) {
			for (NewsFeedItem item : newsFeedItems) {
				mContext.getContentResolver()
				.insert(DestinyCommunityHubContentProvider.COMMUNITY_NEWS_FEED_URI, DaoUtils.convert(item));
			}
            Db db = Db.get(mContext);
            db.getReadableDatabase().execSQL("DELETE FROM community_news_feed where _id NOT IN (SELECT _id from community_news_feed ORDER BY date DESC LIMIT "+LIMIT+")");
        }
	}

	@Override
	public void persistTwitterFeedItems(List<Status> twitterFeedItems) {
		if (twitterFeedItems != null && !twitterFeedItems.isEmpty()) {
			for (Status item : twitterFeedItems){
				mContext.getContentResolver()
				.insert(DestinyCommunityHubContentProvider.TWITTER_FEED_URI, DaoUtils.convert(item));
			}
            Db db = Db.get(mContext);
            db.getReadableDatabase().execSQL("DELETE FROM twitter_feed where _id NOT IN (SELECT _id from twitter_feed ORDER BY date DESC LIMIT "+LIMIT+")");
		}
	}

	@Override
	public void persistPodcastItems(List<PodcastFeedItem> podcasts, String author) {
		if (podcasts != null && !podcasts.isEmpty()) {
			for (PodcastFeedItem item : podcasts) {
				item.setAuthor(author);
				if(item.getInfo() != null){ // Fix for posts without an enclousure aka a stream url
					mContext.getContentResolver()
					.insert(DestinyCommunityHubContentProvider.PODCAST_FEED_URI, DaoUtils.convert(item));
				}
			}
            Db db = Db.get(mContext);
            db.getReadableDatabase().execSQL("DELETE FROM podcast_feed where _id NOT IN (SELECT _id from podcast_feed ORDER BY date DESC LIMIT "+LIMIT+")");
		}
	}


}
