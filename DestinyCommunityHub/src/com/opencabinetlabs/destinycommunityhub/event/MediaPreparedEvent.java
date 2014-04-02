package com.opencabinetlabs.destinycommunityhub.event;

public class MediaPreparedEvent {
	public int lengthInMillis;

	public MediaPreparedEvent(int length){
		this.lengthInMillis = length;
	}
}
