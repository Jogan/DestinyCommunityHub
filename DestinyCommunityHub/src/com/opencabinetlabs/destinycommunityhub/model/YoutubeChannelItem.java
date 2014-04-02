package com.opencabinetlabs.destinycommunityhub.model;


public class YoutubeChannelItem {

	private Id id;
	private Snippet snippet;

	public class Id {
		private String videoId;
		private String playlistId;

		public String getPlaylistId() {
			return playlistId;
		}
		public void setPlaylistId(String playlistId) {
			this.playlistId = playlistId;
		}
		public String getVideoId() {
			return videoId;
		}
		public void setVideoId(String videoId) {
			this.videoId = videoId;
		}
	}

	public Id getId() {
		return id;
	}

	public void setId(Id id) {
		this.id = id;
	}

	public Snippet getSnippet() {
		return snippet;
	}

	public void setSnippet(Snippet snippet) {
		this.snippet = snippet;
	}

	public class Snippet{
		private String title;
		private String description;
		private String channelTitle;
		private String publishedAt;

		private Thumbnails thumbnails;
		
		public class Thumbnails{
			private High high;
			public class High {
				private String url;
				private long width;
				private long height;
				public String getUrl() {
					return url;
				}
				public void setUrl(String url) {
					this.url = url;
				}
				public long getWidth() {
					return width;
				}
				public void setWidth(long width) {
					this.width = width;
				}
				public long getHeight() {
					return height;
				}
				public void setHeight(long height) {
					this.height = height;
				}
			}
			public High getHigh() {
				return high;
			}
			public void setHigh(High high) {
				this.high = high;
			}
		}

		public String getPublishedAt() {
			return publishedAt;
		}

		public void setPublishedAt(String publishedAt) {
			this.publishedAt = publishedAt;
		}
		

		public String getTitle() {
			return title;
		}

		public void setTitle(String title) {
			this.title = title;
		}

		public String getDescription() {
			return description;
		}

		public void setDescription(String description) {
			this.description = description;
		}

		public Thumbnails getThumbnails() {
			return thumbnails;
		}

		public void setThumbnails(Thumbnails thumbnails) {
			this.thumbnails = thumbnails;
		}

		public String getChannelTitle() {
			return channelTitle;
		}

		public void setChannelTitle(String channelTitle) {
			this.channelTitle = channelTitle;
		}
	}


}
