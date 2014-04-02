package com.opencabinetlabs.destinycommunityhub.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.PagerTabStrip;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.text.Html;
import android.text.Spanned;
import android.text.util.Linkify;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.crashlytics.android.Crashlytics;
import com.opencabinetlabs.destinycommunityhub.CommunityHubConfig;
import com.opencabinetlabs.destinycommunityhub.R;
import com.opencabinetlabs.destinycommunityhub.api.CommunityRssApi;
import com.opencabinetlabs.destinycommunityhub.event.BufferingUpdateEvent;
import com.opencabinetlabs.destinycommunityhub.event.CurrentlyPlayingEvent;
import com.opencabinetlabs.destinycommunityhub.event.MediaPreparedEvent;
import com.opencabinetlabs.destinycommunityhub.event.PlaybackEvent;
import com.opencabinetlabs.destinycommunityhub.event.SeekBarUpdateEvent;
import com.opencabinetlabs.destinycommunityhub.model.PodcastFeedItem;
import com.opencabinetlabs.destinycommunityhub.service.ApiExecutorService;
import com.opencabinetlabs.destinycommunityhub.service.mediaplayer.PodcastService;
import com.opencabinetlabs.destinycommunityhub.ui.dialog.LicenseDialog;
import com.opencabinetlabs.destinycommunityhub.ui.fragment.CommunityFragment;
import com.opencabinetlabs.destinycommunityhub.ui.fragment.CommunityNewsFragment;
import com.opencabinetlabs.destinycommunityhub.ui.fragment.PodcastFragment;
import com.opencabinetlabs.destinycommunityhub.ui.fragment.TwitterStreamFragment;
import com.opencabinetlabs.destinycommunityhub.ui.fragment.VideosFragment;
import com.opencabinetlabs.destinycommunityhub.ui.view.DepthPageTransformer;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;
import com.sothree.slidinguppanel.SlidingUpPanelLayout.PanelSlideListener;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import fr.nicolaspomepuy.discreetapprate.AppRate;
import fr.nicolaspomepuy.discreetapprate.RetryPolicy;
import timber.log.Timber;

public class MainActivity extends BaseActivity implements PodcastListener {

	private final static String TAG = MainActivity.class.getSimpleName();

	public static final String SAVED_STATE_ACTION_BAR_HIDDEN = "saved_state_action_bar_hidden";
	public static final String KEY_CURRENTLY_PLAYING_BUNDLE = "currently_playing_bundle";
	public static final String KEY_HAS_MADE_COMMUNITY_NEWS_FEED_REQUEST = "has_made_community_news_request";
	public static final String KEY_HAS_MADE_TWITTER_FEED_REQUEST = "has_made_twitter_feed_request";
	public static final String KEY_HAS_MADE_PODCAST_FEED_REQUEST = "has_made_podcast_feed_request";

	private static final String DIALOG_LICENSE = "com.opencabinetlabs.destinycomhub.ui.MainActivity.licenseDialog";

    /**
	 * Holds number of fragments
	 */
	private int mNumFragments;

	/**
	 * The pager widget, which handles animation and allows swiping horizontally to access previous
	 * and next wizard steps.
	 */
	private ViewPager mPager;

	/**
	 * Broadcast receiver to receive events from service
	 */
	private BroadcastReceiver broadcastReceiver;

	/**
	 * Fragments
	 */
	private CommunityNewsFragment mCommunityNewsFragment;
	private VideosFragment mVideosFragment;
	private CommunityFragment mCommunityFragment;
	private TwitterStreamFragment mTwitterStreamFragment;
	private PodcastFragment mPodcastFragment;

    private FrameLayout webpageContainer;

	/*
	 * Currently Playing Bundle info
	 */
	private Bundle currentlyPlayingBundle;

	/*
	 * Shared Preferences
	 */
	private SharedPreferences mPrefs;

	/**
	 * Tracks weather or not we have made our network request for new community news feed items
	 */
	private boolean mHasMadeCommunityNewsRequest;
	private boolean mHasMadeTwitterFeedRequest;
	private boolean mHasMadePodcastsRequest;
	private long mLastTimeApiRequestsMade;

	// Array of strings storing names
	String[] mNavDrawerLinks ;

