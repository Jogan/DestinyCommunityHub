package com.opencabinetlabs.destinycommunityhub.ui.fragment;

import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.opencabinetlabs.destinycommunityhub.R;
import com.opencabinetlabs.destinycommunityhub.ui.VideoWallActivity;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class VideosFragment extends Fragment {
	private ListView mListView;
	private List<HashMap<String,String>> mList ;
	private SimpleAdapter mAdapter;

	//Array of strings storing channel names
	private String[] mChannelNames;

	// Array of strings to store channelIds
	private String[] mChannelIds;

	private String[] mChannelImageUrls;

	final private String NAME = "name";
	final private String IMAGE = "image";
	final private String ID = "id";


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		mChannelNames = getResources().getStringArray(R.array.youtube_channel_names);
		mChannelIds = getResources().getStringArray(R.array.youtube_channel_ids);
		mChannelImageUrls = getResources().getStringArray(R.array.youtube_channel_image_urls_mobile);

		ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fragment_videos, container, false);
		mListView = (ListView)rootView.findViewById(R.id.channelsListView);

		return rootView;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		//setEmptyText(getString(R.string.empty_social_stream));

		// Each row in the list stores channel name, id and image
		mList = new ArrayList<HashMap<String,String>>();
		for(int i=0; i < mChannelNames.length ;i++){
			HashMap<String, String> hm = new HashMap<String,String>();
			hm.put(NAME, mChannelNames[i]);
			hm.put(ID,  mChannelIds[i]);
			hm.put(IMAGE, mChannelImageUrls[i]);
			mList.add(hm);
		}

		// Keys used in Hashmap
		String[] from = { NAME };

		// Ids of views in listview_layout
		int[] to = {R.id.channelTextView};

		// Instantiating an adapter to store each items
		// R.layout.drawer_layout defines the layout of each item
			mAdapter = new SimpleAdapter(getActivity(), mList, R.layout.channel_list_item, from, to){
				@Override 
				public View getView(int position, View convertView, ViewGroup parent){
					View view = super.getView(position, convertView, parent);

					ImageView img = (ImageView)view.findViewById(R.id.channelImage);
					Picasso.with(getActivity()).load(mChannelImageUrls[position]).into(img);

					return view;
				}
			};
		
		mListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position,
					long id) {
				Intent i = new Intent(getActivity(), VideoWallActivity.class);
				HashMap<String, String> item = mList.get(position);
				i.putExtra("channelId", item.get(ID));
				i.putExtra("title", item.get(NAME));
				startActivity(i);
			}
		});

		// Setting the adapter to the listView
		mListView.setAdapter(mAdapter);

	}
}
