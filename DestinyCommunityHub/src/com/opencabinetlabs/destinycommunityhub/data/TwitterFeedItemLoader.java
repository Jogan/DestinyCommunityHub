package com.opencabinetlabs.destinycommunityhub.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import com.opencabinetlabs.destinycommunityhub.model.TwitterListResult.Status;
import com.opencabinetlabs.destinycommunityhub.util.DaoUtils;
import com.opencabinetlabs.destinycommunityhub.util.ProviderUtils;

public class TwitterFeedItemLoader extends AsyncLoader<List<Status>> {
	/**
     * Comparator which sorts by forecast date
     */
    private static Comparator<Status> DATE_COMPARATOR = new Comparator<Status>() {
        @Override
        public int compare(final Status lhs, final Status rhs) {
        	return Long.valueOf(rhs.getDate()).compareTo(Long.valueOf(lhs.getDate()));
        }
    };

    public TwitterFeedItemLoader(final Context context) {
        super(context);
    }

    @Override
    protected Uri getContentUri() {
        return DestinyCommunityHubContentProvider.TWITTER_FEED_LOADER_URI;
    }

    @Override
    public List<Status> loadInBackground() {
        Cursor cursor = null;
        try {
            cursor = ProviderUtils.query(DestinyCommunityHubContentProvider.TWITTER_FEED_URI)
                    .sort(Db.Field.DATE + " DESC")
                    //.limit(CommunityRssApi.MAX_NUM_RESULTS)
                    .cursor(getContext());
            if (cursor != null && cursor.moveToFirst()) {
                final List<Status> retval = new ArrayList<Status>(cursor.getCount());
                do {
                    retval.add(DaoUtils.build(Status.class, cursor));
                } while (cursor.moveToNext());

                Collections.sort(retval, DATE_COMPARATOR);

                return retval;
            }
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }

        return null;
    }
}