	// Array of integers points to images stored in /res/drawables
	int[] mIcons = new int[]{
			R.drawable.ic_action_news,
			R.drawable.ic_action_youtube,
			R.drawable.ic_action_soundcloud,
			R.drawable.ic_action_users,
			R.drawable.ic_action_list_2
	};

	// Drawer setup
	private DrawerLayout mDrawerLayout;
	private ListView mDrawerList;
	private ActionBarDrawerToggle mDrawerToggle;
	private LinearLayout mDrawer ;
	private List<HashMap<String,String>> mList ;
	private SimpleAdapter mAdapter;
	final private String TITLE = "title";
	final private String ICON = "icon";

	// Sliding Panel setup
	private SlidingUpPanelLayout mSlidingLayout;
	private LinearLayout mDraggableView;
	private LinearLayout mPodcastPanel;
	private ImageView mPodcastArtwork;
	private TextView mPodcastTitle;
	private TextView mPodcastArtist;
	private ImageButton mPlayPauseButton;
	private ImageButton mStopButton;
	private SeekBar mSeeker;
	private TextView mCurrentTime;
	private TextView mTotalDuration;
	// Sliding Panel Expanded setup;
	private ImageView mExpPodcastArtwork;
	private TextView mExpPodcastTitle;
	private TextView mExpPodcastArtist;
	private TextView mExpPodcastDescription;

    //Mopub
    //private MoPubView moPubView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Crashlytics.start(this);
		getWindow().requestFeature(Window.FEATURE_ACTION_BAR_OVERLAY);

		if (isFinishing()) {
			return;
		}

		setContentView(R.layout.activity_main);

		bindViews();

		// Enabling Up navigation
		getActionBar().setDisplayHomeAsUpEnabled(true);
		getActionBar().setDisplayShowHomeEnabled(true);
		getActionBar().setIcon(R.drawable.action_bar_icon);

		if(savedInstanceState != null){
			mCommunityNewsFragment = (CommunityNewsFragment) getSupportFragmentManager()
					.getFragment(savedInstanceState, CommunityNewsFragment.class.getName());
			mVideosFragment = (VideosFragment) getSupportFragmentManager()
					.getFragment(savedInstanceState, VideosFragment.class.getName());
			mCommunityFragment = (CommunityFragment) getSupportFragmentManager()
					.getFragment(savedInstanceState, CommunityFragment.class.getName());
			mTwitterStreamFragment = (TwitterStreamFragment) getSupportFragmentManager()
					.getFragment(savedInstanceState, TwitterStreamFragment.class.getName());
			mPodcastFragment = (PodcastFragment) getSupportFragmentManager()
					.getFragment(savedInstanceState, PodcastFragment.class.getName());
		}
		if (mCommunityNewsFragment == null) {
			mCommunityNewsFragment = new CommunityNewsFragment();
		}
		if (mVideosFragment == null) {
			mVideosFragment = new VideosFragment();		
		}
		if(mCommunityFragment == null){
			mCommunityFragment = new CommunityFragment();
		}
		if(mTwitterStreamFragment == null){
			mTwitterStreamFragment = new TwitterStreamFragment();
		}
		if(mPodcastFragment == null){
			mPodcastFragment = new PodcastFragment();
		}

		//Initialize sharedPrefs
		mPrefs = getSharedPreferences(CommunityHubConfig.PREFS_NAME, Context.MODE_PRIVATE);

		mLastTimeApiRequestsMade = mPrefs.getLong("lastTimeApiRequestsMade", 0);

		// Instantiate a ViewPager and a PagerAdapter.
		mPager = (ViewPager) findViewById(R.id.pager);


        HomePagerAdapter mPagerAdapter = new HomePagerAdapter(getSupportFragmentManager());
        mPager.setAdapter(mPagerAdapter);
        mNumFragments = mPagerAdapter.getCount();

		mPager.setPageTransformer(true, new DepthPageTransformer());

		//mNumFragments = mPagerAdapter.getCount();

		// Getting an array of link names
		mNavDrawerLinks = getResources().getStringArray(R.array.navdrawer_links);

		// Title of the activity
		// = (String)getTitle();

