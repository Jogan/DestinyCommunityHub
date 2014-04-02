
package com.opencabinetlabs.destinycommunityhub.service.mediaplayer;

import java.io.IOException;

import javax.inject.Inject;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnBufferingUpdateListener;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.media.RemoteControlClient;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.WifiLock;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.opencabinetlabs.destinycommunityhub.CommunityHubApp;
import com.opencabinetlabs.destinycommunityhub.R;
import com.opencabinetlabs.destinycommunityhub.event.BufferingUpdateEvent;
import com.opencabinetlabs.destinycommunityhub.event.CurrentlyPlayingEvent;
import com.opencabinetlabs.destinycommunityhub.event.MediaPreparedEvent;
import com.opencabinetlabs.destinycommunityhub.event.PlaybackEvent;
import com.opencabinetlabs.destinycommunityhub.event.SeekBarUpdateEvent;
import com.opencabinetlabs.destinycommunityhub.ui.MainActivity;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

/**
 * Service that handles media playback. This is the Service through which we perform all the media
 * handling in our application. Upon initialization, it starts a {@link MusicRetriever} to scan
 * the user's media. Then, it waits for Intents (which come from our main activity,
 * {@link MainActivity}, which signal the service to perform specific operations: Play, Pause,
 * Rewind, Skip, etc.
 */
