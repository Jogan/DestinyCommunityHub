package com.opencabinetlabs.destinycommunityhub.model;

import java.util.List;


public class YoutubeSearchResponse {

	private String nextPageToken;
	private String prevPageToken;
	private PageInfo pageInfo;
	private List<YoutubeChannelItem> items;

	public String getNextPageToken() {
		return nextPageToken;
	}
	public void setNextPageToken(String nextPageToken) {
		this.nextPageToken = nextPageToken;
	}
	public String getPrevPageToken() {
		return prevPageToken;
	}
	public void setPrevPageToken(String prevPageToken) {
		this.prevPageToken = prevPageToken;
	}

	public List<YoutubeChannelItem> getItems() {
		return items;
	}
	public void setItems(List<YoutubeChannelItem> items) {
		this.items = items;
	}
	
	public PageInfo getPageInfo() {
		return pageInfo;
	}
	public void setPageInfo(PageInfo pageInfo) {
		this.pageInfo = pageInfo;
	}

	private class PageInfo {
		private int totalResults;
		private int resultsPerPage;

		public int getTotalResults() {
			return totalResults;
		}
		public void setTotalResults(int totalResults) {
			this.totalResults = totalResults;
		}
		public int getResultsPerPage() {
			return resultsPerPage;
		}
		public void setResultsPerPage(int resultsPerPage) {
			this.resultsPerPage = resultsPerPage;
		}

	}





}
