package com.opencabinetlabs.destinycommunityhub.event;

import android.os.Bundle;

public class PlaybackEvent {
	
	public String action;
	public Bundle podcast_info;
	
	public PlaybackEvent(String action, Bundle b){
		this.action = action;
		this.podcast_info = b;
	}
}
