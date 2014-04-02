package com.opencabinetlabs.destinycommunityhub.model;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import timber.log.Timber;

import android.text.TextUtils;

import com.google.gson.annotations.SerializedName;

public class TwitterListResult {

	private final static String TWITTER_DATE_FORMAT = "EEE MMM dd HH:mm:ss ZZZZZ yyyy";

	private static final SimpleDateFormat twitter_date_format = new SimpleDateFormat(TWITTER_DATE_FORMAT,Locale.US);
	static {
		twitter_date_format.setLenient(true);
	}

	public List<Status> statuses;

	public List<Status> getStatuses() {
		return statuses;
	}

	public void setStatuses(List<Status> statuses) {
		this.statuses = statuses;
	}

	public static class Status {

		private transient long mDate = -1;

		@SerializedName("id")
		public long id;

		@SerializedName("created_at")
		public String pubDate;

		@SerializedName("text")
		public String text;

		@SerializedName("user")
		public User user;

		public Date createdDate;

		public long getId() {
			return id;
		}

		public void setId(long id) {
			this.id = id;
		}

		public String getPubDate() {
			return pubDate;
		}

		public void setPubDate(String pubDate) {
			this.pubDate = pubDate;
		}

		public Date getCreatedDate(){
			if(createdDate == null){
				try {
					createdDate = twitter_date_format.parse(pubDate);
				} catch (ParseException e) {
					e.printStackTrace();
				}
			}
			return createdDate;
		}

		public long getDate() {
			if (mDate < 0 && !TextUtils.isEmpty(pubDate)) {
				DateFormat dfm = new SimpleDateFormat(TWITTER_DATE_FORMAT, Locale.US);
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

		public String getText() {
			return text;
		}

		public void setText(String text) {
			this.text = text;
		}

		public User getUser() {
			return user;
		}

		public void setUser(User user) {
			this.user = user;
		}

	}

	public static class User {
		@SerializedName("id")
		public long id;

		@SerializedName("name")
		public String name;

		public long getId() {
			return id;
		}

		public void setId(long id) {
			this.id = id;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public String getNickName() {
			return nickName;
		}

		public void setNickName(String nickName) {
			this.nickName = nickName;
		}

		public String getUrl() {
			return url;
		}

		public void setUrl(String url) {
			this.url = url;
		}

		public String getDescription() {
			return description;
		}

		public void setDescription(String description) {
			this.description = description;
		}

		@SerializedName("screen_name")
		public String nickName;

		@SerializedName("profile_image_url")
		public String url;

		@SerializedName("description")
		public String description;

	}
}
