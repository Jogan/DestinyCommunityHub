package com.opencabinetlabs.destinycommunityhub.api;

import java.util.List;

import timber.log.Timber;

import com.opencabinetlabs.destinycommunityhub.model.NewsFeed;
import com.opencabinetlabs.destinycommunityhub.model.NewsFeedData;
import com.opencabinetlabs.destinycommunityhub.model.PodcastFeed;
import com.opencabinetlabs.destinycommunityhub.model.PodcastFeedData;
import com.opencabinetlabs.destinycommunityhub.model.TwitterListResult.Status;


public class ApiManager implements IApiManager {
	private static final String TAG = ApiManager.class.getSimpleName();

    private final CommunityRssApi mCommunityRssApi;
    private final TwitterApi mTwitterApi;
    private final DispatchPodcastRssApi mDispatchApi;
    private final GuardianPodcastRssApi mGuardianApi;

    public ApiManager(CommunityRssApi communityRssApi, TwitterApi twitterApi, DispatchPodcastRssApi dispatchApi, GuardianPodcastRssApi guardianApi) {
        mCommunityRssApi = communityRssApi;
        mTwitterApi = twitterApi;
        mDispatchApi = dispatchApi;
        mGuardianApi = guardianApi;
    }

    @Override
    public NewsFeedData getCommunityNewsFeed(final int fromResult) {
        NewsFeed response = mCommunityRssApi.getCommunityNewsFeed(CommunityRssApi.NUM_RESULTS, fromResult);
        Timber.d(TAG+" GETTING COMM NEWS");
        return response == null ?
                null : response.getNewsData();
    }
    
    @Override
    public List<Status> getTimelineList(String list_id, int include_rts){
    	List<Status> response = mTwitterApi.getTimeLineList(list_id, include_rts);
    	Timber.d(TAG+" GETTING TWITTER TIMELINE LIST");
    	return response == null ? null : response;
    	
    }
    
    @Override
    public PodcastFeedData getDispatchPodcastFeed() {
        PodcastFeed response = mDispatchApi.getHDDispatchFeed();
        Timber.d(TAG+" GETTING DISPATCH FEED");
        return response == null ?
                null : response.getPodcastFeedData();
    }
    
    @Override
    public PodcastFeedData getGuardianPodcastFeed() {
        PodcastFeed response = mGuardianApi.getGuardianRadioFeed();
        Timber.d(TAG+" GETTING GUARDIAN RADIO FEED");
        return response == null ?
                null : response.getPodcastFeedData();
    }

    

   
}


