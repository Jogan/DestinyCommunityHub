package com.opencabinetlabs.destinycommunityhub.modules;

import javax.inject.Singleton;

import com.opencabinetlabs.destinycommunityhub.service.mediaplayer.PodcastService;
import com.opencabinetlabs.destinycommunityhub.ui.MainActivity;
import com.opencabinetlabs.destinycommunityhub.ui.fragment.CommunityNewsFragment;
import com.opencabinetlabs.destinycommunityhub.ui.fragment.TwitterStreamFragment;
import com.squareup.otto.Bus;
import com.squareup.otto.ThreadEnforcer;

import dagger.Module;
import dagger.Provides;


/**
 * Provides an Otto Bus to the above mentioned activites/classes/fragments
 * @author John
 *
 */
@Module(
		injects = {
				MainActivity.class,
				CommunityNewsFragment.class,
				TwitterStreamFragment.class,
				PodcastService.class
				//TODO: Add any fragments or other classes which will utilize the Bus
		},
        complete = false
)
public class OttoModule {
	@Provides
	@Singleton
	public Bus providesBus(){
		return new Bus(ThreadEnforcer.ANY);
	}
}
