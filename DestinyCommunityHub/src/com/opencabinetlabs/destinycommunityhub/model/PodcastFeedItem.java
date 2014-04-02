package com.opencabinetlabs.destinycommunityhub.model;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

import com.opencabinetlabs.destinycommunityhub.R;

import timber.log.Timber;

import android.text.TextUtils;

@Root(name="item", strict=false)
public class PodcastFeedItem {

	private transient long mId = -1;
	private final static String PODCAST_DATE_FORMAT = "EEE, dd MMM yyyy HH:mm:ss Z";

	private static final SimpleDateFormat sdf = new SimpleDateFormat(PODCAST_DATE_FORMAT, Locale.US);
	static {
		sdf.setLenient(true);
	}
	
	private String author;

	@Element
	private String title;

	@Element
	private String pubDate;

	@Element(required = false)
	private String description;

	@Element(name = "enclosure", required = false)
	private PodcastInfo info;

	public PodcastInfo getInfo() {
		return info;
	}

	public void setInfo(PodcastInfo info) {
		this.info = info;
	}

	private Date createdDate;

	private transient long mDate = -1;

	public long getId() {
		return mId;
	}

	public void setId(final long id) {
		mId = id;
	}
	
	public String getAuthor() {
		return author;
	}
	
	public void setAuthor(String author) {
		this.author = author;
	}

	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}

	public String getPubDate() {
		return this.pubDate;
	}
	public void setPubDate(String pubDate){
		this.pubDate = pubDate;
	}

	public String getDescription(){
		return this.description;
	}
	public void setDescription(String description){
		this.description = description;
	}

	public Date getCreatedDate(){
		if(createdDate == null){
			try {
				createdDate = sdf.parse(pubDate);
			} catch (ParseException e) {
				e.printStackTrace();
			}
		}
		return createdDate;
	}
	public long getDate() {
		if (mDate < 0 && !TextUtils.isEmpty(pubDate)) {
			DateFormat dfm = new SimpleDateFormat(PODCAST_DATE_FORMAT, Locale.US);
			//dfm.setLenient(true);
			//dfm.setTimeZone(TimeZone.getTimeZone("GMT"));
			try {
				Date d = dfm.parse(pubDate);
				mDate = d.getTime() / 1000;
			} catch (ParseException e) {
				Timber.e("Error parsing datestring: " + pubDate, e);
			}
		}
		return mDate;
	}

	public void setDate(final long date) {
		mDate = date;
	}

	@Root
	public static class PodcastInfo {

		@Attribute
		private String url;
		@Attribute(required = false)
		private String type;
		@Attribute
		private String length;


		public String getUrl() {
			return url;           
		}
		
		public String getType() {
			return type;           
		}
		
		public String getLength() {
			return length;           
		}
		public void setUrl(String url) {
			this.url = url;
		}

		public void setLength(String length) {
			this.length = length;
		}
	}


    // TODO: Replace with server URL to image
	public int getArtworkForPlayer() {
		if(getAuthor().trim().equalsIgnoreCase("HDDispatch")){
			return R.drawable.banner_destiny_dispatch;
		}else if(getAuthor().trim().equalsIgnoreCase("Guardian Radio")){
			return R.drawable.banner_guardianradio;
		}
		return R.drawable.banner_default;
	}
	 
}
