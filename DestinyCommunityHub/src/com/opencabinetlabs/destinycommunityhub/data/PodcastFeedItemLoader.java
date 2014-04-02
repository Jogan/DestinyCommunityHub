package com.opencabinetlabs.destinycommunityhub.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import com.opencabinetlabs.destinycommunityhub.model.PodcastFeedItem;
import com.opencabinetlabs.destinycommunityhub.util.DaoUtils;
import com.opencabinetlabs.destinycommunityhub.util.ProviderUtils;

public class PodcastFeedItemLoader extends AsyncLoader<List<PodcastFeedItem>> {
	/**
     * Comparator which sorts by forecast date
     */
    private static Comparator<PodcastFeedItem> DATE_COMPARATOR = new Comparator<PodcastFeedItem>() {
        @Override
        public int compare(final PodcastFeedItem lhs, final PodcastFeedItem rhs) {
        	return Long.valueOf(rhs.getDate()).compareTo(Long.valueOf(lhs.getDate()));
        }
    };

    public PodcastFeedItemLoader(final Context context) {
        super(context);
    }

    @Override
    protected Uri getContentUri() {
        return DestinyCommunityHubContentProvider.PODCAST_FEED_LOADER_URI;
    }

    @Override
    public List<PodcastFeedItem> loadInBackground() {
        Cursor cursor = null;
        try {
            cursor = ProviderUtils.query(DestinyCommunityHubContentProvider.PODCAST_FEED_URI)
                    .sort(Db.Field.DATE + " DESC")
                    .cursor(getContext());
            if (cursor != null && cursor.moveToFirst()) {
                final List<PodcastFeedItem> retval = new ArrayList<PodcastFeedItem>(cursor.getCount());
                do {
                    retval.add(DaoUtils.build(PodcastFeedItem.class, cursor));
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
