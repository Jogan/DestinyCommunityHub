package com.opencabinetlabs.destinycommunityhub.model;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

import timber.log.Timber;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

@Root(name="item", strict=false)
public class NewsFeedItem implements Parcelable {

	private transient long mId = -1;
	private final static String NEWS_DATE_FORMAT = "EEE, dd MMM yyyy HH:mm:ss Z"; // Yahoo
	private final static String NEWS_DATE_FORMAT2 = "EEE, dd MMM yyyy HH:mm:ss z"; // Bungie

	private static final SimpleDateFormat sdf = new SimpleDateFormat(NEWS_DATE_FORMAT, Locale.US);
	static {
		sdf.setLenient(true);
	}
	private static final SimpleDateFormat sdf2 = new SimpleDateFormat(NEWS_DATE_FORMAT2, Locale.US);
	static {
		sdf2.setLenient(true);
	}

	private final static String OPTION1 = "hddispatch";
	private final static String OPTION2 = "destinynews";
	private final static String OPTION3 = "desti-nation";
	private final static String OPTION4 = "destiny.bungie.org";
	private final static String OPTION5 = "bungie.net";

	@Element
	private String title;
	@Element
	private String link;
	@Element
	private String pubDate;
	@Element(required = false)
	private String description;

	private Date createdDate;
	
    private transient long mDate = -1;
    
    private String author;

	public long getId() {
		return mId;
	}

	public void setId(final long id) {
		mId = id;
	}

	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}

	public String getLink() {
		return link;
	}
	public void setLink(String link) {
		this.link = link;
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
			DateFormat dfm = new SimpleDateFormat(NEWS_DATE_FORMAT, Locale.US);
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

    // This will eventually be replaced as app will be moved to all server side data
	public String getAuthor() {
		if(link.contains(OPTION1))
			return "Destiny Dispatch";
		else if(link.contains(OPTION2)){
			return "Destiny-News";
		}else if(link.contains(OPTION3)){
			return "Desti-Nation";
		}else if(link.contains(OPTION4)){
			return "Destiny.Bungie.Org";
		}else if(link.contains(OPTION5)){
			return "Bungie [OFFICIAL]";
		}else{
			return "";
		}
	}
	
	public void setAuthor(String author) {
		this.author = author;
	}

	@Override
	public int describeContents() {
		return hashCode();
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeLong(mId);
		dest.writeString(pubDate);
		dest.writeString(title);
		dest.writeString(description);
		dest.writeString(link);
		dest.writeString(getAuthor());
		dest.writeLong(mDate);
	}

	public static final Creator<NewsFeedItem> CREATOR = new Creator<NewsFeedItem>() {
		public NewsFeedItem createFromParcel(Parcel in) {
			final NewsFeedItem item = new NewsFeedItem();

			item.mId = in.readLong();
			item.pubDate = in.readString();
			item.title = in.readString();
			item.description = in.readString();
			item.link = in.readString();
			item.author = in.readString();
			item.mDate = in.readLong();


			return item;
		}

		public NewsFeedItem[] newArray(int size) {
			return new NewsFeedItem[size];
		}
	};

}
