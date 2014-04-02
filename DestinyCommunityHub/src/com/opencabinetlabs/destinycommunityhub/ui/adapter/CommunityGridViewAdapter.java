package com.opencabinetlabs.destinycommunityhub.ui.adapter;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.etsy.android.grid.util.DynamicHeightImageView;
import com.opencabinetlabs.destinycommunityhub.R;
import com.opencabinetlabs.destinycommunityhub.ui.CommunityWebviewActivity;
import com.squareup.picasso.Picasso;

import java.util.List;

public class CommunityGridViewAdapter extends ArrayAdapter<String> {

	private Context mContext;
	private List<CommunityGridItem> mData;
	
    private static final SparseArray<Double> sPositionHeightRatios = new SparseArray<Double>();


	public CommunityGridViewAdapter(Context context, List<Integer> items, List<CommunityGridItem> data) {
		super(context, R.layout.community_cell_stgv);
		mContext = context;
		mData = data;

	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		View v = convertView;
		final ViewHolder viewHolder;

		if(v == null){
			LayoutInflater inflater = (LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			v = inflater.inflate(R.layout.community_cell_stgv, null);

			viewHolder = new ViewHolder();
			viewHolder.tv_community_name = (TextView)v.findViewById(R.id.tv_community_title);
			viewHolder.tv_community_desc = (TextView)v.findViewById(R.id.tv_community_description);
			viewHolder.img_content = (DynamicHeightImageView)v.findViewById(R.id.img_community_image);
			viewHolder.go_to_website = (RelativeLayout)v.findViewById(R.id.layout_gotowebsite);
			
			v.setTag(viewHolder);

		} else {
			viewHolder = (ViewHolder) v.getTag();
		}
		
        double positionHeight = getPositionRatio(position);

		final CommunityGridItem result = mData.get(position);
		if(result != null){

			viewHolder.tv_community_name.setText(result.getTitle());
			viewHolder.tv_community_desc.setText(result.getText());
			

			viewHolder.img_content.setHeightRatio(positionHeight);

            Picasso.with(mContext).load(result.getImgResource()).into(viewHolder.img_content);

			viewHolder.url = result.getURL();

			viewHolder.go_to_website.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					if(viewHolder.url != null){

                        int[] screenLocation = new int[2];
                        viewHolder.img_content.getLocationOnScreen(screenLocation);
                        Intent intent = new Intent(mContext, CommunityWebviewActivity.class);
                        int orientation = mContext.getResources().getConfiguration().orientation;
                        intent.putExtra("orientation", orientation)
                                .putExtra("left",screenLocation[0])
                                .putExtra("top",screenLocation[1])
                                .putExtra("resourceId", result.getImgResource())
                                .putExtra("width", viewHolder.img_content.getWidth())
                                .putExtra("height",viewHolder.img_content.getHeight())
                                .putExtra("siteTitle", viewHolder.tv_community_name.getText())
                                .putExtra("url", viewHolder.url);

                        Uri uri = Uri.parse(viewHolder.url);
                        if (uri.getHost().contains("youtube.com")) {
                            Intent youtubeIntent = new Intent(Intent.ACTION_VIEW, uri);
                            mContext.startActivity(youtubeIntent);
                        }else{
                            mContext.startActivity(intent);
                        }
					}
				}
			});

		}

		return v;
	}
	
	static class ViewHolder {
		DynamicHeightImageView img_content;
		TextView tv_community_name;
		TextView tv_community_desc;
		RelativeLayout go_to_website;
		String url;
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
	
	@Override
	public int getCount() {
		return mData.size();
	}

}