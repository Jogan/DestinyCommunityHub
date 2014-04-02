package com.opencabinetlabs.destinycommunityhub.event;

public class BufferingUpdateEvent {
	public final int percent;
	
	public BufferingUpdateEvent(int percent){
		this.percent = percent;
	}
}

