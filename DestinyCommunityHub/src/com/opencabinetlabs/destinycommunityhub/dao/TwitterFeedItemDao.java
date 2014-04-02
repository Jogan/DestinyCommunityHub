package com.opencabinetlabs.destinycommunityhub.dao;

import static com.opencabinetlabs.destinycommunityhub.data.Db.Field.TWEET_DESCRIPTION;
import static com.opencabinetlabs.destinycommunityhub.data.Db.Field.TWEET_NAME;
import static com.opencabinetlabs.destinycommunityhub.data.Db.Field.TWEET_PROFILE_IMG_URL;
import static com.opencabinetlabs.destinycommunityhub.data.Db.Field.TWEET_TEXT;
import static com.opencabinetlabs.destinycommunityhub.data.Db.Field.TWEET_USERNAME;
import static com.opencabinetlabs.destinycommunityhub.data.Db.Field.DATE;
import static com.opencabinetlabs.destinycommunityhub.data.Db.Field.DATE_STR;
import static com.opencabinetlabs.destinycommunityhub.data.Db.Field.ID;

import java.util.Map;

import android.content.ContentValues;
import android.database.Cursor;

import com.opencabinetlabs.destinycommunityhub.model.TwitterListResult;
import com.opencabinetlabs.destinycommunityhub.model.TwitterListResult.Status;
import com.opencabinetlabs.destinycommunityhub.model.TwitterListResult.User;
import com.opencabinetlabs.destinycommunityhub.util.ContentValuesBuilder;

public class TwitterFeedItemDao extends BaseDao implements IDao<TwitterListResult.Status> {

	/**
     * Holds a mapping from column name --> index so we can avoid
     * doing repeated lookups
     */
    private Map<String, Integer> mCursorCols;
	
	@Override
	public Status build(Cursor cursor) {
		if (mCursorCols == null) {
            mCursorCols = buildCursorCols(cursor);
        }

        final Status s = new Status();
        s.setId(cursor.getLong(mCursorCols.get(ID.getName())));
        s.setPubDate(cursor.getString(mCursorCols.get(DATE_STR.getName())));
        s.setText(cursor.getString(mCursorCols.get(TWEET_TEXT.getName())));
        final User u = new User();
        u.setName(cursor.getString(mCursorCols.get(TWEET_USERNAME.getName())));
        u.setNickName(cursor.getString(mCursorCols.get(TWEET_NAME.getName())));
        u.setDescription(cursor.getString(mCursorCols.get(TWEET_DESCRIPTION.getName())));
        u.setUrl(cursor.getString(mCursorCols.get(TWEET_PROFILE_IMG_URL.getName())));
        s.setUser(u);
        
        return s;
	}

	@Override
    public ContentValues convert(final Status status) {
        final ContentValuesBuilder builder = new ContentValuesBuilder()
        		.put(ID, status.getId())
                .put(DATE_STR, status.getPubDate())
                .put(DATE, status.getDate())
                .put(TWEET_TEXT, status.getText())
                .put(TWEET_USERNAME, status.getUser().getName())
                .put(TWEET_NAME, status.getUser().getNickName())
                .put(TWEET_PROFILE_IMG_URL, status.getUser().getUrl())
                .put(TWEET_DESCRIPTION,status.getUser().getDescription());
               



        return builder.build();
    }

}
