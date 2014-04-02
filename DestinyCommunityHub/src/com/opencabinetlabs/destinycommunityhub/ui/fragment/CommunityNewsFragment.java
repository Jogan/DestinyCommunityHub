package com.opencabinetlabs.destinycommunityhub.ui.fragment;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.opencabinetlabs.destinycommunityhub.CommunityHubConfig;
import com.opencabinetlabs.destinycommunityhub.R;
import com.opencabinetlabs.destinycommunityhub.api.CommunityRssApi;
import com.opencabinetlabs.destinycommunityhub.data.CommunityNewsFeedItemLoader;
import com.opencabinetlabs.destinycommunityhub.model.NewsFeedItem;
import com.opencabinetlabs.destinycommunityhub.service.ApiExecutorService;
import com.opencabinetlabs.destinycommunityhub.ui.NewsWebViewActivity;
import com.opencabinetlabs.destinycommunityhub.ui.adapter.CommunityNewsFeedArrayAdapter;
import com.opencabinetlabs.destinycommunityhub.ui.adapter.CommunityNewsFeedArrayAdapter.ViewHolder;

import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;
import uk.co.senab.actionbarpulltorefresh.library.ActionBarPullToRefresh;
import uk.co.senab.actionbarpulltorefresh.library.PullToRefreshLayout;
import uk.co.senab.actionbarpulltorefresh.library.listeners.OnRefreshListener;

public class CommunityNewsFragment extends BaseFragment implements LoaderManager.LoaderCallbacks<List<NewsFeedItem>>, OnRefreshListener {

	private static final String STATE_POSITION = "position";
	private static final String STATE_TOP = "top";
    public static float sAnimatorScale = 1;

    private static final int LOADER_ID_COMMUNITY_NEWS_FEED = 0x01;

	private ListView mListView;
    private RelativeLayout mEmptyView;
    private Button mRetryButton;

    private CommunityNewsFeedArrayAdapter mAdapter;

	private int mListViewStatePosition = -1;
	private int mListViewStateTop = 0;
	private final String TAG = CommunityNewsFragment.class.getSimpleName();

	private PullToRefreshLayout mPullToRefreshLayout;

	/*
	 * Shared Preferences
	 */
	private SharedPreferences mPrefs;
	private long mLastRequestTime;


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//setRetainInstance(true);
		mPrefs = getActivity().getSharedPreferences(CommunityHubConfig.PREFS_NAME, Context.MODE_PRIVATE);
		mLastRequestTime = mPrefs.getLong("lastNewsFeedRequest", 0);

	}

	@Override
	public void onResume() {
		super.onResume();
		reload(LOADER_ID_COMMUNITY_NEWS_FEED, this);
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
	public Loader<List<NewsFeedItem>> onCreateLoader(final int id, final Bundle args) {
		return new CommunityNewsFeedItemLoader(getActivity());
	}

	@Override
	public void onLoadFinished(final Loader<List<NewsFeedItem>> loader, final List<NewsFeedItem> data) {
		if (data == null || data.isEmpty()) {
            //TODO: Hide all and show empty view
            mListView.setVisibility(View.GONE);
            mEmptyView.setVisibility(View.VISIBLE);
            mRetryButton.setEnabled(true);
        } else {
            mListView.setVisibility(View.VISIBLE);
            mEmptyView.setVisibility(View.GONE);
			Timber.d("onLoadFinished");
			mAdapter.setNewsFeedItems(data);
			mAdapter.notifyDataSetChanged();
			// Notify PullToRefreshLayout that the refresh has finished
			mPullToRefreshLayout.setRefreshComplete();
		}
		if(mListViewStatePosition!=-1){
			mListView.setSelectionFromTop(mListViewStatePosition, mListViewStateTop);
		}
	}

	@Override
	public void onLoaderReset(final Loader<List<NewsFeedItem>> loader) {
		mAdapter.setNewsFeedItems(null);
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
		ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fragment_news, container, false);
		mListView = (ListView)rootView.findViewById(R.id.newsListView);
        mListView.setVisibility(View.VISIBLE);
        mEmptyView = (RelativeLayout) rootView.findViewById(R.id.emptyView);
        mEmptyView.setVisibility(View.GONE);
        mRetryButton = (Button) rootView.findViewById(R.id.btn_retry);
        mRetryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mRetryButton.setEnabled(false);
                ApiExecutorService.AsyncRequest.getCommunityNewsFeed((Context)getActivity(), CommunityRssApi.INITIAL_PAGE); //Request first page for most recent
                mPullToRefreshLayout.setRefreshing(true);
            }
        });
		return rootView;
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view,savedInstanceState);
		ViewGroup viewGroup = (ViewGroup) view;

		mPullToRefreshLayout = new PullToRefreshLayout(viewGroup.getContext());

		ActionBarPullToRefresh.from(getActivity())
		// We need to insert the PullToRefreshLayout into the Fragment's ViewGroup
		.insertLayoutInto(viewGroup)
		// Here we mark just the ListView and it's Empty View as pullable
		.theseChildrenArePullable(R.id.newsListView, android.R.id.empty)
		.listener(this)
		.setup(mPullToRefreshLayout);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		mListView.setDividerHeight(0);
		if(mListView.getAdapter() == null) {
			mAdapter = new CommunityNewsFeedArrayAdapter((Context)getActivity(), new ArrayList<NewsFeedItem>());
			mListView.setAdapter(mAdapter);
			//EndlessScrollListener listener = new EndlessScrollListener();
			//listener.setOnEndReachedListener(mAdapter);
			//mListView.setOnScrollListener(listener);
			/*if (mListViewStatePosition != -1 && isAdded()) {
	            mListView.setSelectionFromTop(mListViewStatePosition, mListViewStateTop);
	            mListViewStatePosition = -1;
	        }*/
		}


		mListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position,
					long id) {
				ViewHolder holder = (ViewHolder)view.getTag();
				Log.v(TAG, "Go to url " + holder.destinationUrl);
				if(holder.destinationUrl != null){

                    int[] screenLocation = new int[2];
                    view.getLocationOnScreen(screenLocation);
					Intent intent = new Intent(getActivity(), NewsWebViewActivity.class);
                    int orientation = getResources().getConfiguration().orientation;
                    intent.putExtra("orientation", orientation)
                            .putExtra("left",screenLocation[0])
                            .putExtra("top",screenLocation[1])
                            .putExtra("width", view.getWidth())
                            .putExtra("height",view.getHeight())
                            .putExtra("siteTitle",holder.siteTitle)
                            .putExtra("url", holder.destinationUrl);
					getActivity().startActivity(intent);

                    //Override transistions
                    //getActivity().overridePendingTransition(0,0);
				}

			}
		});
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
			Log.d(TAG, "Made news feed request via pull to refresh");
			ApiExecutorService.AsyncRequest.getCommunityNewsFeed((Context)getActivity(), CommunityRssApi.INITIAL_PAGE); //Request first page for most recent 
			SharedPreferences.Editor editor = mPrefs.edit();
			mLastRequestTime = timeRequested;
			editor.putLong("lastNewsFeedRequest", mLastRequestTime).commit();
		}else{
			Log.d(TAG, "NO news feed request made via pull to refresh");
			mPullToRefreshLayout.setRefreshComplete();
			Toast.makeText(getActivity(), "There are no new items.", Toast.LENGTH_LONG).show();
		}
	}




}

