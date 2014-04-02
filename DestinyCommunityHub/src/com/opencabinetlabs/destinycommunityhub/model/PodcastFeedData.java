package com.opencabinetlabs.destinycommunityhub.model;

import java.util.List;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

import android.os.Parcel;
import android.os.Parcelable;

@Root(name="channel", strict=false)
public class PodcastFeedData  {

	
	@Element(name="title")
	private String title; // Author
	
	@Element(name="description")
	private String description;
	
	public String getTitle() {
		return title;
	}
	
	@ElementList(inline = true, required = false)
    private List<PodcastFeedItem> item;
	
	public List<PodcastFeedItem> getPodcasts() {
        return item == null ? null : item;
    }

    public void setPodcasts(List<PodcastFeedItem> podcasts) {
        this.item = podcasts;
    }

}
