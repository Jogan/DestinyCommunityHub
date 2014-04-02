package com.opencabinetlabs.destinycommunityhub.dao;

import static com.opencabinetlabs.destinycommunityhub.data.Db.Field.DATE;
import static com.opencabinetlabs.destinycommunityhub.data.Db.Field.DATE_STR;
import static com.opencabinetlabs.destinycommunityhub.data.Db.Field.ID;
import static com.opencabinetlabs.destinycommunityhub.data.Db.Field.PODCAST_DESCRIPTION;
import static com.opencabinetlabs.destinycommunityhub.data.Db.Field.PODCAST_LENGTH;
import static com.opencabinetlabs.destinycommunityhub.data.Db.Field.PODCAST_TITLE;
import static com.opencabinetlabs.destinycommunityhub.data.Db.Field.PODCAST_AUTHOR;
import static com.opencabinetlabs.destinycommunityhub.data.Db.Field.PODCAST_URL;

import java.util.Map;

import android.content.ContentValues;
import android.database.Cursor;

import com.opencabinetlabs.destinycommunityhub.model.PodcastFeedItem;
import com.opencabinetlabs.destinycommunityhub.model.PodcastFeedItem.PodcastInfo;
import com.opencabinetlabs.destinycommunityhub.util.ContentValuesBuilder;

public class PodcastFeedItemDao extends BaseDao implements IDao<PodcastFeedItem> {
	/**
	 * Holds a mapping from column name --> index so we can avoid
	 * doing repeated lookups
	 */
	private Map<String, Integer> mCursorCols;

	@Override
	public PodcastFeedItem build(final Cursor cursor) {
		if (mCursorCols == null) {
			mCursorCols = buildCursorCols(cursor);
		}

		final PodcastFeedItem f = new PodcastFeedItem();
		f.setId(cursor.getLong(mCursorCols.get(ID.getName())));
		f.setPubDate(cursor.getString(mCursorCols.get(DATE_STR.getName())));
		f.setDescription(cursor.getString(mCursorCols.get(PODCAST_DESCRIPTION.getName())));
		f.setDate(cursor.getLong(mCursorCols.get(DATE.getName())));
		f.setTitle(cursor.getString(mCursorCols.get(PODCAST_TITLE.getName())));
		f.setAuthor(cursor.getString(mCursorCols.get(PODCAST_AUTHOR.getName())));
		final PodcastInfo i = new PodcastInfo();
		i.setUrl(cursor.getString(mCursorCols.get(PODCAST_URL.getName())));
		i.setLength(cursor.getString(mCursorCols.get(PODCAST_LENGTH.getName())));
		f.setInfo(i);
		return f;
	}

	@Override
	public ContentValues convert(final PodcastFeedItem podcastFeedItem) {
		final ContentValuesBuilder builder = new ContentValuesBuilder()
		.put(DATE_STR, podcastFeedItem.getPubDate())
		.put(DATE, podcastFeedItem.getDate())
		.put(PODCAST_TITLE, podcastFeedItem.getTitle())
		.put(PODCAST_DESCRIPTION, podcastFeedItem.getDescription())
		.put(PODCAST_AUTHOR,podcastFeedItem.getAuthor())
		.put(PODCAST_URL, podcastFeedItem.getInfo().getUrl())
		.put(PODCAST_LENGTH, podcastFeedItem.getInfo().getLength());

		
		if (podcastFeedItem.getId() > 0) {
			builder.put(ID, podcastFeedItem.getId());
		}

		return builder.build();
	}
}
