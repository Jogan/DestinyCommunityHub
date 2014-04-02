package com.opencabinetlabs.destinycommunityhub.modules;

import com.opencabinetlabs.destinycommunityhub.CommunityHubApp;

import dagger.Module;
import dagger.Provides;

/**
 * Provides injection of our global {@link android.app.Application} object
 */
@Module(
        library = true
)
public class AppModule {
	private CommunityHubApp mApp;

    public AppModule(CommunityHubApp app) {
        mApp = app;
    }

    @Provides
    public CommunityHubApp providesApp() {
        return mApp;
    }

}
