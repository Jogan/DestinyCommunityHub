package com.opencabinetlabs.destinycommunityhub.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.opencabinetlabs.destinycommunityhub.R;
import com.opencabinetlabs.destinycommunityhub.model.TwitterListResult.Status;
import com.opencabinetlabs.destinycommunityhub.service.ApiExecutorService;
import com.opencabinetlabs.destinycommunityhub.util.EndlessScrollListener.OnEndReachedListener;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import timber.log.Timber;


public class TweetAdapter extends ArrayAdapter<String> {
//public class TweetAdapter extends ArrayAdapter<String> implements OnEndReachedListener {


        private final String TAG = TweetAdapter.class.getSimpleName();

	private static final SimpleDateFormat sdf = new SimpleDateFormat("LLL d", Locale.US);

	/**
	 *  The data that drives the adapter
	 */
	private List<Status> mData;

	/**
	 * The last network response containing twitter metadata
	 */
	private boolean isLoading;

	private Context mContext;

	/**
	 * Flag telling us our last network call returned 0 results and we do not need to execute any new requests
	 */
	private boolean moreDataToLoad;


	public TweetAdapter(Context context, List<Status> newData) {
		super(context, R.layout.tweet_list_item);
		mContext = context;
		mData = newData;

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
			v = inflater.inflate(R.layout.tweet_list_item, null);

			viewHolder = new ViewHolder();
			//viewHolder.twitterUserImage = (NetworkImageView)v.findViewById(R.id.twitterUserImage);
			viewHolder.dateView = (TextView)v.findViewById(R.id.tweetTimeTextView);
			viewHolder.nameView = (TextView)v.findViewById(R.id.usernameTextView);
			viewHolder.textView = (TextView)v.findViewById(R.id.messageTextView);
			viewHolder.imageView = (ImageView)v.findViewById(R.id.twitterUserImage);

			v.setTag(viewHolder);

		} else {
			viewHolder = (ViewHolder) v.getTag();
		}

		Status status = mData.get(position);
		if(status != null){
			viewHolder.dateView.setText(formatDisplayDate(status.getCreatedDate()));
			viewHolder.nameView.setText(status.getUser().getNickName());
			viewHolder.textView.setText(status.getText());
            String profile_img_url = status.getUser().getUrl().replace("_normal","");
			Picasso.with(mContext).load(profile_img_url).into(viewHolder.imageView);
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
	public void add(List<Status> newData)
	{
		isLoading = false;
		if(!newData.isEmpty()){
			mData.addAll(newData);
			notifyDataSetChanged();
		}
	}

	public List<Status> getTwitterFeedItems() {
		return mData;
	}

	public void setTwitterFeedItems(List<Status> twitterFeedItems) {
		this.mData = twitterFeedItems;
	}



	public static class ViewHolder {
		private ImageView imageView;
		private TextView nameView;
		private TextView textView;
		private TextView dateView;
	}

	/*@Override
	public void onEndReached() {
		Timber.d("OnEndReached");
		Timber.d("Adapter DATA SIZE: "+mData.size());
		if(mData.size()<100 && getCount() > 0){
			long maxId = mData.get(getCount() - 1).getId() - 1;
			ApiExecutorService.AsyncRequest.getTwitterFeed(mContext, Long.toString(maxId));
		}
	}*/






}