public class PodcastService extends Service implements OnCompletionListener, OnPreparedListener,
OnErrorListener, MusicFocusable,
PrepareMusicRetrieverTask.MusicRetrieverPreparedListener, OnBufferingUpdateListener {

	// The tag we put on debug messages
	final static String TAG = PodcastService.class.getSimpleName();

	@Inject
	protected Bus mEventBus;

	//Convenience Constants
	public static final String PODCAST_TITLE = "podcast_title";
	public static final String PODCAST_ARTIST = "podcast_artist";
	public static final String PODCAST_DESCRIPTION = "podcast_description";
	public static final String PODCAST_DATE = "podcast_date";
	public static final String PODCAST_BUNDLE_INFO = "podcast_bundle";
	public static final String PODCAST_ARTWORK = "podcast_artwork";
	public static final String PODCAST_DURATION = "podcast_duration";

	// These are the Intent actions that we are prepared to handle. Notice that the fact these
	// constants exist in our class is a mere convenience: what really defines the actions our
	// service can handle are the <action> tags in the <intent-filters> tag for our service in
	// AndroidManifest.xml.
	public static final String ACTION_TOGGLE_PLAYBACK =
			"com.opencabinetlabs.destinycommunityhub.podcastplayer.action.TOGGLE_PLAYBACK";
	public static final String ACTION_PLAY = "com.opencabinetlabs.destinycommunityhub.podcastplayer.action.PLAY";
	public static final String ACTION_PAUSE = "com.opencabinetlabs.destinycommunityhub.podcastplayer.action.PAUSE";
	public static final String ACTION_STOP = "com.opencabinetlabs.destinycommunityhub.podcastplayer.action.STOP";
	public static final String ACTION_URL = "com.opencabinetlabs.destinycommunityhub.podcastplayer.action.URL";
	public static final String ACTION_SEEK = "com.opencabinetlabs.destinycommunityhub.podcastplayer.action.SEEK";
	public static final String ACTION_PING_FOR_INFO = "com.opencabinetlabs.destinycommunityhub.podcastplayer.action.PING";
	//public static final String ACTION_OPEN_STREAM_FRAG = "com.opencabinetlabs.destinycommunityhub.podcastplayer.action.OPEN_STREAM_FRAG";

	// The volume we set the media player to when we lose audio focus, but are allowed to reduce
	// the volume instead of stopping playback.
	public static final float DUCK_VOLUME = 0.1f;

	// our media player
	MediaPlayer mPlayer = null;
	private int mediaFileLengthInMilliseconds; // This value contains the song duration in milliseconds. 
	private final Handler handler = new Handler();
	// our AudioFocusHelper object, if it's available (it's available on SDK level >= 8)
	// If not available, this will be null. Always check for null before using!
	AudioFocusHelper mAudioFocusHelper = null;

	// indicates the state our service:
	public enum State {
		Retrieving, // the MediaRetriever is retrieving music
		Stopped,    // media player is stopped and not prepared to play
		Preparing,  // media player is preparing...
		Playing,    // playback active (media player ready!). (but the media player may actually be
		// paused in this state if we don't have audio focus. But we stay in this state
		// so that we know we have to resume playback once we get focus back)
		Paused      // playback paused (media player ready!)
	};

	static State mState = State.Retrieving;

	// if in Retrieving mode, this flag indicates whether we should start playing immediately
	// when we are ready or not.
	boolean mStartPlayingAfterRetrieve = false;

	// if mStartPlayingAfterRetrieve is true, this variable indicates the URL that we should
	// start playing when we are ready. If null, we should play a random song from the device
	Uri mWhatToPlayAfterRetrieve = null;

	enum PauseReason {
		UserRequest,  // paused by user request
		FocusLoss,    // paused because of audio focus loss
	};

	// why did we pause? (only relevant if mState == State.Paused)
	PauseReason mPauseReason = PauseReason.UserRequest;

	// do we have audio focus?
	enum AudioFocus {
		NoFocusNoDuck,    // we don't have audio focus, and can't duck
		NoFocusCanDuck,   // we don't have focus, but can play at a low volume ("ducking")
		Focused           // we have full audio focus
	}
	AudioFocus mAudioFocus = AudioFocus.NoFocusNoDuck;


	private Bundle mTrackInfoBundle = new Bundle();
	// title of the song we are currently playing
	private String mSongTitle = "";
	// artist of the song we are currently playing
	private String mSongArtist = "";
	// album URL of the song we are currently playing
	private String mSongAlbumArtUrl = "";
	// duration of the song we are currently playing
	long mSongDuration = 0;
	// the drawable artwork of the song we are currently playing
	int mSongArtwork;

	// whether the song we are playing is streaming from the network
	boolean mIsStreaming = false;

	// Wifi lock that we hold when streaming files from the internet, in order to prevent the
	// device from shutting off the Wifi radio
	WifiLock mWifiLock;

	// The ID we use for the notification (the onscreen alert that appears at the notification
	// area at the top of the screen as an icon -- and as text as well if the user expands the
	// notification area).
	final int NOTIFICATION_ID = 117; // A salute to John

	// Our instance of our MusicRetriever, which handles scanning for media and
	// providing titles and URIs as we need.
	MusicRetriever mRetriever;

	// our RemoteControlClient object, which will use remote control APIs available in
	// SDK level >= 14, if they're available.
	RemoteControlClientCompat mRemoteControlClientCompat;

	// The component name of MusicIntentReceiver, for use with media button and remote control
	// APIs
	ComponentName mMediaButtonReceiverComponent;

	AudioManager mAudioManager;
	NotificationManager mNotificationManager;

	/**
	 * Makes sure the media player exists and has been reset. This will create the media player
	 * if needed, or reset the existing media player if one already exists.
	 */
	void createMediaPlayerIfNeeded() {
		if (mPlayer == null) {
			mPlayer = new MediaPlayer();

			// Make sure the media player will acquire a wake-lock while playing. If we don't do
			// that, the CPU might go to sleep while the song is playing, causing playback to stop.
			//
			// Remember that to use this, we have to declare the android.permission.WAKE_LOCK
			// permission in AndroidManifest.xml.
			mPlayer.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);

			// we want the media player to notify us when it's ready preparing, and when it's done
			// playing:
			mPlayer.setOnPreparedListener(this);
			mPlayer.setOnCompletionListener(this);
			mPlayer.setOnBufferingUpdateListener(this);
			mPlayer.setOnErrorListener(this);
		}
		else
			mPlayer.reset();
	}

	@Override
	public void onCreate() {
		Log.i(TAG, "debug: Creating service");

		CommunityHubApp app = (CommunityHubApp) getApplication();
		app.inject(this);

		mEventBus.register(this);

		// Create the Wifi lock (this does not acquire the lock, this just creates it)
		mWifiLock = ((WifiManager) getSystemService(Context.WIFI_SERVICE))
				.createWifiLock(WifiManager.WIFI_MODE_FULL, "mylock");

		mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		mAudioManager = (AudioManager) getSystemService(AUDIO_SERVICE);

		// Create the retriever and start an asynchronous task that will prepare it.
		mRetriever = new MusicRetriever(getContentResolver());
		(new PrepareMusicRetrieverTask(mRetriever,this)).execute();

		// create the Audio Focus Helper, if the Audio Focus feature is available (SDK 8 or above)
		if (android.os.Build.VERSION.SDK_INT >= 8)
			mAudioFocusHelper = new AudioFocusHelper(getApplicationContext(), this);
		else
			mAudioFocus = AudioFocus.Focused; // no focus feature, so we always "have" audio focus

		mMediaButtonReceiverComponent = new ComponentName(this, MusicIntentReceiver.class);

	}

	/**
	 * Called when we receive an Intent. When we receive an intent sent to us via startService(),
	 * this is the method that gets called. So here we react appropriately depending on the
	 * Intent's action, which specifies what is being requested of us.
	 */
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		String action = intent.getAction();
		if (action.equals(ACTION_TOGGLE_PLAYBACK)) processTogglePlaybackRequest();
		else if (action.equals(ACTION_PLAY)) processPlayRequest();
		else if (action.equals(ACTION_PAUSE)) processPauseRequest();
		else if (action.equals(ACTION_STOP)) processStopRequest();
		else if (action.equals(ACTION_SEEK)) processSeekRequest(intent.getIntExtra("seekProgress", 0));
		else if (action.equals(ACTION_URL)) {
			processAddRequest(intent);
		}
		//else if (action.equals(ACTION_PING_FOR_INFO)) {
		//	processPingRequest();
		//}

		return START_NOT_STICKY; // Means we started the service, but don't want it to
		// restart in case it's killed.
	}

	

	void processTogglePlaybackRequest() {
		if (mState == State.Paused || mState == State.Stopped) {
			processPlayRequest();
		} else {
			processPauseRequest();
		}
	}

	void processPlayRequest() {
		if (mState == State.Retrieving) {
			// If we are still retrieving media, just set the flag to start playing when we're
			// ready
			mWhatToPlayAfterRetrieve = null; // play a random song
			mStartPlayingAfterRetrieve = true;
			return;
		}

		tryToGetAudioFocus();

		// actually play the song

		if (mState == State.Stopped) {
			// If we're stopped, just go ahead to the next song and start playing
			playNextSong(null, mTrackInfoBundle);
		}
		else if (mState == State.Paused) {
			// If we're paused, just continue playback and restore the 'foreground service' state.
			mState = State.Playing;
			setUpAsForeground(mSongTitle + " (playing)", mSongArtist, mSongArtwork, true);
			configAndStartMediaPlayer();

			primarySeekBarProgressUpdater();

			mEventBus.post(new PlaybackEvent(ACTION_PLAY, null));

		}

		// Tell any remote controls that our playback state is 'playing'.
		if (mRemoteControlClientCompat != null) {
			mRemoteControlClientCompat
			.setPlaybackState(RemoteControlClient.PLAYSTATE_PLAYING);
		}
	}

	void processPauseRequest() {
		if (mState == State.Retrieving) {
			// If we are still retrieving media, clear the flag that indicates we should start
			// playing when we're ready
			mStartPlayingAfterRetrieve = false;
			return;
		}

		if (mState == State.Playing) {
			// Pause media player and cancel the 'foreground service' state.
			mState = State.Paused;
			mPlayer.pause();
			relaxResources(false); // while paused, we always retain the MediaPlayer

			// Tell UI we are paused
			mEventBus.post(new PlaybackEvent(ACTION_PAUSE, null));
			// Update the current notification
			updateNotification(mSongTitle, mSongArtist, mSongArtwork, false);
		}

		// Tell any remote controls that our playback state is 'paused'.
		if (mRemoteControlClientCompat != null) {
			mRemoteControlClientCompat
			.setPlaybackState(RemoteControlClient.PLAYSTATE_PAUSED);
		}
	}

	public static State getMusicServiceState() {
		return mState;
	}

	void processStopRequest() {
		processStopRequest(false);
	}

	void processStopRequest(boolean force) {
		if (mState == State.Playing || mState == State.Paused || force) {
			mState = State.Stopped;

			// let go of all resources...
			relaxResources(true);
			giveUpAudioFocus();

			// Tell the UI that we are stopped
			mEventBus.post(new PlaybackEvent(ACTION_STOP, null));

			// Tell any remote controls that our playback state is 'paused'.
			if (mRemoteControlClientCompat != null) {
				mRemoteControlClientCompat
				.setPlaybackState(RemoteControlClient.PLAYSTATE_STOPPED);
			}

			// service is no longer necessary. Will be started again if needed.
			stopSelf();
		}
	}

	void processSeekRequest(int progress) {
		seek(progress);
	}

	public void seek(int timeInMillis) {
		if(mPlayer != null && mPlayer.isPlaying()) {
			mPlayer.seekTo(timeInMillis);
		}
	}

	public void seekAndPlay(int timeInMillis) {
		if(mPlayer != null) {
			if (!mPlayer.isPlaying())
				//mPlayer.play();
				mPlayer.seekTo(timeInMillis);
		}
	}


	/**
	 * Releases resources used by the service for playback. This includes the "foreground service"
	 * status and notification, the wake locks and possibly the MediaPlayer.
	 *
	 * @param releaseMediaPlayer Indicates whether the Media Player should also be released or not
	 */
	void relaxResources(boolean releaseMediaPlayer) {
		// stop being a foreground service
		stopForeground(false);

		// stop and release the Media Player, if it's available
		if (releaseMediaPlayer && mPlayer != null) {
			mPlayer.reset();
			mPlayer.release();
			mPlayer = null;
		}

		// we can also release the Wifi lock, if we're holding it
		if (mWifiLock.isHeld()) mWifiLock.release();
	}

	void giveUpAudioFocus() {
		if (mAudioFocus == AudioFocus.Focused && mAudioFocusHelper != null
				&& mAudioFocusHelper.abandonFocus())
			mAudioFocus = AudioFocus.NoFocusNoDuck;
	}

	/**
	 * Reconfigures MediaPlayer according to audio focus settings and starts/restarts it. This
	 * method starts/restarts the MediaPlayer respecting the current audio focus state. So if
	 * we have focus, it will play normally; if we don't have focus, it will either leave the
	 * MediaPlayer paused or set it to a low volume, depending on what is allowed by the
	 * current focus settings. This method assumes mPlayer != null, so if you are calling it,
	 * you have to do so from a context where you are sure this is the case.
	 */
	void configAndStartMediaPlayer() {
		if (mAudioFocus == AudioFocus.NoFocusNoDuck) {
			// If we don't have audio focus and can't duck, we have to pause, even if mState
			// is State.Playing. But we stay in the Playing state so that we know we have to resume
			// playback once we get the focus back.
			if (mPlayer.isPlaying()) mPlayer.pause();
			return;
		}
		else if (mAudioFocus == AudioFocus.NoFocusCanDuck)
			mPlayer.setVolume(DUCK_VOLUME, DUCK_VOLUME);  // we'll be relatively quiet
		else
			mPlayer.setVolume(1.0f, 1.0f); // we can be loud

		if (!mPlayer.isPlaying()) mPlayer.start();
	}

	void processAddRequest(Intent intent) {
		// user wants to play a song directly by URL or path. The URL or path comes in the "data"
		// part of the Intent. This Intent is sent by {@link MainActivity} after the user
		// specifies the URL/path via an alert box.
		if (mState == State.Retrieving) {
			// we'll play the requested URL right after we finish retrieving
			mWhatToPlayAfterRetrieve = intent.getData();
			this.mTrackInfoBundle = intent.getBundleExtra(PODCAST_BUNDLE_INFO);
			mStartPlayingAfterRetrieve = true;
		}
		else if (mState == State.Playing || mState == State.Paused || mState == State.Stopped) {
			Log.i(TAG, "Playing from URL/path: " + intent.getData().toString());
			tryToGetAudioFocus();

            this.mTrackInfoBundle = intent.getBundleExtra(PODCAST_BUNDLE_INFO);

			playNextSong(intent.getData().toString(), intent.getBundleExtra(PODCAST_BUNDLE_INFO));

		}
	}

	void tryToGetAudioFocus() {
		if (mAudioFocus != AudioFocus.Focused && mAudioFocusHelper != null
				&& mAudioFocusHelper.requestFocus())
			mAudioFocus = AudioFocus.Focused;
	}

	/**
	 * Starts playing the next song. If manualUrl is null, the next song will be randomly selected
	 * from our Media Retriever (that is, it will be a random song in the user's device). If
	 * manualUrl is non-null, then it specifies the URL or path to the song that will be played
	 * next.
	 */
	void playNextSong(String manualUrl, Bundle b) {
		mState = State.Stopped;
		relaxResources(false); // release everything except MediaPlayer

		try {
			MusicRetriever.Item playingItem = null;
			if (manualUrl != null) {
				// set the source of the media player to a manual URL or path
				createMediaPlayerIfNeeded();
				mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
				mPlayer.setDataSource(manualUrl);
				mIsStreaming = manualUrl.startsWith("http:") || manualUrl.startsWith("https:");

				mEventBus.post(new PlaybackEvent(ACTION_PLAY, b));

				if (b != null){
					mSongTitle = b.getString(PODCAST_TITLE);
					mSongArtist = b.getString(PODCAST_ARTIST);
					mSongArtwork = b.getInt(PODCAST_ARTWORK);
					//mSongAlbumArtUrl = b.getString();
					Log.d(TAG, "Bundle contains: "
							+mSongTitle+" "
							+mSongArtist+" "
							+mSongAlbumArtUrl+" "
							+mSongDuration);
				}else{
					Log.e(TAG, "bundle is null");
				}

				playingItem = new MusicRetriever.Item(0, null, mSongTitle, mSongArtist, mSongDuration, mSongArtwork);
			}
			else {
				mIsStreaming = false; // playing a locally available song

				// set the source of the media player a a content URI
				createMediaPlayerIfNeeded();
				mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
				mPlayer.setDataSource(getApplicationContext(), playingItem.getURI());
			}

			mSongTitle = playingItem.getTitle();

			mState = State.Preparing;
			setUpAsForeground(mSongTitle + " (loading)", mSongArtist, mSongArtwork, true);

			// Use the media button APIs (if available) to register ourselves for media button
			// events

			MediaButtonHelper.registerMediaButtonEventReceiverCompat(
					mAudioManager, mMediaButtonReceiverComponent);

			// Use the remote control APIs (if available) to set the playback state

			if (mRemoteControlClientCompat == null) {
				Intent intent = new Intent(Intent.ACTION_MEDIA_BUTTON);
				intent.setComponent(mMediaButtonReceiverComponent);
				mRemoteControlClientCompat = new RemoteControlClientCompat(
						PendingIntent.getBroadcast(this /*context*/,
								0 /*requestCode, ignored*/, intent /*intent*/, 0 /*flags*/));
				RemoteControlHelper.registerRemoteControlClient(mAudioManager,
						mRemoteControlClientCompat);
			}

			mRemoteControlClientCompat.setPlaybackState(
					RemoteControlClient.PLAYSTATE_PLAYING);

			mRemoteControlClientCompat.setTransportControlFlags(
					RemoteControlClient.FLAG_KEY_MEDIA_PLAY |
					RemoteControlClient.FLAG_KEY_MEDIA_PAUSE |
					RemoteControlClient.FLAG_KEY_MEDIA_STOP);

			// Update the remote controls
			mRemoteControlClientCompat.editMetadata(true)
			.putString(MediaMetadataRetriever.METADATA_KEY_ARTIST, playingItem.getArtist())
			.putString(MediaMetadataRetriever.METADATA_KEY_ALBUM, playingItem.getAlbum())
			.putString(MediaMetadataRetriever.METADATA_KEY_TITLE, playingItem.getTitle())
			.putLong(MediaMetadataRetriever.METADATA_KEY_DURATION,
					playingItem.getDuration())
					.putBitmap(
							RemoteControlClientCompat.MetadataEditorCompat.METADATA_KEY_ARTWORK,
							BitmapFactory.decodeResource(getResources(), playingItem.getArtwork()))
							.apply();


			// starts preparing the media player in the background. When it's done, it will call
			// our OnPreparedListener (that is, the onPrepared() method on this class, since we set
			// the listener to 'this').
			//
			// Until the media player is prepared, we *cannot* call start() on it!
			mPlayer.prepareAsync();

			// If we are streaming from the internet, we want to hold a Wifi lock, which prevents
			// the Wifi radio from going to sleep while the song is playing. If, on the other hand,
			// we are *not* streaming, we want to release the lock if we were holding it before.
			if (mIsStreaming) mWifiLock.acquire();
			else if (mWifiLock.isHeld()) mWifiLock.release();
		}
		catch (IOException ex) {
			Log.e("MusicService", "IOException playing next song: " + ex.getMessage());
			ex.printStackTrace();
		}
	}


	/** Called when media player is done playing current podcast. */
	public void onCompletion(MediaPlayer player) {
		// The media player finished playing the current podcast, so we go ahead and just close the service.
		// User can always start another...
		processStopRequest(true);
	}

	/** Called when media player is done preparing. */
	public void onPrepared(MediaPlayer player) {
		mediaFileLengthInMilliseconds = mPlayer.getDuration(); // gets the song length in milliseconds from URL
		mEventBus.post(new MediaPreparedEvent(mediaFileLengthInMilliseconds));
		// The media player is done preparing. That means we can start playing!
		mState = State.Playing;
		updateNotification(mSongTitle + " (playing)", mSongArtist, mSongArtwork, false);
		configAndStartMediaPlayer();
		// Begin updating the seekbar since the mediaPlayer has started
		primarySeekBarProgressUpdater();
	}

	/** Updates the notification. */
	private	void updateNotification(String text, String artist, int artwork, boolean showticker) {
		Notification notification = buildNotification(text, artist, artwork, showticker);
		if(notification!=null){
			mNotificationManager.notify(NOTIFICATION_ID, notification);
		}
	}

	/**
	 * Configures service as a foreground service. A foreground service is a service that's doing
	 * something the user is actively aware of (such as playing music), and must appear to the
	 * user as a notification. That's why we create the notification here.
	 */
	private	void setUpAsForeground(String text, String artist, int artwork, boolean showticker) {
		Notification notification = buildNotification(text, artist, artwork, showticker);
		if(notification != null){
			startForeground(NOTIFICATION_ID, notification);
		}
	}

	/**
	 * Called when we want to build our notification
	 */
	private Notification buildNotification(String title, String artist, int artwork, boolean showticker){
		PendingIntent pi = PendingIntent.getActivity(getApplicationContext(), 0,
				new Intent(this, MainActivity.class).setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP),
				PendingIntent.FLAG_UPDATE_CURRENT);

		PendingIntent pendingStopIntent = PendingIntent.getService(this, 118, new Intent(PodcastService.ACTION_STOP), PendingIntent.FLAG_CANCEL_CURRENT);
		PendingIntent pendingPauseIntent = PendingIntent.getService(this, 119, new Intent(PodcastService.ACTION_PAUSE), PendingIntent.FLAG_CANCEL_CURRENT);
		PendingIntent pendingPlayIntent = PendingIntent.getService(this, 120, new Intent(PodcastService.ACTION_PLAY), PendingIntent.FLAG_CANCEL_CURRENT);

		RemoteViews mNotificationView = new RemoteViews(getPackageName(),
				R.layout.notification_podcast_view);

		Notification mNotification = null;

		NotificationCompat.Builder builder = new NotificationCompat.Builder(
				this);
		mNotificationView.setImageViewResource(R.id.imageNotification, artwork );
		mNotificationView.setTextViewText(R.id.titleNotification, title);
		mNotificationView.setTextColor(R.id.titleNotification, Color.WHITE);
		mNotificationView.setTextViewText(R.id.textNotification, artist);

		//Use combo of these depending on if media player is playing
		if(mState == State.Playing){
			mNotificationView.setImageViewResource(R.id.control_one_button_notification, R.drawable.ic_action_playback_pause_white);
			mNotificationView.setImageViewResource(R.id.control_two_button_notification, R.drawable.ic_notif_close);
			mNotificationView.setOnClickPendingIntent(R.id.control_one_button_notification, pendingPauseIntent);
			mNotificationView.setOnClickPendingIntent(R.id.control_two_button_notification, pendingStopIntent);
		}else if(mState == State.Paused){
			mNotificationView.setImageViewResource(R.id.control_one_button_notification, R.drawable.ic_action_playback_play_white);
			mNotificationView.setImageViewResource(R.id.control_two_button_notification, R.drawable.ic_notif_close);
			mNotificationView.setOnClickPendingIntent(R.id.control_one_button_notification, pendingPlayIntent);
			mNotificationView.setOnClickPendingIntent(R.id.control_two_button_notification, pendingStopIntent);
		}

		builder.setContentIntent(pi).setSmallIcon(R.drawable.action_bar_icon);
		if(showticker){
			builder.setTicker(title);
		}else{
			builder.setTicker(null);
		}
		builder.setAutoCancel(true).setContent(mNotificationView).setOngoing(true);
		mNotification = builder.build();

		return mNotification;
	}

	/**
	 * Called when there's an error playing media. When this happens, the media player goes to
	 * the Error state. We warn the user about the error and reset the media player.
	 */
	public boolean onError(MediaPlayer mp, int what, int extra) {
		Toast.makeText(getApplicationContext(), "Podcast playback error! Resetting.",
				Toast.LENGTH_SHORT).show();
		Log.e(TAG, "Error: what=" + String.valueOf(what) + ", extra=" + String.valueOf(extra));

		mState = State.Stopped;
		relaxResources(true);
		giveUpAudioFocus();
		return true; // true indicates we handled the error
	}

	public void onGainedAudioFocus() {
		//Toast.makeText(getApplicationContext(), "gained audio focus.", Toast.LENGTH_SHORT).show();
		mAudioFocus = AudioFocus.Focused;

		// restart media player with new focus settings
		if (mState == State.Playing)
			configAndStartMediaPlayer();
	}

	public void onLostAudioFocus(boolean canDuck) {
		//Toast.makeText(getApplicationContext(), "lost audio focus." + (canDuck ? "can duck" :
		//		"no duck"), Toast.LENGTH_SHORT).show();
		mAudioFocus = canDuck ? AudioFocus.NoFocusCanDuck : AudioFocus.NoFocusNoDuck;

		// start/restart/pause media player with new focus settings
		if (mPlayer != null && mPlayer.isPlaying())
			configAndStartMediaPlayer();
	}

	private boolean isPlaying(){
		if(mPlayer !=null) {
			return mPlayer.isPlaying();
		}
		return false;
	}

	public void onMusicRetrieverPrepared() {
		// Done retrieving!
		mState = State.Stopped;

		// If the flag indicates we should start playing after retrieving, let's do that now.
		if (mStartPlayingAfterRetrieve) {
			tryToGetAudioFocus();
			playNextSong(mWhatToPlayAfterRetrieve == null ?
					null : mWhatToPlayAfterRetrieve.toString(), mTrackInfoBundle);
		}
	}


	@Override
	public void onDestroy() {
		// Service is being killed, so make sure we release our resources
		mState = State.Stopped;
		relaxResources(true);
		giveUpAudioFocus();
		mEventBus.unregister(this);
	}

	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}

	/** Method which updates the SeekBar primary progress by current song playing position*/
	private void primarySeekBarProgressUpdater() {
		//seekBarProgress.setProgress((int)(((float)mPlayer.getCurrentPosition()/mediaFileLengthInMilliseconds)*100)); // This math construction give a percentage of "was playing"/"song length"
		if (mPlayer != null && mPlayer.isPlaying()) {
			mEventBus.post(new SeekBarUpdateEvent(mPlayer.getCurrentPosition())); 

			Runnable notification = new Runnable() {
				public void run() {
					primarySeekBarProgressUpdater();
				}
			};
			handler.postDelayed(notification,1000);
		}
	}

	@Override
	public void onBufferingUpdate(MediaPlayer mp, int percent) { //1629432
		//float p = percent/100f;
		//int adjusted = (int) (p*mediaFileLengthInMilliseconds);
		mEventBus.post(new BufferingUpdateEvent((int) ((float)(percent/100f)*mediaFileLengthInMilliseconds)));
	}
	
	@Subscribe
	public void currentlyPlayingEvent(CurrentlyPlayingEvent event) {
		if(mState == State.Playing){
            if(mTrackInfoBundle != null){
			mTrackInfoBundle.putInt(PODCAST_DURATION, mPlayer.getDuration());
            }else{
                Toast.makeText(this, "trackInfo is null!",Toast.LENGTH_LONG).show();
            }
			mEventBus.post(new PlaybackEvent(ACTION_PLAY,mTrackInfoBundle));
		}else if(mState == State.Paused){
			mTrackInfoBundle.putInt(PODCAST_DURATION, mPlayer.getDuration());
			mEventBus.post(new PlaybackEvent(ACTION_PAUSE,mTrackInfoBundle));
		}
	}


}
