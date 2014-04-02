package com.opencabinetlabs.destinycommunityhub.model;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

@Root(name="rss", strict=false)
public class PodcastFeed {
	
	@Element(name="channel")
	private PodcastFeedData channel;
	
	public PodcastFeedData getPodcastFeedData() {
        return channel;
    }

    public void setPodcastFeedData(PodcastFeedData channel) {
        this.channel = channel;
    }
}
