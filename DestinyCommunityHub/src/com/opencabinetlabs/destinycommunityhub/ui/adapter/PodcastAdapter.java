package com.opencabinetlabs.destinycommunityhub.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.opencabinetlabs.destinycommunityhub.R;
import com.opencabinetlabs.destinycommunityhub.model.PodcastFeedItem;
import com.opencabinetlabs.destinycommunityhub.ui.PodcastListener;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class PodcastAdapter extends ArrayAdapter<String> {

	/**
	 *  The data that drives the adapter
	 */
	private List<PodcastFeedItem> mData;
		
	private Context mContext;
	
	private static final SimpleDateFormat sdf = new SimpleDateFormat("MM/d/yyyy", Locale.US);
	
	private PodcastListener mListener;
	/**
	 * @param context
	 * 			The context
	 * @param textViewResourceId
	 * 			Resource for the rows of the listview
	 * @param newData
	 * 			Initial dataset.
	 */
	public PodcastAdapter(Context context, List<PodcastFeedItem> newData) {
		super(context, R.layout.podcast_list_item);
		mContext = context;
		mData = newData;
        mListener = (PodcastListener) context;

	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		View v = convertView;
		ViewHolder viewHolder;

		if(v == null){
			LayoutInflater inflater = (LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			v = inflater.inflate(R.layout.podcast_list_item, null);

			viewHolder = new ViewHolder();
			viewHolder.titleTextView = (TextView)v.findViewById(R.id.titleTextView);
			viewHolder.dateTextView = (TextView)v.findViewById(R.id.artistTextView);
			viewHolder.authorImage = (ImageView)v.findViewById(R.id.artworkImage);
			viewHolder.playPauseButton = (ImageButton)v.findViewById(R.id.playPauseProgressButton);
			v.setTag(viewHolder);

		} else {
			viewHolder = (ViewHolder) v.getTag();
		}

		final PodcastFeedItem item = mData.get(position);
		if(item != null && item.getInfo() != null){
			viewHolder.titleTextView.setText(item.getTitle());
			viewHolder.dateTextView.setText(formatDisplayDate(item.getCreatedDate()));
			viewHolder.streamUrl = item.getInfo().getUrl();
			viewHolder.description = item.getDescription();
			viewHolder.duration = Long.valueOf(item.getInfo().getLength());
			
			viewHolder.authorImage.setImageDrawable(mContext.getResources().getDrawable(item.getArtworkForPlayer()));
			
			
			viewHolder.playPauseButton.setOnClickListener(new OnClickListener(){

				@Override
				public void onClick(View v) {
					mListener.playPodcast(item, formatDisplayDate(item.getCreatedDate()));
				}
				
			});
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
	 * Viewholder for the listview row
	 * 
	 * @author John Hogan
	 *
	 */
	public static class ViewHolder{
		public TextView titleTextView;
		public TextView dateTextView;
		public ImageView authorImage;
		public String streamUrl;
		public String description;
		public long duration;
		public ImageButton playPauseButton;
	}


	public void setPodcastFeedItems(List<PodcastFeedItem> data) {
		this.mData = data;		
	}
}
