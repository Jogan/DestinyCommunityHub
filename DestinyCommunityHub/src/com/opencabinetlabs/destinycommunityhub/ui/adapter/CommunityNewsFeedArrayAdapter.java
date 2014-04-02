package com.opencabinetlabs.destinycommunityhub.ui.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.opencabinetlabs.destinycommunityhub.R;
import com.opencabinetlabs.destinycommunityhub.model.NewsFeedData;
import com.opencabinetlabs.destinycommunityhub.model.NewsFeedItem;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;


//public class CommunityNewsFeedArrayAdapter extends ArrayAdapter<String> implements OnEndReachedListener {
public class CommunityNewsFeedArrayAdapter extends ArrayAdapter<String> {

	private final String TAG = getClass().getSimpleName();

	private static final SimpleDateFormat sdf = new SimpleDateFormat("LLL d", Locale.US);

	/**
	 *  The data that drives the adapter
	 */
	private List<NewsFeedItem> mData;


	/**
	 * The last network response containing twitter metadata
	 */
	private NewsFeedData mFeedData;

	private boolean isLoading;

	private Context mContext;


	/**
	 * Flag telling us our last network call returned 0 results and we do not need to execute any new requests
	 */
	private boolean moreDataToLoad;

	/**
	 * @param context
	 * 			The context
	 * @param textViewResourceId
	 * 			Resource for the rows of the listview
	 * @param newData
	 * 			Initial dataset.
	 */
	public CommunityNewsFeedArrayAdapter(Context context, List<NewsFeedItem> newData) {
		super(context, R.layout.card_news_feed_list_item);
		mContext = context;
		//mData = newData.getNews();
		//mFeedData = newData;
		mData = newData;

		//moreDataToLoad = true;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		View v = convertView;
		ViewHolder viewHolder;

		//check to see if we need to load more data
		//if(shouldLoadMoreData(mData, position) ) {
		//	loadMoreData();
		//}

		if(v == null){
			LayoutInflater inflater = (LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			v = inflater.inflate(R.layout.card_news_feed_list_item, null);

			viewHolder = new ViewHolder();
			//viewHolder.twitterUserImage = (NetworkImageView)v.findViewById(R.id.twitterUserImage);
			viewHolder.newsTitleTextView = (TextView)v.findViewById(R.id.newsTitleTextView);
			viewHolder.newsDescriptionTextView = (TextView)v.findViewById(R.id.newsDescriptionTextView);
			viewHolder.newsTimeTextView = (TextView)v.findViewById(R.id.newsTimeTextView);

			v.setTag(viewHolder);

		} else {
			viewHolder = (ViewHolder) v.getTag();
		}

		NewsFeedItem newsItem = mData.get(position);
		viewHolder.newsDescriptionTextView.setTextColor(Color.BLACK);
		if(newsItem != null){
			//viewHolder.twitterUserImage.setImageUrl(newsItem.getUserImageUrl(), ImageCacheManager.getInstance().getImageLoader());
			viewHolder.newsTitleTextView.setText(newsItem.getTitle());
			viewHolder.newsDescriptionTextView.setText(newsItem.getAuthor());
			if(newsItem.getAuthor().equalsIgnoreCase("Bungie [OFFICIAL]")){ //TODO: remove hardcoded values
				viewHolder.newsDescriptionTextView.setTextColor(mContext.getResources().getColor(R.color.holo_blue_dark));
			}
			viewHolder.newsTimeTextView.setText(formatDisplayDate(newsItem.getCreatedDate()));
			viewHolder.destinationUrl = newsItem.getLink();
            viewHolder.siteTitle = newsItem.getAuthor();
		}

		return v;
	}

	@Override
	public int getCount() {
		return mData.size();
	}

	private String formatDisplayDate(Date date){
		if(date != null){
			return sdf.format(date);
		}

		return "";
	}
	/**
	 * Add the data to the current listview
	 * @param newData
	 * 			Data to be added to the listview
	 */
	public void add(List<NewsFeedItem> newData)
	{
		isLoading = false;
		if(!newData.isEmpty()){
			mData.addAll(newData);
			notifyDataSetChanged();
		}
	}


	/*
	private void loadMoreData(){
		isLoading = true;
		//Log.v(getClass().toString(), "Load more newsItems");
		//RequestManager.getInstance(mContext).getDefaultBungieNewsFeed(this, this, RequestManager.getInstance(mContext).getCurrentPage());
		ApiExecutorService.AsyncRequest.getCommunityNewsFeed(mContext, mData.size()/10);
	}
	 */
	public List<NewsFeedItem> getNewsFeedItems() {
		return mData;
	}

	public void setNewsFeedItems(List<NewsFeedItem> newsItems) {
		this.mData = newsItems;
	}



	public static class ViewHolder{
		//NetworkImageView twitterUserImage;
		TextView newsTitleTextView;
		TextView newsDescriptionTextView;
		TextView newsTimeTextView;
		public String destinationUrl;
        public String siteTitle;
	}
	/*
	@Override
	public void onResponse(NewsFeed response) {
		if(response != null){
			mData.addAll(response.getNewsData().getNews());
			mFeedData = response.getNewsData();
			RequestManager.getInstance(mContext).incrementCurrentPage();
			if(mFeedData.getNews() != null && mFeedData.getNews().size() > 0){ //&& mFeedData.getNextPage() != null && !mFeedData.getNextPage().equals("")) {
				moreDataToLoad = true;
			}
			else {
				moreDataToLoad = false;
			}

			notifyDataSetChanged();
			Log.v(TAG, "New newsItems retrieved");
		}

		isLoading = false;
	}

	@Override
	public void onErrorResponse(VolleyError error) {
		Log.e(TAG, "Error retrieving additional newsItems: " + error.getMessage());
		isLoading = false;
	}

	 */

	/*@Override
	public void onEndReached() {
		Timber.d("OnEndReached");
		Timber.d("Adapter DATA SIZE: "+mData.size());
		if(mData.size()<100){
			ApiExecutorService.AsyncRequest.getCommunityNewsFeed(mContext, mData.size()+CommunityRssApi.NUM_RESULTS);
		}
	}*/






}
