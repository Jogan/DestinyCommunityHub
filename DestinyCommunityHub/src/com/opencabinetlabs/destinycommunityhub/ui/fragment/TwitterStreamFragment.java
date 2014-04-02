package com.opencabinetlabs.destinycommunityhub.ui.fragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.opencabinetlabs.destinycommunityhub.CommunityHubConfig;
import com.opencabinetlabs.destinycommunityhub.R;
import com.opencabinetlabs.destinycommunityhub.data.TwitterFeedItemLoader;
import com.opencabinetlabs.destinycommunityhub.model.TwitterListResult.Status;
import com.opencabinetlabs.destinycommunityhub.service.ApiExecutorService;
import com.opencabinetlabs.destinycommunityhub.ui.adapter.TweetAdapter;
import com.opencabinetlabs.destinycommunityhub.util.EndlessScrollListener;

import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;
import uk.co.senab.actionbarpulltorefresh.library.ActionBarPullToRefresh;
import uk.co.senab.actionbarpulltorefresh.library.PullToRefreshLayout;
import uk.co.senab.actionbarpulltorefresh.library.listeners.OnRefreshListener;

public class TwitterStreamFragment extends BaseFragment implements LoaderManager.LoaderCallbacks<List<Status>>, OnRefreshListener {

	private final String TAG = TwitterStreamFragment.class.getSimpleName();

	private static final String STATE_POSITION = "position";
	private static final String STATE_TOP = "top";

	private static final int LOADER_ID_TWITTER_FEED = 0x02;

	private ListView mListView;
    private RelativeLayout mEmptyView;
    private Button mRetryButton;

	private TweetAdapter mAdapter;

	private int mListViewStatePosition = -1;
	private int mListViewStateTop = 0;

	private PullToRefreshLayout mPullToRefreshLayout;

	private SharedPreferences mPrefs;

	private long mLastRequestTime;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mPrefs = getActivity().getSharedPreferences(CommunityHubConfig.PREFS_NAME, Context.MODE_PRIVATE);
		mLastRequestTime = mPrefs.getLong("lastTwitterFeedRequest", 0);
	}


	@Override
	public void onResume(){
		super.onResume();
		reload(LOADER_ID_TWITTER_FEED, this);
	}


	@Override
	public void onPause() {
		super.onPause();
		try{
			mListViewStatePosition = mListView.getFirstVisiblePosition();
			View v = mListView.getChildAt(0);
			mListViewStateTop = (v == null) ? 0 : v.getTop();
		}
		catch(Throwable t){
			t.printStackTrace();
		}

	}

	@Override
	public Loader<List<Status>> onCreateLoader(final int id, final Bundle args) {
		return new TwitterFeedItemLoader(getActivity());
	}

	@Override
	public void onLoadFinished(final Loader<List<Status>> loader, final List<Status> data) {
		if (data == null || data.isEmpty()) {
            //TODO: Hide all and show empty view
            mListView.setVisibility(View.GONE);
            mEmptyView.setVisibility(View.VISIBLE);
            mRetryButton.setEnabled(true);
		} else {
            Timber.d("onLoadFinished in TwitterStream");
            mListView.setVisibility(View.VISIBLE);
            mEmptyView.setVisibility(View.GONE);
			mAdapter.setTwitterFeedItems(data);
			mAdapter.notifyDataSetChanged();
			// Notify PullToRefreshLayout that the refresh has finished
			mPullToRefreshLayout.setRefreshComplete();
		}
		if(mListViewStatePosition!=-1){
			mListView.setSelectionFromTop(mListViewStatePosition, mListViewStateTop);
		}
	}

	@Override
	public void onLoaderReset(final Loader<List<Status>> loader) {
		//mAdapter.setNewsFeedItems(null);
		mAdapter.setTwitterFeedItems(null);
	}


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		if (savedInstanceState != null) {
			Timber.d("SAVED INSTANCE POSTION = "+mListViewStatePosition);
			Timber.d("SAVED INSTANCE TOP POSTION = "+mListViewStateTop);

			mListViewStatePosition = savedInstanceState.getInt(STATE_POSITION, -1);
			mListViewStateTop = savedInstanceState.getInt(STATE_TOP, 0);
		} else {
			//mListViewStatePosition = -1;
			//mListViewStateTop = 0;
		}
		ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fragment_twitter_stream, container, false);
		mListView = (ListView)rootView.findViewById(R.id.twitterFeedListView);
        mEmptyView = (RelativeLayout) rootView.findViewById(R.id.emptyView);
        mEmptyView.setVisibility(View.GONE);
        mRetryButton = (Button) rootView.findViewById(R.id.btn_retry);
        mRetryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mRetryButton.setEnabled(false);

                    ApiExecutorService.AsyncRequest.getTwitterFeed((Context)getActivity());

                mPullToRefreshLayout.setRefreshing(true);
            }
        });
        return rootView;
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view,savedInstanceState);
		ViewGroup viewGroup = (ViewGroup) view;

		// As we're using a ListFragment we create a PullToRefreshLayout manually
		mPullToRefreshLayout = new PullToRefreshLayout(viewGroup.getContext());

		// We can now setup the PullToRefreshLayout
		ActionBarPullToRefresh.from(getActivity())
		// We need to insert the PullToRefreshLayout into the Fragment's ViewGroup
		.insertLayoutInto(viewGroup)
		// Here we mark just the ListView and it's Empty View as pullable
		.theseChildrenArePullable(R.id.twitterFeedListView, android.R.id.empty)
		.listener(this)
		.setup(mPullToRefreshLayout);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		//setEmptyText(getString(R.string.empty_list));
		// So alternative layout if view is empty

		if(mListView.getAdapter() == null) {
			mAdapter = new TweetAdapter((Context)getActivity(), new ArrayList<Status>());
			mListView.setAdapter(mAdapter);
			//EndlessScrollListener listener = new EndlessScrollListener();
			//listener.setOnEndReachedListener(mAdapter);
			//mListView.setOnScrollListener(listener);
			/*if (mListViewStatePosition != -1 && isAdded()) {
	            mListView.setSelectionFromTop(mListViewStatePosition, mListViewStateTop);
	            mListViewStatePosition = -1;
	        }*/
		}
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		if (isAdded()) {
			View v = mListView.getChildAt(0);
			int top = (v == null) ? 0 : v.getTop();
			Timber.d("POSTION = "+mListView.getFirstVisiblePosition());
			Timber.d("TOP POSTION = "+top);
			outState.putInt(STATE_POSITION, mListView.getFirstVisiblePosition());
			outState.putInt(STATE_TOP, top); 
		}
		super.onSaveInstanceState(outState);
	}

	@Override
	public void onRefreshStarted(View view) {
		//TODO: Limit the refresh so there are not continuous calls to server
		mListViewStatePosition = -1;
		mListViewStateTop = 0;
			long timeRequested = System.currentTimeMillis();
			if(((timeRequested - mLastRequestTime) >= CommunityHubConfig.API_DELAY) || mAdapter.getCount() == 0){
				Log.d(TAG, "Made twitter feed request via pull to refresh");
				ApiExecutorService.AsyncRequest.getTwitterFeed((Context)getActivity());
				SharedPreferences.Editor editor = mPrefs.edit();
				mLastRequestTime = timeRequested;
				editor.putLong("lastTwitterFeedRequest", mLastRequestTime).commit();
			}else{
				Log.d(TAG, "NO twitter feed request made via pull to refresh");
				mPullToRefreshLayout.setRefreshComplete();
				Toast.makeText(getActivity(), "There are no new items.", Toast.LENGTH_LONG).show();
			}

	}

}
