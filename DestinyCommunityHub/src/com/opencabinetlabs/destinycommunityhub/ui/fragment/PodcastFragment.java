package com.opencabinetlabs.destinycommunityhub.ui.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RelativeLayout;

import com.opencabinetlabs.destinycommunityhub.CommunityHubApp;
import com.opencabinetlabs.destinycommunityhub.R;
import com.opencabinetlabs.destinycommunityhub.data.PodcastFeedItemLoader;
import com.opencabinetlabs.destinycommunityhub.model.PodcastFeedItem;
import com.opencabinetlabs.destinycommunityhub.modules.ApiModule;
import com.opencabinetlabs.destinycommunityhub.service.ApiExecutorService;
import com.opencabinetlabs.destinycommunityhub.ui.adapter.PodcastAdapter;

import java.util.ArrayList;
import java.util.List;

public class PodcastFragment extends BaseFragment implements LoaderManager.LoaderCallbacks<List<PodcastFeedItem>> {
	private final String TAG = PodcastFragment.class.getSimpleName();

	private static final String STATE_POSITION = "position";
	private static final String STATE_TOP = "top";

	private static final int LOADER_ID_PODCAST_FEED = 0x03;

	private PodcastAdapter mAdapter;

	private int mListViewStatePosition = -1;
	private int mListViewStateTop = 0;

	private ListView mListView;
    private RelativeLayout mEmptyView;
    private Button mRetryButton;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//setRetainInstance(true);

		CommunityHubApp app = (CommunityHubApp) getActivity().getApplication();
		app.inject(this, new ApiModule());
	}

	@Override
	public void onResume() {
		super.onResume();
		reload(LOADER_ID_PODCAST_FEED, this);
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
	public Loader<List<PodcastFeedItem>> onCreateLoader(final int id, final Bundle args) {
		return new PodcastFeedItemLoader(getActivity());
	}

	@Override
	public void onLoadFinished(final Loader<List<PodcastFeedItem>> loader, final List<PodcastFeedItem> data) {
		if (data == null || data.isEmpty()) {
            mListView.setVisibility(View.GONE);
            mEmptyView.setVisibility(View.VISIBLE);
            mRetryButton.setEnabled(true);
		} else {
            mListView.setVisibility(View.VISIBLE);
            mEmptyView.setVisibility(View.GONE);
			mAdapter.setPodcastFeedItems(data);
			mAdapter.notifyDataSetChanged();
		}
		if(mListViewStatePosition!=-1){
			mListView.setSelectionFromTop(mListViewStatePosition, mListViewStateTop);
		}
	}

	@Override
	public void onLoaderReset(final Loader<List<PodcastFeedItem>> loader) {
		mAdapter.setPodcastFeedItems(null);
	}


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		if (savedInstanceState != null) {
			mListViewStatePosition = savedInstanceState.getInt(STATE_POSITION, -1);
			mListViewStateTop = savedInstanceState.getInt(STATE_TOP, 0);
		} else {
			//mListViewStatePosition = -1;
			//mListViewStateTop = 0;
		}
		ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fragment_podcast, container, false);
		mListView = (ListView)rootView.findViewById(R.id.podcastListView);
        mEmptyView = (RelativeLayout) rootView.findViewById(R.id.emptyView);
        mEmptyView.setVisibility(View.GONE);
        mRetryButton = (Button) rootView.findViewById(R.id.btn_retry);
        mRetryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               mRetryButton.setEnabled(false);
               ApiExecutorService.AsyncRequest.getDispatchPodcastFeed((Context)getActivity());
               ApiExecutorService.AsyncRequest.getGuardianPodcastFeed((Context)getActivity());            }
        });
		return rootView;
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		//setEmptyText(getString(R.string.empty_list));
		// So alternative layout if view is empty

		mListView.setDividerHeight(0);
		if(mListView.getAdapter() == null) {
			mAdapter = new PodcastAdapter((Context)getActivity(), new ArrayList<PodcastFeedItem>());
			mListView.setAdapter(mAdapter);
			
		}
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		if (isAdded()) {
			View v = mListView.getChildAt(0);
			int top = (v == null) ? 0 : v.getTop();
			outState.putInt(STATE_POSITION, mListView.getFirstVisiblePosition());
			outState.putInt(STATE_TOP, top); 
		}
		super.onSaveInstanceState(outState);
	}


}
