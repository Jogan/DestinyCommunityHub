package com.opencabinetlabs.destinycommunityhub.dao;

import static com.opencabinetlabs.destinycommunityhub.data.Db.Field.ARTICLE_DESC;
import static com.opencabinetlabs.destinycommunityhub.data.Db.Field.ARTICLE_TITLE;
import static com.opencabinetlabs.destinycommunityhub.data.Db.Field.ARTICLE_URL;
import static com.opencabinetlabs.destinycommunityhub.data.Db.Field.AUTHOR;
import static com.opencabinetlabs.destinycommunityhub.data.Db.Field.DATE;
import static com.opencabinetlabs.destinycommunityhub.data.Db.Field.DATE_STR;
import static com.opencabinetlabs.destinycommunityhub.data.Db.Field.ID;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import android.content.ContentValues;
import android.database.Cursor;

import com.opencabinetlabs.destinycommunityhub.model.NewsFeedItem;
import com.opencabinetlabs.destinycommunityhub.util.ContentValuesBuilder;


/**
 * Performs java object --> database conversion for {@link com.opencabinetlabs.destinycommunityhub.model.NewsFeedItem}
 * objects (and visa-versa)
 */
public class CommunityRssDao extends BaseDao implements IDao<NewsFeedItem> {

    /**
     * Holds a mapping from column name --> index so we can avoid
     * doing repeated lookups
     */
    private Map<String, Integer> mCursorCols;

    @Override
    public NewsFeedItem build(final Cursor cursor) {
        if (mCursorCols == null) {
            mCursorCols = buildCursorCols(cursor);
        }

        final NewsFeedItem f = new NewsFeedItem();
        f.setId(cursor.getLong(mCursorCols.get(ID.getName())));
        f.setPubDate(cursor.getString(mCursorCols.get(DATE_STR.getName())));
        f.setDescription(cursor.getString(mCursorCols.get(ARTICLE_DESC.getName())));
        f.setDate(cursor.getLong(mCursorCols.get(DATE.getName())));
        f.setTitle(cursor.getString(mCursorCols.get(ARTICLE_TITLE.getName())));
        f.setAuthor(cursor.getString(mCursorCols.get(AUTHOR.getName())));
        f.setLink(cursor.getString(mCursorCols.get(ARTICLE_URL.getName())));
        
        return f;
    }

    @Override
    public ContentValues convert(final NewsFeedItem newsFeedItem) {
        final ContentValuesBuilder builder = new ContentValuesBuilder()
                .put(DATE_STR, newsFeedItem.getPubDate())
                .put(DATE, newsFeedItem.getDate())
                .put(ARTICLE_TITLE, newsFeedItem.getTitle())
                .put(ARTICLE_DESC, newsFeedItem.getDescription())
                .put(AUTHOR, newsFeedItem.getAuthor())
                .put(ARTICLE_URL, newsFeedItem.getLink());
                

        if (newsFeedItem.getId() > 0) {
            builder.put(ID, newsFeedItem.getId());
        }

        return builder.build();
    }

    
}
