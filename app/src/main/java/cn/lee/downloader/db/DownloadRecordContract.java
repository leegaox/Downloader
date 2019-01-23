package cn.lee.downloader.db;

import android.provider.BaseColumns;

/**
 * @author yanfa
 * @Title: {标题}
 * @Description:{描述}
 * @date 2019/1/23
 */
public final class DownloadRecordContract {
    // To prevent someone from accidentally instantiating the contract class,
    // make the constructor private.
    private DownloadRecordContract() {}

    /* Inner class that defines the table contents */
    public static class FeedEntry implements BaseColumns {
        public static final String TABLE_NAME = "download_record";
        public static final String COLUMN_NAME_URL = "url";
        public static final String COLUMN_NAME_PATH = "path";
        public static final String COLUMN_NAME_START_LOCATION ="start_location";
        public static final String COLUMN_NAME_END_LOCATION ="end_location";
        public static final String COLUMN_NAME_TOTAL_LENGTH = "total_length";
        public static final String COLUMN_NAME_STATE = "state";
    }
}
