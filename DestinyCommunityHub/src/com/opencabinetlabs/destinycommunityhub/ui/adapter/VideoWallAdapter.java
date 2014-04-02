package com.opencabinetlabs.destinycommunityhub.ui.adapter;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.opencabinetlabs.destinycommunityhub.R;
import com.opencabinetlabs.destinycommunityhub.api.YoutubeApi;
import com.opencabinetlabs.destinycommunityhub.model.YoutubeChannelItem;
import com.opencabinetlabs.destinycommunityhub.ui.view.DimImageView;
import com.squareup.picasso.Picasso;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class VideoWallAdapter extends ArrayAdapter<String> {
	private final String TAG = getClass().getSimpleName();

	private Context mContext;

	private final ArrayList<YoutubeChannelItem> mData;
	
    private static final SparseArray<Double> sPositionHeightRatios = new SparseArray<Double>();

	private static final SimpleDateFormat after = new SimpleDateFormat("LLL d", Locale.US);
	private static final SimpleDateFormat before = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss.s'Z'");
	

	public VideoWallAdapter(Context context) {
		super(context, R.layout.video_cell_stgv);
		mContext = context;
		
		mData = new ArrayList<YoutubeChannelItem>();
		
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {		
		View v = convertView;
		final ViewHolder viewHolder;

		if(v == null){
			LayoutInflater inflater = (LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			v = inflater.inflate(R.layout.video_cell_stgv, null);

			viewHolder = new ViewHolder();
			viewHolder.tv_info = (TextView)v.findViewById(R.id.tv_info);
			viewHolder.img_content = (DimImageView)v.findViewById(R.id.img_content);
			viewHolder.tv_date = (TextView)v.findViewById(R.id.tv_date);
			
			v.setTag(viewHolder);

		} else {
			viewHolder = (ViewHolder) v.getTag();
		}
		
        double positionHeight = getPositionRatio(position);

			YoutubeChannelItem result = mData.get(position);

			if(result != null){
				viewHolder.tv_info.setText(result.getSnippet().getTitle());
				viewHolder.videoId = result.getId().getVideoId();
				viewHolder.tv_date.setText(formatDisplayDate(result.getSnippet().getPublishedAt()));

				viewHolder.img_content.setHeightRatio(positionHeight);

				Picasso.with(mContext).load(result.getSnippet().getThumbnails().getHigh().getUrl()).into(viewHolder.img_content);

				viewHolder.img_content.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						//Open Youtube URL
						Log.v(TAG, "Go to url for videoId: " + viewHolder.videoId);
						if(viewHolder.videoId != null){
							//TODO: Put into safer method like youtube service that cleanly generates correct URL
							Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(YoutubeApi.YOUTUBE_VIDEO_ENDPOINT+viewHolder.videoId));
							mContext.startActivity(intent);
						}
					}
				});	
			}
		return v;
	}

	@Override
	public int getCount() {
		return mData.size();
	}

	/**
	 * Add the data to the current listview
	 * @param newData
	 * 			Data to be added to the listview
	 */
	public void add(YoutubeChannelItem newData){
		if(newData != null){
			mData.add(newData);
		}
	}
	
	@Override
    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
    }
	
	private double getPositionRatio(final int position) {
        double ratio = sPositionHeightRatios.get(position, 0.0);
        // if not yet done generate and stash the columns height
        // in our real world scenario this will be determined by
        // some match based on the known height and width of the image
        // and maybe a helpful way to get the column height!
        if (ratio == 0) {
            ratio = getRandomHeightRatio();
            sPositionHeightRatios.append(position, ratio);
        }
        return ratio;
    }

    private double getRandomHeightRatio() {
        return 1.0; // height will be 1.0 - 1.5 the width
    }

	static class ViewHolder {
		DimImageView img_content;
		TextView tv_info;
		TextView tv_date;
		String videoId;
	}
	
	private String formatDisplayDate(String oldDate){
		Date date = null;
		try {
			date = before.parse(oldDate);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return after.format(date);
	}

}
