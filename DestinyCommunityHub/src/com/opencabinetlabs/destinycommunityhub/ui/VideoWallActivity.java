package com.opencabinetlabs.destinycommunityhub.ui;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.util.Log;
import android.util.Property;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;

import com.etsy.android.grid.StaggeredGridView;
import com.opencabinetlabs.destinycommunityhub.CommunityHubApp;
import com.opencabinetlabs.destinycommunityhub.CommunityHubConfig;
import com.opencabinetlabs.destinycommunityhub.R;
import com.opencabinetlabs.destinycommunityhub.api.YoutubeApi;
import com.opencabinetlabs.destinycommunityhub.model.YoutubeChannelItem;
import com.opencabinetlabs.destinycommunityhub.model.YoutubeSearchResponse;
import com.opencabinetlabs.destinycommunityhub.modules.ApiModule;
import com.opencabinetlabs.destinycommunityhub.ui.adapter.VideoWallAdapter;
import com.opencabinetlabs.destinycommunityhub.ui.view.MutableForegroundColorSpan;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import javax.inject.Inject;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import timber.log.Timber;


public class VideoWallActivity extends Activity implements OnScrollListener, Callback<YoutubeSearchResponse>  {
	private final String TAG = VideoWallActivity.class.getSimpleName();

	@Inject
	YoutubeApi mYoutubeApi;

	private StaggeredGridView mStgv;
	private VideoWallAdapter mAdapter;
	private String mChannelId;

	private boolean mHasRequestedMore;

	private ArrayList<YoutubeChannelItem> mData;
	private String mNextPageToken;
	
	private View footerView;

