package com.opencabinetlabs.destinycommunityhub.data;

import java.sql.SQLException;

import timber.log.Timber;
import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;

import com.opencabinetlabs.destinycommunityhub.util.ProviderUtils;

/**
 * Provides access to the underlying datastore
 */
public class DestinyCommunityHubContentProvider extends ContentProvider {

    private static final String AUTHORITY = "com.opencabinetlabs.destinycommunityhub.data.DestinyCommunityHubContentProvider";

    private static final Uri BASE_URI = Uri.parse("content://" + AUTHORITY);

    public static final Uri COMMUNITY_NEWS_FEED_LOADER_URI = Uri.withAppendedPath(BASE_URI, "community_news_feed_loader");
    public static final Uri TWITTER_FEED_LOADER_URI = Uri.withAppendedPath(BASE_URI, "twitter_feed_loader");
    public static final Uri PODCAST_FEED_LOADER_URI = Uri.withAppendedPath(BASE_URI, "podcast_feed_loader");
    
    public static final int TYPE_COMMUNITY_NEWS = 0x1;
    public static final int TYPE_TWITTER_FEED = 0x2;
    public static final int TYPE_PODCAST_FEED = 0x3;

    public static Uri COMMUNITY_NEWS_FEED_URI;
    public static Uri TWITTER_FEED_URI;
    public static Uri PODCAST_FEED_URI;

    private static final UriMatcher sURIMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    private static Db mDb;

    private ContentResolver mContentResolver;

    static {
        COMMUNITY_NEWS_FEED_URI = Uri.withAppendedPath(BASE_URI, "community_news_feed");
        TWITTER_FEED_URI = Uri.withAppendedPath(BASE_URI, "twitter_feed");
        PODCAST_FEED_URI = Uri.withAppendedPath(BASE_URI, "podcast_feed");

        sURIMatcher.addURI(AUTHORITY, "community_news_feed", TYPE_COMMUNITY_NEWS);
        sURIMatcher.addURI(AUTHORITY, "twitter_feed", TYPE_TWITTER_FEED);
        sURIMatcher.addURI(AUTHORITY, "podcast_feed", TYPE_PODCAST_FEED);
    }

    @Override
    public boolean onCreate() {
        mDb = Db.get(getContext());
        mContentResolver = getContext().getContentResolver();
        return true;
    }

    @Override
    public String getType(final Uri uri) {
        return sURIMatcher.match(uri) == UriMatcher.NO_MATCH ?
                null : uri.toString();
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String sel, String[] selArgs, String sort) {
        try {
            SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
            qb.setTables(getTableFromType(sURIMatcher.match(uri)).getName());

            Cursor cursor = qb.query(mDb.getReadableDatabase(),
                    projection, sel, selArgs, null, null, sort, uri.getQueryParameter("limit"));

            if (cursor != null) {
                cursor.setNotificationUri(mContentResolver, uri);
            }
            return cursor;
        } catch (Exception e) {
            Timber.e(e, "Error quering database");
        }

        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        final SQLiteDatabase db = mDb.getWritableDatabase();

        try {
            final int type = sURIMatcher.match(uri);
            final DbTable table = getTableFromType(type);
            final DbField[] upsertFields = getUpsertFieldsFromType(type);

            final long id;
            if (upsertFields == null || upsertFields.length == 0) {
                id = db.replaceOrThrow(table.getName(), null, values);
            } else {
                id = ProviderUtils.upsert(db, table, values, upsertFields);
            }

            if (id >= 0) {
                Uri newUri = ContentUris.withAppendedId(uri, id);
                mContentResolver.notifyChange(uri, null);
                return newUri;
            } else {
                throw new SQLException("Failed to insert row into " + uri);
            }

        } catch (Exception e) {
            Timber.e(e, "Error inserting into database");
        }

        return null;
    }

    @Override
    public int delete(Uri uri, String sel, String[] selArgs) {
        try {
            final int type = sURIMatcher.match(uri);
            final SQLiteDatabase db = mDb.getWritableDatabase();
            int rowsAffected = db.delete(getTableFromType(type).getName(), sel, selArgs);

            mContentResolver.notifyChange(uri, null);
            return rowsAffected;
        } catch (Exception e) {
            Timber.e(e, "Error deleting from database");
        }

        return 0;
    }

    @Override
    public int update(Uri uri, ContentValues values, String sel, String[] selArgs) {
        try {
            final int type = sURIMatcher.match(uri);
            final SQLiteDatabase db = mDb.getWritableDatabase();
            final int rowsAffected = db.update(getTableFromType(type).getName(), values, sel, selArgs);

            mContentResolver.notifyChange(uri, null);
            return rowsAffected;
        } catch (Exception e) {
            Timber.e(e, "Error updating database");
        }

        return 0;
    }

    private DbField[] getUpsertFieldsFromType(int type) {
        switch (type) {
            case TYPE_COMMUNITY_NEWS:
                return new DbField[]{Db.Field.DATE_STR};
            case TYPE_TWITTER_FEED:
            	return new DbField[]{Db.Field.DATE_STR};
            case TYPE_PODCAST_FEED:
            	return new DbField[]{Db.Field.DATE_STR};
            default:
                return null;
        }
    }

    private DbTable getTableFromType(int type) {
        switch (type) {
            case TYPE_COMMUNITY_NEWS:
                return Db.Table.COMMUNITY_NEWS;
            case TYPE_TWITTER_FEED:
            	return Db.Table.TWITTER_FEED;
            case TYPE_PODCAST_FEED:
            	return Db.Table.PODCAST_FEED;
            default:
                throw new IllegalArgumentException("Unrecognised uri type: " + type);
        }
    }
}
