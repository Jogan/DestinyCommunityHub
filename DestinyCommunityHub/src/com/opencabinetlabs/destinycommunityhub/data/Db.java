package com.opencabinetlabs.destinycommunityhub.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.opencabinetlabs.destinycommunityhub.CommunityHubConfig;

import timber.log.Timber;

import java.lang.reflect.Modifier;

public class Db extends SQLiteOpenHelper {
    private static final int VERSION = 1;
    public static final String DB_NAME = "destinycommunityhub.db";

    private static Db sInstance;

    public static final Object[] LOCK = new Object[0];

    private Context mContext;

    public static Db get(Context c) {
        if (sInstance == null)
            sInstance = new Db(c);

        return sInstance;
    }

    protected Db(Context context) {
        super(context.getApplicationContext(), DB_NAME, null, VERSION);

        mContext = context;
    }

    @Override
    public void onCreate(final SQLiteDatabase db) {
        runForEachTable(new TableRunnable() {
            @Override
            public void run(final DbTable table) {
                db.execSQL(table.getCreateSql());
            }
        });
    }

    @Override
    public void onUpgrade(final SQLiteDatabase db, int oldVersion, int newVersion) {
        runForEachTable(new TableRunnable() {
            @Override
            public void run(final DbTable table) {
                db.execSQL(table.getDropSql());
            }
        });
        // create new tables
        onCreate(db);

        // Force the API delay...This is hacky,ugly and crude...
        SharedPreferences mPrefs = mContext.getSharedPreferences(CommunityHubConfig.PREFS_NAME, Context.MODE_PRIVATE);
        mPrefs.edit().putLong("lastTimeApiRequestsMade",mPrefs.getLong("lastTimeApiRequestsMade",0) - CommunityHubConfig.API_DELAY).commit();

        // throw new IllegalStateException("We havent implemented db upgrades yet!");
    }

    @Override
    public SQLiteDatabase getReadableDatabase() {
        synchronized (LOCK) {
            return super.getReadableDatabase();
        }
    }

    @Override
    public SQLiteDatabase getWritableDatabase() {
        synchronized (LOCK) {
            return super.getWritableDatabase();
        }
    }

    /**
     * Execute a given against each table in the database
     *
     * @param runnable The task to perform
     */
    private void runForEachTable(TableRunnable runnable) {
        java.lang.reflect.Field[] declaredFields = Db.Table.class.getDeclaredFields();
        for (java.lang.reflect.Field field : declaredFields) {
            if (Modifier.isStatic(field.getModifiers()) && field.getType().equals(DbTable.class)) {
                try {
                    runnable.run((DbTable) field.get(null));
                } catch (IllegalAccessException e) {
                    Timber.e(e, "Error executing table runnable: " + field.getName());
                }
            }
        }
    }

    /**
     * Encapsulates a task to be run against a table
     */
    private interface TableRunnable {
        /**
         * Execute the request task
         *
         * @param table The table to execute the task on
         */
        public void run(DbTable table);
    }

    /**
     * Database fields used in the app
     */
    public static class Field {
        private Field() {
        }

        public static final DbField ID = new DbField("_id", "integer", "primary key");
        public static final DbField DATE_STR = new DbField("date_str", "text");
        public static final DbField DATE = new DbField("date", "integer");
        
        /* Community News Feed Fields */
        public static final DbField ARTICLE_TITLE = new DbField("title", "text");
        public static final DbField ARTICLE_DESC = new DbField("desc", "text");
        public static final DbField AUTHOR = new DbField("author", "text");
        public static final DbField ARTICLE_URL = new DbField("url", "text");
        
        /* Twitter Feed Fields */
        public static final DbField TWEET_TEXT = new DbField("text", "text");
        public static final DbField TWEET_USERNAME = new DbField("username", "text");
        public static final DbField TWEET_NAME = new DbField("name", "text");
        public static final DbField TWEET_DESCRIPTION = new DbField("description", "text");
        public static final DbField TWEET_PROFILE_IMG_URL = new DbField("img_url", "text");
        
        /* Podcast Feed Fields */
        public static final DbField PODCAST_AUTHOR = new DbField("author", "text"); // or title
        public static final DbField PODCAST_TITLE = new DbField("title", "text");
        public static final DbField PODCAST_DESCRIPTION = new DbField("description", "text");
        public static final DbField PODCAST_URL = new DbField("url", "text");
        public static final DbField PODCAST_LENGTH = new DbField("length", "text");
        
    }

    /**
     * Application database tables
     */
    public static class Table {
        private Table() {
        }

        /* Community News Feed Table */
        public static final DbTable COMMUNITY_NEWS = DbTable.with("community_news_feed")
                .columns(Db.Field.ID,
                        Db.Field.DATE_STR,
                        Db.Field.DATE,
                        Db.Field.ARTICLE_TITLE,
                        Db.Field.ARTICLE_DESC,
                        Db.Field.AUTHOR,
                        Db.Field.ARTICLE_URL)
                .scripts("CREATE UNIQUE INDEX unique_community_article_by_date ON community_news_feed(" + Db.Field.DATE_STR + ")")
                .create();
        
        /* Twitter Feed Table */
        public static final DbTable TWITTER_FEED = DbTable.with("twitter_feed")
                .columns(Db.Field.ID,
                        Db.Field.DATE_STR,
                        Db.Field.DATE,
                        Db.Field.TWEET_TEXT,
                        Db.Field.TWEET_USERNAME,
                        Db.Field.TWEET_NAME,
                        Db.Field.TWEET_DESCRIPTION,
                        Db.Field.TWEET_PROFILE_IMG_URL)
                .scripts("CREATE UNIQUE INDEX unique_tweet_by_date ON twitter_feed(" + Db.Field.DATE_STR + ")")
                .create();
        
        /* Podcast Feed Table */
        public static final DbTable PODCAST_FEED = DbTable.with("podcast_feed")
                .columns(Db.Field.ID,
                        Db.Field.DATE_STR,
                        Db.Field.DATE,
                        Db.Field.PODCAST_AUTHOR,
                        Db.Field.PODCAST_TITLE,
                        Db.Field.PODCAST_DESCRIPTION,
                        Db.Field.PODCAST_URL,
                        Db.Field.PODCAST_LENGTH)
                .scripts("CREATE UNIQUE INDEX unique_podcast_by_date ON podcast_feed(" + Db.Field.DATE_STR + ")")
                .create();
    }
}