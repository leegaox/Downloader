package cn.lee.downloader.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.provider.BaseColumns;

import cn.lee.downloader.db.DownloadRecordContract.FeedEntry;

/**
 * @author Lee
 * @Title: {标题}
 * @Description:{描述}
 * @date 2019/1/23
 */
public class SQLiteOpenHelper {

    // Gets the data repository in write mode
    private SQLiteDatabase db;

    DownloadDbHelper dbHelper;

    public SQLiteOpenHelper(Context context) {
        dbHelper = new DownloadDbHelper(context);
        dbHelper.getReadableDatabase();
        db =dbHelper.getWritableDatabase();
    }


    public void insertRecord(DownloadEntity entity) {
        // Create a new map of values, where column names are the keys
        ContentValues values = new ContentValues();
        values.put(FeedEntry.COLUMN_NAME_URL, entity.downloadUrl);
        values.put(FeedEntry.COLUMN_NAME_PATH, entity.path);
        values.put(FeedEntry.COLUMN_NAME_START_LOCATION, entity.startLocation);
        values.put(FeedEntry.COLUMN_NAME_END_LOCATION, entity.endLocation);
        values.put(FeedEntry.COLUMN_NAME_TOTAL_LENGTH, entity.fileSize);
        values.put(FeedEntry.COLUMN_NAME_STATE, entity.state);
        // Insert the new row, returning the primary key value of the new row
        long newRowId = db.insert(DownloadRecordContract.FeedEntry.TABLE_NAME, null, values);
    }

    public DownloadEntity queryRecord(String path) {
        DownloadEntity record = null;

        // Define a projection that specifies which columns from the database
        // you will actually use after this query.
        String[] projection = {
                BaseColumns._ID,
                FeedEntry.COLUMN_NAME_URL,
                FeedEntry.COLUMN_NAME_PATH,
                FeedEntry.COLUMN_NAME_START_LOCATION,
                FeedEntry.COLUMN_NAME_END_LOCATION,
                FeedEntry.COLUMN_NAME_TOTAL_LENGTH,
                FeedEntry.COLUMN_NAME_STATE,
        };

        // Filter results WHERE "title" = 'My Title'
        String selection = FeedEntry.COLUMN_NAME_PATH + " = ?";
        String[] selectionArgs = {path};

        // How you want the results sorted in the resulting Cursor
        String sortOrder =
                FeedEntry._ID + " DESC";

        Cursor cursor = db.query(
                FeedEntry.TABLE_NAME,   // The table to query
                projection,             // The array of columns to return (pass null to get all)
                selection,              // The columns for the WHERE clause
                selectionArgs,          // The values for the WHERE clause
                null,         // don't group the rows
                null,           // don't filter by row groups
                sortOrder               // The sort order
        );
        while (cursor.moveToNext()) {
            String downloadUrl = cursor.getString(cursor.getColumnIndexOrThrow(FeedEntry.COLUMN_NAME_URL));
            String savePath = cursor.getString(cursor.getColumnIndexOrThrow(FeedEntry.COLUMN_NAME_PATH));
            long startLocation = cursor.getLong(cursor.getColumnIndexOrThrow(FeedEntry.COLUMN_NAME_START_LOCATION));
            long endLocation = cursor.getLong(cursor.getColumnIndexOrThrow(FeedEntry.COLUMN_NAME_END_LOCATION));
            long fileSize = cursor.getLong(cursor.getColumnIndexOrThrow(FeedEntry.COLUMN_NAME_TOTAL_LENGTH));
            int state = cursor.getInt(cursor.getColumnIndexOrThrow(FeedEntry.COLUMN_NAME_STATE));
            record = new DownloadEntity(fileSize, downloadUrl, savePath, startLocation, endLocation, state);
        }
        return record;
    }


    public void deleteRecord(String path) {
        // Define 'where' part of query.
        String selection = FeedEntry.COLUMN_NAME_PATH + " LIKE ?";
        // Specify arguments in placeholder order.
        String[] selectionArgs = {path};
        // Issue SQL statement.
        int deletedRows = db.delete(FeedEntry.TABLE_NAME, selection, selectionArgs);
    }

    public void updateDownloadProgress(String path, long startLocation) {
        ContentValues values = new ContentValues();
        values.put(FeedEntry.COLUMN_NAME_START_LOCATION, startLocation);     // New value for one column

        // Which row to update, based on the title
        String selection = FeedEntry.COLUMN_NAME_PATH + " LIKE ?";
        String[] selectionArgs = {path};

        int count = db.update(
                FeedEntry.TABLE_NAME,
                values,
                selection,
                selectionArgs);
    }

    /**
     * @param path  文件保存路径
     * @param state 0:下载中；1：成功 ；-1：失败
     */
    public void updateDownloadState(String path, int state) {
        ContentValues values = new ContentValues();
        // New value for one column
        values.put(FeedEntry.COLUMN_NAME_STATE, state);

        // Which row to update, based on the title
        String selection = FeedEntry.COLUMN_NAME_PATH + " LIKE ?";
        String[] selectionArgs = {path};

        int count = db.update(
                FeedEntry.TABLE_NAME,
                values,
                selection,
                selectionArgs);
    }


    public void close() {
        if (dbHelper != null) {
            dbHelper.close();
        }
    }

}