    /**
     * Fancy ActionBar Animation
     */
    private AccelerateDecelerateInterpolator mSmoothInterpolator;
    private CharSequence mActionBarTitle;
    private SpannableString mActionBarTitleSpannableString;
    private HashSet<Object> mSpans = new HashSet<Object>();


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_staggered_videowall);

		CommunityHubApp app = (CommunityHubApp) getApplication();
		app.inject(this, new ApiModule());

		mStgv = (StaggeredGridView)findViewById(R.id.stgv);

		Intent i = getIntent();
		mChannelId = i.getStringExtra("channelId");
		String title = i.getStringExtra("title");

        mActionBarTitle = title;
        mActionBarTitleSpannableString = new SpannableString(mActionBarTitle);
        mSmoothInterpolator = new AccelerateDecelerateInterpolator();

		//getActionBar().setTitle(title);
		getActionBar().setIcon(R.drawable.action_bar_icon);	

		LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		footerView = inflater.inflate(R.layout.layout_loading_footer, null);	
		mStgv.addFooterView(footerView);

		mStgv.setOnScrollListener(this);

		mAdapter = new VideoWallAdapter(VideoWallActivity.this);

		if (mData == null) {
			Log.e(TAG, "mData is null");
			mData = new ArrayList<YoutubeChannelItem>();
			// Get the first page
			mYoutubeApi.getYoutubeChannelVideos(YoutubeApi.YOUTUBE_PART, 
					mChannelId, 
					YoutubeApi.YOUTUBE_DEFAULT_PAGE_SIZE, 
					YoutubeApi.YOUTUBE_SORT_BY, 
					YoutubeApi.YOUTUBE_DEFAULT_SEARCH_TERM, 
					CommunityHubConfig.YOUTUBE_API_KEY,
					this);
		}

		mStgv.setAdapter(mAdapter);

        // Do ActionBar Animation
        animateActionBarFireworks();


		/*mStgv.setOnLoadmoreListener(new StaggeredGridView.OnLoadmoreListener() {
			@Override
			public void onLoadmore() {
				if(mAdapter.shouldLoadMoreData()){
					mAdapter.loadMoreData();
				}else{
					mStgv.hideFooter();
				}
			}
		});*/
	}

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem,
			int visibleItemCount, int totalItemCount) {

		if (!mHasRequestedMore) {
			int lastInScreen = firstVisibleItem + visibleItemCount;
			if (lastInScreen >= totalItemCount) {
				Log.d(TAG, "onScroll lastInScreen - so load more");
				mHasRequestedMore = true;
				onLoadMoreItems();
			}
		}

	}

	private void onLoadMoreItems() {
		if(mNextPageToken != null){
			mYoutubeApi.getYoutubeChannelVideosWithPage(YoutubeApi.YOUTUBE_PART, 
					mChannelId, 
					YoutubeApi.YOUTUBE_DEFAULT_PAGE_SIZE, 
					YoutubeApi.YOUTUBE_SORT_BY, 
					mNextPageToken,
					YoutubeApi.YOUTUBE_DEFAULT_SEARCH_TERM, 
					CommunityHubConfig.YOUTUBE_API_KEY, this);
		}else{
			mStgv.removeFooterView(footerView);
		}
	}

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {

	}

	@Override
	protected void onResume() {
		super.onResume();

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_with_cancel_menu, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		
		case R.id.action_cancel:
			this.finish();
			return true;
		
		}

		return super.onOptionsItemSelected(item);
	}

	@Override
	public void failure(RetrofitError arg0) {
		Timber.e("RetrofitError "+arg0.getMessage()+"URL: "+arg0.getUrl());

	}

	@Override
	public void success(YoutubeSearchResponse response, Response arg1) {
		List<YoutubeChannelItem> items = response.getItems();
		Log.d(TAG, "ON SUCCESS - NextPage = "+ response.getNextPageToken());
		mNextPageToken = response.getNextPageToken();
		for(YoutubeChannelItem item : items){
			mAdapter.add(item);
		}
		// stash all the data in our backing store
		mData.addAll(items);

		// notify the adapter that we can update now
		mAdapter.notifyDataSetChanged();
		mHasRequestedMore = false;				
	}

    private void animateActionBarFireworks() {
        FireworksSpanGroup spanGroup = buildFireworksSpanGroup(0, mActionBarTitle.length() - 1);
        ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(spanGroup, FIREWORKS_GROUP_PROGRESS_PROPERTY, 0.0f, 1.0f);
        objectAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                //refresh
                setTitle(mActionBarTitleSpannableString);
            }
        });
        objectAnimator.setInterpolator(mSmoothInterpolator);
        objectAnimator.setDuration(2000);
        objectAnimator.start();
    }

    private static final class FireworksSpanGroup {

        private static final boolean DEBUG = false;
        private static final String TAG = "FireworksSpanGroup";

        private final float mProgress;
        private final ArrayList<MutableForegroundColorSpan> mSpans;
        private final ArrayList<Integer> mSpanIndexes;

        private FireworksSpanGroup() {
            mProgress = 0;
            mSpans = new ArrayList<MutableForegroundColorSpan>();
            mSpanIndexes = new ArrayList<Integer>();
        }

        public void addSpan(MutableForegroundColorSpan span) {
            span.setAlpha(0);
            mSpanIndexes.add(mSpans.size());
            mSpans.add(span);
        }

        public void init() {
            Collections.shuffle(mSpans);
        }

        public void setProgress(float progress) {
            int size = mSpans.size();
            float total = 1.0f * size * progress;

            if(DEBUG) Log.d(TAG, "progress " + progress + " * 1.0f * size => " + total);

            for(int index = 0 ; index < size; index++) {
                MutableForegroundColorSpan span = mSpans.get(index);

                if(total >= 1.0f) {
                    span.setAlpha(255);
                    total -= 1.0f;
                } else {
                    span.setAlpha((int) (total * 255));
                    total = 0.0f;
                }
            }
        }

        public float getProgress() {
            return mProgress;
        }
    }

    private FireworksSpanGroup buildFireworksSpanGroup(int start, int end) {
        final FireworksSpanGroup group = new FireworksSpanGroup();
        for(int index = start ; index <= end ; index++) {
            MutableForegroundColorSpan span = new MutableForegroundColorSpan(0, Color.WHITE);
            mSpans.add(span);
            group.addSpan(span);
            mActionBarTitleSpannableString.setSpan(span, index, index + 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        group.init();
        return group;
    }

    private static final Property<FireworksSpanGroup, Float> FIREWORKS_GROUP_PROGRESS_PROPERTY =
            new Property<FireworksSpanGroup, Float>(Float.class, "FIREWORKS_GROUP_PROGRESS_PROPERTY") {

                @Override
                public void set(FireworksSpanGroup spanGroup, Float value) {
                    spanGroup.setProgress(value);
                }

                @Override
                public Float get(FireworksSpanGroup spanGroup) {
                    return spanGroup.getProgress();
                }
            };

}
