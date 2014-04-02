package com.opencabinetlabs.destinycommunityhub.ui.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;

import com.opencabinetlabs.destinycommunityhub.CommunityHubApp;
import com.squareup.otto.Bus;

import javax.inject.Inject;

/**
 * All fragments should extend this for dependency injection.
 */
public class BaseFragment extends Fragment {

	@Inject
    protected Bus mEventBus;

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final CommunityHubApp app = (CommunityHubApp) getActivity().getApplication();
        app.inject(this);
    }

    /**
     * Reload the data from a loader
     *
     * @param loaderId  The id of the loader to reload
     * @param callbacks The callback to be invoked by the underlying loader
     */
    protected void reload(int loaderId, LoaderManager.LoaderCallbacks callbacks) {
        final Loader loader = getLoaderManager().getLoader(loaderId);
        if (loader == null) {
            getLoaderManager().initLoader(loaderId, null, callbacks);
        } else {
            getLoaderManager().restartLoader(loaderId, null, callbacks);
        }
    }

}
