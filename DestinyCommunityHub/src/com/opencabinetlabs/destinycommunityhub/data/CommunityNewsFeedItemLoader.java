package com.opencabinetlabs.destinycommunityhub.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import com.opencabinetlabs.destinycommunityhub.api.CommunityRssApi;
import com.opencabinetlabs.destinycommunityhub.model.NewsFeedItem;
import com.opencabinetlabs.destinycommunityhub.util.DaoUtils;
import com.opencabinetlabs.destinycommunityhub.util.ProviderUtils;

/**
 * Loader which retrieves results from the local database and transforms
 * them into a format useable by the app.
 */
public class CommunityNewsFeedItemLoader extends AsyncLoader<List<NewsFeedItem>> {

    /**
     * Comparator which sorts by forecast date
     */
    private static Comparator<NewsFeedItem> DATE_COMPARATOR = new Comparator<NewsFeedItem>() {
        @Override
        public int compare(final NewsFeedItem lhs, final NewsFeedItem rhs) {
        	return Long.valueOf(rhs.getDate()).compareTo(Long.valueOf(lhs.getDate()));
        }
    };

    public CommunityNewsFeedItemLoader(final Context context) {
        super(context);
    }

    @Override
    protected Uri getContentUri() {
        return DestinyCommunityHubContentProvider.COMMUNITY_NEWS_FEED_LOADER_URI;
    }

    @Override
    public List<NewsFeedItem> loadInBackground() {
        Cursor cursor = null;
        try {
            cursor = ProviderUtils.query(DestinyCommunityHubContentProvider.COMMUNITY_NEWS_FEED_URI)
                    .sort(Db.Field.DATE + " DESC")
                    //.limit(CommunityRssApi.MAX_NUM_RESULTS)
                    .cursor(getContext());
            if (cursor != null && cursor.moveToFirst()) {
                final List<NewsFeedItem> retval = new ArrayList<NewsFeedItem>(cursor.getCount());
                do {
                    retval.add(DaoUtils.build(NewsFeedItem.class, cursor));
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
