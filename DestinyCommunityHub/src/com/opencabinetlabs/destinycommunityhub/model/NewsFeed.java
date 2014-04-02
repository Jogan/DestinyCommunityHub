package com.opencabinetlabs.destinycommunityhub.model;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

@Root(name="rss", strict=false)
public class NewsFeed {
	
	@Element(name="channel")
	private NewsFeedData channel;
	
	public NewsFeedData getNewsData() {
        return channel;
    }

    public void setNewsFeedData(NewsFeedData channel) {
        this.channel = channel;
    }
}
