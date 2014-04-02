package com.opencabinetlabs.destinycommunityhub.modules;

import javax.inject.Named;
import javax.inject.Singleton;

import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;

import retrofit.RestAdapter;

import com.opencabinetlabs.destinycommunityhub.CommunityHubApp;
import com.opencabinetlabs.destinycommunityhub.CommunityHubConfig;
import com.opencabinetlabs.destinycommunityhub.api.ApiManager;
import com.opencabinetlabs.destinycommunityhub.api.CommunityRssApi;
import com.opencabinetlabs.destinycommunityhub.api.DispatchPodcastRssApi;
import com.opencabinetlabs.destinycommunityhub.api.GuardianPodcastRssApi;
import com.opencabinetlabs.destinycommunityhub.api.IApiManager;
import com.opencabinetlabs.destinycommunityhub.api.IPersistenceManager;
import com.opencabinetlabs.destinycommunityhub.api.PersistenceManager;
import com.opencabinetlabs.destinycommunityhub.api.TwitterApi;
import com.opencabinetlabs.destinycommunityhub.api.YoutubeApi;
import com.opencabinetlabs.destinycommunityhub.service.ApiExecutorService;
import com.opencabinetlabs.destinycommunityhub.ui.VideoWallActivity;
import com.opencabinetlabs.destinycommunityhub.ui.fragment.PodcastFragment;
import com.opencabinetlabs.destinycommunityhub.util.SimpleXMLConverter;

import dagger.Module;
import dagger.Provides;


/**
 * Provides access to the various APIs
 */
@Module(
		injects = {
				ApiExecutorService.class,
				VideoWallActivity.class,
				PodcastFragment.class

		},
		complete = false,
		library = true
		)
public class ApiModule {

	private static final Serializer serializer = new Persister();

	@Provides
	@Singleton
	public CommunityRssApi providesCommunityRssApi() {
		return new RestAdapter.Builder()
		.setServer(CommunityHubConfig.DESTINY_SERVER_BASE)
		.setConverter(new SimpleXMLConverter(serializer))
		.build()
		.create(CommunityRssApi.class);
	}

	@Provides
	@Singleton
	public BungieRssApi providesBungieRssApi() {
		return new RestAdapter.Builder()
		.setServer(CommunityHubConfig.BUNGIE_NEWS_BASE)
		.setConverter(new SimpleXMLConverter(serializer))
		.build()
		.create(BungieRssApi.class);

	}

	@Provides
	@Singleton
	public YoutubeApi providesYoutubeApi() {
		return new RestAdapter.Builder()
		.setServer(CommunityHubConfig.YOUTUBE_API_BASE)
		.build()
		.create(YoutubeApi.class);

	}

	@Provides
	@Singleton
	public TwitterApi providesTwitterApi(){
		return new RestAdapter.Builder()
		.setServer(CommunityHubConfig.DESTINY_SERVER_BASE)
		.build()
		.create(TwitterApi.class);
	}

	@Provides
	@Singleton
	public GuardianPodcastRssApi providesGuardianPodcastRssApi(){	
		return new RestAdapter.Builder()
		.setConverter(new SimpleXMLConverter(serializer))
		.setServer("http://www.game-insider.com")
		.build()
		.create(GuardianPodcastRssApi.class);
	}
	
	@Provides
	@Singleton
	public DispatchPodcastRssApi providesDispatchPodcastRssApi(){	
		return new RestAdapter.Builder()
		.setConverter(new SimpleXMLConverter(serializer))
		.setServer("http://www.hddispatch.net")
		.build()
		.create(DispatchPodcastRssApi.class);
	}

	@Provides
	@Singleton
	public IPersistenceManager providesPersistenceManager(CommunityHubApp app) {
		return new PersistenceManager(app);
	}

	@Provides
	@Singleton
	public IApiManager providesApiManager(CommunityRssApi rssApi, TwitterApi twitterApi, DispatchPodcastRssApi dispatchApi, GuardianPodcastRssApi guardianApi) {
		return new ApiManager(rssApi, twitterApi, dispatchApi, guardianApi);
	}
}
