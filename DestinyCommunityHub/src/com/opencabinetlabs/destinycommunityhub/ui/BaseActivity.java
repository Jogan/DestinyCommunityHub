package com.opencabinetlabs.destinycommunityhub.ui;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.Window;

import com.opencabinetlabs.destinycommunityhub.CommunityHubApp;
import com.opencabinetlabs.destinycommunityhub.receiver.ApiBroadcastReceiver;
import com.squareup.otto.Bus;

import javax.inject.Inject;

import static com.opencabinetlabs.destinycommunityhub.service.ApiExecutorService.AsyncRequest;

/**
 * Base class for all activities in the app
 */
public class BaseActivity extends FragmentActivity {
    
	@Inject
    protected Bus mEventBus;

    protected CommunityHubApp mApp;

    /**
     * Catches API-related broadcasts
     */
    private ApiBroadcastReceiver mApiReceiver = new ApiBroadcastReceiver() {
        @Override
        protected void onStart(final String token) {
            setProgressBarIndeterminateVisibility(getRunningCounter() > 0);
            onApiRequestStart(token);
        }

        @Override
        protected void onFinish(final String token) {
            setProgressBarIndeterminateVisibility(getRunningCounter() > 0);
            onApiRequestFinish(token);
        }

        @Override
        protected void onError(final String token, final String errorMsg) {
            onApiRequestError(token, errorMsg);
        }
    };

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);

        mApp = (CommunityHubApp) getApplication();
        mApp.inject(this);
        
    }

    @Override
    protected void onResume() {
        super.onResume();
        mApiReceiver.register(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mApiReceiver.unregister(this);
    }

    protected void onApiRequestStart(String action) {
    }

    protected void onApiRequestFinish(String action) {
    }

    protected void onApiRequestError(String action, String errorMsg) {
    }

    /**
     * Listen out for API broadcasts of type <code>token</code>
     *
     * @param token The token returned from a method in {@link AsyncRequest}
     */
    protected void registerForApi(String token) {
        mApiReceiver.addAcceptableToken(token);
    }

    /**
     * @param id  The id of the fragment to retrieve
     * @param <T> A {@link Fragment} subclass
     * @return The fragment with id <code>id</code>, or null if it doesn't exist
     */
    protected <T extends Fragment> T findFragment(int id) {
        return (T) getSupportFragmentManager().findFragmentById(id);
    }
}
