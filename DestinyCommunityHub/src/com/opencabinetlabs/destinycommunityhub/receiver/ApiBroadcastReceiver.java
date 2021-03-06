package com.opencabinetlabs.destinycommunityhub.receiver;

import java.util.HashSet;
import java.util.Set;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;

import com.opencabinetlabs.destinycommunityhub.R;
import com.opencabinetlabs.destinycommunityhub.service.ApiExecutorService;

/**
 * Catches broadcasts sent at different lifecycle events
 * of Api requests as executed by {@link ApiExecutorService}
 */
public abstract class ApiBroadcastReceiver extends BroadcastReceiver {

    private Set<String> mAcceptableTokens = new HashSet<String>();

    /**
     * Called when a new API request has started
     *
     * @param token The kind of request being executed
     */
    protected abstract void onStart(String token);

    /**
     * Called when an API request has finished
     *
     * @param token The kind of request that finished
     */
    protected abstract void onFinish(String token);

    /**
     * Called when there is an error with an API request
     *
     * @param token    The kind of request which caused an error
     * @param errorMsg The human-readable error message representing the problem
     */
    protected abstract void onError(String token, String errorMsg);

    /**
     * Start listening for API events
     *
     * @param context
     */
    public void register(Context context) {
        final IntentFilter filter = new IntentFilter();
        filter.addAction(ApiExecutorService.ACTION_API_START);
        filter.addAction(ApiExecutorService.ACTION_API_FINISH);
        filter.addAction(ApiExecutorService.ACTION_API_ERROR);
        LocalBroadcastManager.getInstance(context).registerReceiver(this, filter);
    }

    /**
     * Cease listening to API events
     *
     * @param context
     */
    public void unregister(Context context) {
        LocalBroadcastManager.getInstance(context).unregisterReceiver(this);
    }

    /**
     * Accept broadcasts of type <code>token</code>
     *
     * @param token The kind of broadcast to accept
     */
    public void addAcceptableToken(String token) {
        mAcceptableTokens.add(token);
    }

    /**
     * Reference counter for the number of requests currently executing
     */
    private int mRunningCounter = 0;

    @Override
    public void onReceive(Context context, Intent intent) {
        final String action = intent.getAction();
        final String token = intent.getStringExtra(ApiExecutorService.EXTRA_TOKEN);
        if (action != null && token != null && mAcceptableTokens.contains(token)) {
           if(action.equalsIgnoreCase(ApiExecutorService.ACTION_API_START)){
                    mRunningCounter++;
                    onStart(action);
           }else if(action.equalsIgnoreCase(ApiExecutorService.ACTION_API_FINISH)){
                    mRunningCounter--;
                    if (mRunningCounter < 0) {
                        mRunningCounter = 0;
                    }
                    onFinish(action);
           }else{
        	   String errorMsg = intent.getStringExtra(ApiExecutorService.EXTRA_ERROR_MESSAGE);
               if (TextUtils.isEmpty(errorMsg)) {
                   errorMsg = context.getString(R.string.unknown_error);
               }

               onError(action, errorMsg); 
           }                               
        }
    }

    /**
     * @return The number of currently executing requests
     */
    protected int getRunningCounter() {
        return mRunningCounter;
    }
}