		// Getting a reference to the drawer listview
		mDrawerList = (ListView) findViewById(R.id.drawer_list);
		final TypedArray styledAttributes = this.getTheme().obtainStyledAttributes(
				new int[] { android.R.attr.actionBarSize });
		int mActionBarSize = (int) styledAttributes.getDimension(0, 0);
		styledAttributes.recycle();
		mDrawerList.setPadding(0, mActionBarSize, 0, 0);

		// Getting a reference to the sidebar drawer ( Title + ListView )
		mDrawer = ( LinearLayout) findViewById(R.id.drawer);

		mList = new ArrayList<HashMap<String,String>>();
		for(int i=0;i < 5;i++){
			HashMap<String, String> hm = new HashMap<String,String>();
			hm.put(TITLE, mNavDrawerLinks[i]);
			hm.put(ICON, Integer.toString(mIcons[i]) );
			mList.add(hm);
		}

		// Keys used in Hashmap
		String[] from = { ICON,TITLE };

		// Ids of views in listview_layout
		int[] to = { R.id.icon , R.id.title};

		// Instantiating an adapter to store each items
		// R.layout.drawer_layout defines the layout of each item
		mAdapter = new SimpleAdapter(this, mList, R.layout.drawer_layout, from, to){

			@Override 
			public View getView(int position, View convertView, ViewGroup parent){
				View view = super.getView(position, convertView, parent);

				TextView textView = (TextView) view.findViewById(R.id.title);
				textView.setTextColor(Color.WHITE);

				return view;
			}
		};

		mDrawerLayout = (DrawerLayout)findViewById(R.id.drawer_layout);

