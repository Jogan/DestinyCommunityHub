package com.opencabinetlabs.destinycommunityhub.util;

import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;

/**
 * 
 * @author Ben Cull
 * 
 *         from http://benjii.me/2010/08/endless-scrolling-listview-in-android/
 * 
 */

public class EndlessScrollListener implements OnScrollListener {

	private int visibleThreshold = 5;
	private int previousTotal = 0;
	private boolean loading = true;
	private OnEndReachedListener listener;

	public EndlessScrollListener() {
	}

	public EndlessScrollListener(int visibleThreshold) {
		this.visibleThreshold = visibleThreshold;
	}

	public void setOnEndReachedListener(OnEndReachedListener listener) {
		this.listener = listener;
	}

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
		if (loading) {
			if (totalItemCount > previousTotal) {
				loading = false;
				previousTotal = totalItemCount;
			}
		}
		if (!loading && (totalItemCount - visibleItemCount) <= (firstVisibleItem + visibleThreshold)) {
			if (listener != null) {
				listener.onEndReached();
			}
			loading = true;
		}
	}

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
	}

	public interface OnEndReachedListener {
		public void onEndReached();
	}
}