package com.opencabinetlabs.destinycommunityhub.modules;

import javax.inject.Singleton;

import com.opencabinetlabs.destinycommunityhub.dao.CommunityRssDao;
import com.opencabinetlabs.destinycommunityhub.dao.PodcastFeedItemDao;
import com.opencabinetlabs.destinycommunityhub.dao.TwitterFeedItemDao;
import com.opencabinetlabs.destinycommunityhub.util.DaoUtils;

import dagger.Module;
import dagger.Provides;

/**
 * Provides access to our model DAO objects
 */
@Module(
        staticInjections = {
                DaoUtils.class
        }
)
public class DaoModule {

    @Provides
    @Singleton
    public CommunityRssDao providesCommunityRssDao() {
        return new CommunityRssDao();
    }
    
    @Provides
    @Singleton
    public TwitterFeedItemDao providesTwitterFeedItemDao() {
    	return new TwitterFeedItemDao(); 	
    }
    
    @Provides
    @Singleton
    public PodcastFeedItemDao providesPodcastFeedItemDao() {
    	return new PodcastFeedItemDao();
    	
    }
}