		// Creating a ToggleButton for NavigationDrawer with drawer event listener
		mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.drawable.ic_drawer , R.string.drawer_open, R.string.drawer_close){

			/** Called when drawer is closed */
			public void onDrawerClosed(View view) {
				getActionBar().setTitle(R.string.app_name);
				supportInvalidateOptionsMenu();
			}

			/** Called when a drawer is opened */
			public void onDrawerOpened(View drawerView) {
				getActionBar().setTitle(R.string.title_nav_opened);
				supportInvalidateOptionsMenu();
			}
		};

		// Setting event listener for the drawer
		mDrawerLayout.setDrawerListener(mDrawerToggle);

		// ItemClick event handler for the drawer items
		mDrawerList.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int position,
					long arg3) {

				// Increment hit count of the drawer list item
				//incrementHitCount(position);

				if(position <= mNumFragments) { // Show fragment
					showFragment(position);
				}

				// Closing the drawer
				mDrawerLayout.closeDrawer(mDrawer);
			}
		});

		// Setting the adapter to the listView
		mDrawerList.setAdapter(mAdapter);

		PagerTabStrip strip = (PagerTabStrip)findViewById(R.id.pagertitlestrip);
		strip.setDrawFullUnderline(true);
		strip.setTabIndicatorColor(Color.WHITE);
		strip.setNonPrimaryAlpha(0.5f);
		strip.setTextSpacing(25);
		strip.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);

		/* Sliding Panel Layout Setup */
		mSlidingLayout.setShadowDrawable(getResources().getDrawable(R.drawable.above_shadow));
		//mSlidingLayout.setAnchorPoint(0.3f);
		mSlidingLayout.setDragView(mDraggableView);
		mSlidingLayout.setPanelSlideListener(new PanelSlideListener() {

			@Override
			public void onPanelSlide(View panel, float slideOffset) {
				if (slideOffset < 0.2) {
					if (getActionBar().isShowing()) {
						getActionBar().hide();
					}
				} else {
					if (!getActionBar().isShowing()) {
						getActionBar().show();
					}
				}
			}

			@Override
			public void onPanelExpanded(View panel) {


			}

			@Override
			public void onPanelCollapsed(View panel) {


			}

			@Override
			public void onPanelAnchored(View panel) {


			}
		});

		//Initially hide the podcast panel
		mPodcastPanel.setVisibility(View.GONE);

		mHasMadeCommunityNewsRequest = savedInstanceState == null ?
				false : savedInstanceState.getBoolean(KEY_HAS_MADE_COMMUNITY_NEWS_FEED_REQUEST, false);
		mHasMadeTwitterFeedRequest = savedInstanceState == null ?
				false : savedInstanceState.getBoolean(KEY_HAS_MADE_TWITTER_FEED_REQUEST, false);
		mHasMadePodcastsRequest = savedInstanceState == null ?
				false : savedInstanceState.getBoolean(KEY_HAS_MADE_PODCAST_FEED_REQUEST, false);

		boolean actionBarHidden = savedInstanceState != null ?
				savedInstanceState.getBoolean(SAVED_STATE_ACTION_BAR_HIDDEN, false): false;
				if (actionBarHidden) {
					getActionBar().hide();
				}
    }

	private void bindViews() {
		mSlidingLayout = (SlidingUpPanelLayout) findViewById(R.id.sliding_up_panel_root_layout);
		mDraggableView = (LinearLayout) findViewById(R.id.podcastInfoLayout);
		mPodcastPanel = (LinearLayout) findViewById(R.id.sliding_panel_layout);
		mPodcastArtwork = (ImageView) findViewById(R.id.podcastArtwork);
		mPodcastTitle = (TextView) findViewById(R.id.podcastTitleTextView);
		mPodcastArtist = (TextView) findViewById(R.id.podcastArtistTextView);
		mPlayPauseButton = (ImageButton) findViewById(R.id.playPauseButton);
		mPlayPauseButton.setOnClickListener(playPausePodcastOnClickListener);
		mStopButton = (ImageButton) findViewById(R.id.stopButton);
		mStopButton.setOnClickListener(stopPodcastOnClickListener);
		mSeeker = (SeekBar) findViewById(R.id.podcastSeekbar);
		mSeeker.setOnSeekBarChangeListener(new TimeChangedListener());
		mCurrentTime = (TextView) findViewById(R.id.textCurrentTime);
		mTotalDuration = (TextView) findViewById(R.id.textTotalDuration);

		mExpPodcastArtwork = (ImageView) findViewById(R.id.exp_podcastArtwork);
		mExpPodcastTitle = (TextView) findViewById(R.id.exp_podcastTitleTextView);
		mExpPodcastArtist= (TextView) findViewById(R.id.exp_podcastArtistTextView);
		mExpPodcastDescription= (TextView) findViewById(R.id.exp_podcastDescriptionTextView);

        webpageContainer = (FrameLayout) findViewById(R.id.webpageContainer);

    }

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		mDrawerToggle.syncState();

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (mDrawerToggle.onOptionsItemSelected(item)) {
			return true;
		}
		switch (item.getItemId()) {
		case R.id.action_licenses:
			new LicenseDialog().show(getSupportFragmentManager(), DIALOG_LICENSE);
			return true;
		case R.id.action_feedback:
			Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts(
					"mailto","destinycommunityhub1@gmail.com", null));
			emailIntent.putExtra(Intent.EXTRA_SUBJECT, getResources().getString(R.string.feedback_subject));
			startActivity(Intent.createChooser(emailIntent, "Send feedback..."));
			return true;
		}

		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onResume() {
		super.onResume();
		mEventBus.register(this);

		long timeRequested = System.currentTimeMillis();
		if((timeRequested - mLastTimeApiRequestsMade) >= CommunityHubConfig.API_DELAY){
			Log.d(TAG, "MADE REQUESTS");
			if (!mHasMadeCommunityNewsRequest) {
				registerForApi(ApiExecutorService.AsyncRequest.getCommunityNewsFeed(this, CommunityRssApi.INITIAL_PAGE));
                mHasMadeCommunityNewsRequest = true;
			}
			if(!mHasMadeTwitterFeedRequest){
				registerForApi(ApiExecutorService.AsyncRequest.getTwitterFeed(this));
				mHasMadeTwitterFeedRequest = true;
			}
			if (!mHasMadePodcastsRequest) {
				registerForApi(ApiExecutorService.AsyncRequest.getDispatchPodcastFeed(this));
				registerForApi(ApiExecutorService.AsyncRequest.getGuardianPodcastFeed(this));
				mHasMadePodcastsRequest = true;
			}
			SharedPreferences.Editor editor = mPrefs.edit();
			mLastTimeApiRequestsMade = timeRequested;
			editor.putLong("lastTimeApiRequestsMade", mLastTimeApiRequestsMade).commit();
		}else{
			Log.d(TAG, "NO REQUESTS MADE");
		}
		

		// Podcast fix if user presses stop from notification bar
		if((PodcastService.getMusicServiceState() == PodcastService.State.Stopped)){
			mPodcastPanel.setVisibility(View.GONE);
		}else if(PodcastService.getMusicServiceState() == PodcastService.State.Playing){
			mPodcastPanel.setVisibility(View.VISIBLE);
		}
		
		mEventBus.post(new CurrentlyPlayingEvent());

	}

    @Override
	protected void onPause() {
		super.onPause();
		mEventBus.unregister(this);
	}

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		outState.putBoolean(KEY_HAS_MADE_COMMUNITY_NEWS_FEED_REQUEST, mHasMadeCommunityNewsRequest);
		outState.putBoolean(KEY_HAS_MADE_TWITTER_FEED_REQUEST, mHasMadeTwitterFeedRequest);
		outState.putBoolean(KEY_HAS_MADE_PODCAST_FEED_REQUEST, mHasMadePodcastsRequest);

		outState.putBoolean(SAVED_STATE_ACTION_BAR_HIDDEN, !getActionBar().isShowing());

	}

	private Bundle getCurrentPlayingBundle() {
		return this.currentlyPlayingBundle;
	}

	private class HomePagerAdapter extends FragmentPagerAdapter {
		private final String[] titles = { "NEWS", "VIDEOS", "PODCASTS", "COMMUNITY", "STREAM"};

		public HomePagerAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public Fragment getItem(int position) {
			switch (position) {
			case 0:
				return mCommunityNewsFragment;
			case 1:
				return mVideosFragment;
			case 2:
				return mPodcastFragment;
			case 3:
				return mCommunityFragment;
			case 4:
				return mTwitterStreamFragment;
			}
			return null;
		}

		@Override
		public int getCount() {
			return 5;
		}

		@Override
		public CharSequence getPageTitle (int position) {		 
			return titles[position];
		}
	}



	public void showFragment(int position){
        mPager.setCurrentItem(position,true);
	}

	/*
	 * Methods pertaining to the podcast player
	 */
	private class TimeChangedListener implements SeekBar.OnSeekBarChangeListener {
		private Timer delayedSeekTimer;

		public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
			if( fromUser ) {
				Log.v(TAG,"TimeLineChangeListener progress received from user: "+progress);

				scheduleSeek(progress);

				return;
			}
		}

		private void scheduleSeek(final int  progress) {
			if( delayedSeekTimer != null) {
				delayedSeekTimer.cancel();
			}
			delayedSeekTimer = new Timer();
			delayedSeekTimer.schedule(new TimerTask() {

				@Override
				public void run() {
					Intent i = new Intent(PodcastService.ACTION_SEEK);
					i.putExtra("seekProgress", progress);
					startService(i);
				}
			}, 200);
		}

		public void onStartTrackingTouch(SeekBar seekBar) {
		}

		public void onStopTrackingTouch(SeekBar seekBar) {
		}

	}

	private OnClickListener playPausePodcastOnClickListener = new OnClickListener(){

		@Override
		public void onClick(View v) {
			if (PodcastService.getMusicServiceState() == PodcastService.State.Playing)
				startService(new Intent(PodcastService.ACTION_PAUSE));
			else if (PodcastService.getMusicServiceState() == PodcastService.State.Paused)
				startService(new Intent(PodcastService.ACTION_PLAY));
		}

	};

	private OnClickListener stopPodcastOnClickListener = new OnClickListener(){

		@Override
		public void onClick(View v) {
			startService(new Intent(PodcastService.ACTION_STOP));
		}
	};

	@Override
	public void playPodcast(PodcastFeedItem item, String date) {
		Timber.d("Play stream URL " + item.getInfo().getUrl());
		if(item.getInfo().getUrl() != null){
			// Send an intent with the URL of the song to play. This is expected by
			// PodcastService.
			Intent i = new Intent(PodcastService.ACTION_URL);
			Bundle b = new Bundle();
			b.putString(PodcastService.PODCAST_TITLE,item.getTitle());
			b.putString(PodcastService.PODCAST_ARTIST,item.getAuthor());
			b.putString(PodcastService.PODCAST_DESCRIPTION, item.getDescription());
			b.putString(PodcastService.PODCAST_DATE, date);
			b.putInt(PodcastService.PODCAST_ARTWORK, item.getArtworkForPlayer());
			i.putExtra(PodcastService.PODCAST_BUNDLE_INFO, b);
			Uri uri = Uri.parse(item.getInfo().getUrl());
			i.setData(uri);
			startService(i);
		}		
	}

    @Override
    public void onBackPressed() {
        if(mSlidingLayout.isExpanded()){
            mSlidingLayout.collapsePane();
        }else{
            super.onBackPressed();
        }
    }

    @Subscribe
	public void onPodcastEvent(PlaybackEvent event) {
		if (event.action.equalsIgnoreCase(PodcastService.ACTION_PLAY) || event.action.equalsIgnoreCase(PodcastService.ACTION_PAUSE)){
			currentlyPlayingBundle = event.podcast_info;
			if(currentlyPlayingBundle != null){
				int progress = currentlyPlayingBundle.getInt(PodcastService.PODCAST_DURATION);
				mSeeker.setMax(progress);
				mTotalDuration.setText(String.format("%d:%02d", TimeUnit.MILLISECONDS.toMinutes(progress),
						TimeUnit.MILLISECONDS.toSeconds(progress) - 
						TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(progress))));
				
				String title = currentlyPlayingBundle.getString(PodcastService.PODCAST_TITLE);
				mPodcastTitle.setText(title);
				mExpPodcastTitle.setText(title);
				String artist = currentlyPlayingBundle.getString(PodcastService.PODCAST_ARTIST).trim();
				mPodcastArtist.setText(artist);
				mExpPodcastArtist.setText(artist);
				try{
					Spanned desc = Html.fromHtml((currentlyPlayingBundle.getString(PodcastService.PODCAST_DESCRIPTION)));
					if(desc.toString() != null){
						mExpPodcastDescription.setText(desc);
						Linkify.addLinks(mExpPodcastDescription, Linkify.WEB_URLS);
					}
				}catch(Exception e){
					Timber.e("error converting text"+ e.toString());
					mExpPodcastDescription.setText("");
				}

				mPodcastArtwork.setImageDrawable(getResources().getDrawable(currentlyPlayingBundle.getInt(PodcastService.PODCAST_ARTWORK)));
				mExpPodcastArtwork.setImageDrawable(getResources().getDrawable(currentlyPlayingBundle.getInt(PodcastService.PODCAST_ARTWORK)));

				mPodcastPanel.setVisibility(View.VISIBLE);
			}
			if(event.action.equalsIgnoreCase(PodcastService.ACTION_PLAY)){
				mPlayPauseButton.setImageResource(R.drawable.ic_action_playback_pause);
			}else if(event.action.equalsIgnoreCase(PodcastService.ACTION_PAUSE)){
				mPlayPauseButton.setImageResource(R.drawable.ic_action_playback_play);
			}
		}else if(event.action.equalsIgnoreCase(PodcastService.ACTION_STOP)){
			mPodcastPanel.setVisibility(View.GONE);
		}
	}

	@Subscribe 
	public void onSeekBarUpdateEvent(SeekBarUpdateEvent event) {
		int progress = event.progress;
		mSeeker.setProgress(progress);
		mCurrentTime.setText(String.format("%d:%02d", TimeUnit.MILLISECONDS.toMinutes(progress),
				TimeUnit.MILLISECONDS.toSeconds(progress) - 
				TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(progress))));
	}

	@Subscribe 
	public void onBufferingUpdateEvent(BufferingUpdateEvent event) {
		mSeeker.setSecondaryProgress(event.percent);
		Log.d("GOT BUFFER: ", event.percent+"");
	}

	@Subscribe
	public void onMediaPreparedEvent(MediaPreparedEvent event) {
		int millis = event.lengthInMillis;
		mSeeker.setMax(event.lengthInMillis);
		mTotalDuration.setText(String.format("%d:%02d", TimeUnit.MILLISECONDS.toMinutes(millis),
				TimeUnit.MILLISECONDS.toSeconds(millis) - 
				TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis))));
		Log.d("GOT PREPARED: ", event.lengthInMillis+"");
	}

}