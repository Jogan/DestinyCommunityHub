package com.opencabinetlabs.destinycommunityhub.ui.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.etsy.android.grid.StaggeredGridView;
import com.opencabinetlabs.destinycommunityhub.R;
import com.opencabinetlabs.destinycommunityhub.ui.adapter.CommunityGridItem;
import com.opencabinetlabs.destinycommunityhub.ui.adapter.CommunityGridViewAdapter;

import java.util.ArrayList;
import java.util.List;


public class CommunityFragment extends Fragment {

	private CommunityGridViewAdapter mCommunityGridViewAdapter;
	private StaggeredGridView mStgv;
	private List<CommunityGridItem> mData;

	private static int mNumCells;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


    }

    @Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fragment_community, container, false);

		String[] mNames = getResources().getStringArray(R.array.community_names);
		String[] mDescriptions = getResources().getStringArray(R.array.community_descriptions);
		String[] mUrls = getResources().getStringArray(R.array.community_urls);
		Integer[] mBannerIds = {
				R.drawable.banner_destiny_dispatch, // Destiny Dispatch
				R.drawable.banner_default, // Destiny Guardian
				R.drawable.banner_default, // That Destiny Blog
				R.drawable.banner_destinybungieorg, // Destiny.Bungie.Org
				R.drawable.banner_destination, // Desti-Nation
				R.drawable.banner_default, // DestinyNews.Net,
				R.drawable.banner_default, // Destinypedia,
				R.drawable.banner_default, // Destiny Players,
				R.drawable.banner_reddit, // r/DestinyTheGame,
				R.drawable.banner_default, // Destiny Spain,
				R.drawable.banner_destinymexico, // Destiny Mexico
				R.drawable.banner_guardianradio, // Guardian Radio
				R.drawable.banner_destinyblogde, // Destiny Blog.de
				R.drawable.banner_dattodoesdestiny, // Datto Does Destiny
				R.drawable.banner_destinyoverwatch, // Destiny Overwatch,
				R.drawable.banner_destinyupdates, // Destiny Updates
				R.drawable.banner_mathchief, // MathChief
				R.drawable.banner_moreconsole, // MoreConsole
				R.drawable.banner_reachforge, //Reach Forge Network,
				R.drawable.banner_chrisfrancisk, // ChrisFrancisK
                R.drawable.banner_destiny_fr, // Destiny Fr
                R.drawable.banner_destiny_tracker, // Destiny Tracker
                R.drawable.banner_dearbungie, // Dear Bungie
		};

		// Each row in the list stores community name, description, image and url
		mData = new ArrayList<CommunityGridItem>();
		for(int i=0; i < mNames.length ;i++){
			CommunityGridItem cgi = new CommunityGridItem(mNames[i], mBannerIds[i], mDescriptions[i], mUrls[i]);
			mData.add(cgi);
		}

		mNumCells = mData.size();

		mStgv = (StaggeredGridView)rootView.findViewById(R.id.community_stgv);

		return rootView;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		if(mStgv.getAdapter() == null) {
			mCommunityGridViewAdapter = new CommunityGridViewAdapter((Context)getActivity(), getItems(), mData);
			mStgv.setAdapter(mCommunityGridViewAdapter);
			mCommunityGridViewAdapter.notifyDataSetChanged();
		}

	}

	public static ArrayList<Integer> getItems() {
		ArrayList<Integer> items = new ArrayList<Integer>();
		for (int i = 0; i < mNumCells; i++) {
			items.add(i);
		}
		return items;
	}

}
