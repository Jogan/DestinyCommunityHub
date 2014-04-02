package com.opencabinetlabs.destinycommunityhub.model;

import java.util.List;

import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

import android.os.Parcel;
import android.os.Parcelable;

//public class NewsFeedData implements Parcelable {
@Root(name="channel", strict=false)
public class NewsFeedData {


        @ElementList(inline = true, required = false)
    private List<NewsFeedItem> item;
	
	public List<NewsFeedItem> getNews() {
        return item == null ? null : item;
    }

    public void setNews(List<NewsFeedItem> news) {
        this.item = news;
    }

}
