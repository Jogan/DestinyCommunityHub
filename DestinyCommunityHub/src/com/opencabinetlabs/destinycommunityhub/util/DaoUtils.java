package com.opencabinetlabs.destinycommunityhub.util;

import javax.inject.Inject;

import android.content.ContentValues;
import android.database.Cursor;

import com.opencabinetlabs.destinycommunityhub.dao.CommunityRssDao;
import com.opencabinetlabs.destinycommunityhub.dao.IDao;
import com.opencabinetlabs.destinycommunityhub.dao.PodcastFeedItemDao;
import com.opencabinetlabs.destinycommunityhub.dao.TwitterFeedItemDao;
import com.opencabinetlabs.destinycommunityhub.model.NewsFeedItem;
import com.opencabinetlabs.destinycommunityhub.model.PodcastFeedItem;
import com.opencabinetlabs.destinycommunityhub.model.TwitterListResult.Status;

/**
 * Provides a single point of conversion for all dao-related objects
 */
public class DaoUtils {

    @Inject
    protected static CommunityRssDao sCommRssDao;
    
    @Inject
    protected static TwitterFeedItemDao sTwitterItemDao;
    
    @Inject
    protected static PodcastFeedItemDao sPodcastFeedItemDao;

    public static ContentValues convert(Object obj) {
        IDao dao = getDao(obj.getClass());
        return dao.convert(obj);
    }

    public static <T> T build(Class<T> cls, Cursor cursor) {
        IDao<T> dao = getDao(cls);
        return dao.build(cursor);
    }

    @SuppressWarnings("unchecked")
	public static <T> IDao<T> getDao(Class<T> cls) {
        if (cls.equals(NewsFeedItem.class)) {
            return (IDao<T>) sCommRssDao;
        }else if(cls.equals(Status.class)){
        	return (IDao<T>) sTwitterItemDao;
        }else if(cls.equals(PodcastFeedItem.class)){
        	return (IDao<T>) sPodcastFeedItemDao;
        }
        else {
            throw new IllegalArgumentException("No Dao for class: " + cls.getSimpleName());
        }
    }

}
