package com.opencabinetlabs.destinycommunityhub.dao;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import android.database.Cursor;

public class BaseDao {
	/**
     * Populates a mapping of column name --> index
     *
     * @param cursor The cursor to extract
     * @return The newly created mapping
     */
    protected static Map<String, Integer> buildCursorCols(final Cursor cursor) {
        Map<String, Integer> retval = new ConcurrentHashMap<String, Integer>();
        for (int i = 0, len = cursor.getColumnCount(); i < len; i++) {
            retval.put(cursor.getColumnName(i), i);
        }

        return retval;
    }
}
