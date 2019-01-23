package cn.lee.downloader.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import cn.lee.downloader.db.DownloadRecordContract.FeedEntry;


/**
 * @author yanfa
 * @Title: {标题}
 * @Description:{描述}
 * @date 2019/1/23
 */
public class DownloadDbHelper extends SQLiteOpenHelper {
    private static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + DownloadRecordContract.FeedEntry.TABLE_NAME + " (" +
                    FeedEntry._ID + " INTEGER PRIMARY KEY," +
                    FeedEntry.COLUMN_NAME_URL + " TEXT," +
                    FeedEntry.COLUMN_NAME_PATH + " TEXT," +
                    FeedEntry.COLUMN_NAME_START_LOCATION + " LONG," +
                    FeedEntry.COLUMN_NAME_END_LOCATION + " LONG," +
                    FeedEntry.COLUMN_NAME_TOTAL_LENGTH + " LONG," +
                    FeedEntry.COLUMN_NAME_STATE + " INTEGER)";

    private static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + DownloadRecordContract.FeedEntry.TABLE_NAME;

    // If you change the database schema, you must increment the database version.
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "download_record.db";

    public DownloadDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_ENTRIES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        db.execSQL(SQL_DELETE_ENTRIES);
        onCreate(db);
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }

}
