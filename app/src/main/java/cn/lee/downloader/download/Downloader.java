package cn.lee.downloader.download;

import android.content.Context;

import java.io.File;
import java.util.HashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import cn.lee.downloader.db.SQLiteOpenHelper;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;

/**
 * @author yanfa
 * @Title: {下载器}
 * @Description:{描述}
 * @date 2019/1/24
 */
public class Downloader {


    private final static String TAG = "Downloader";

    private static final int DEFAULT_TIMEOUT = 15;

    private String savePath;
    private String saveName;
    private int connectTimeout = DEFAULT_TIMEOUT;
    private int readTimeout = DEFAULT_TIMEOUT;
    private int callTimeout = DEFAULT_TIMEOUT;


    private DownloadListener listener;
    private DownloadApi downloadApi;
    private Object lockA = new Object();
    private SQLiteOpenHelper db;

    /***下载中的任务集***/
    private HashMap<String, DownloadTask> downloadingTask;
    /*** 最大下载任务数***/
    private int taskThreshold = 3;

    /**
     * 私有构造函数，通过Builder 进行构造。
     *
     * @param builder
     */
    private Downloader(Builder builder) {
        downloadingTask = new HashMap<>();
        this.savePath = builder.savePath;
        this.connectTimeout = builder.connectTimeout == 0 ? DEFAULT_TIMEOUT : builder.connectTimeout;
        this.readTimeout = builder.readTimeout == 0 ? DEFAULT_TIMEOUT : builder.readTimeout;
        this.callTimeout = builder.callTimeout == 0 ? DEFAULT_TIMEOUT : builder.callTimeout;
        this.taskThreshold = builder.taskThreshold == 0 ? 3 : builder.taskThreshold;
    }

    /**
     * 初始化httpClient
     */
    public DownloadApi getDownloadApi() {
        synchronized (lockA) {
            if (downloadApi == null) {
                synchronized (lockA) {
                    OkHttpClient httpClient = new OkHttpClient.Builder()
                            .retryOnConnectionFailure(true)
                            .connectTimeout(connectTimeout, TimeUnit.SECONDS)
                            .readTimeout(readTimeout, TimeUnit.SECONDS)
                            .callTimeout(callTimeout, TimeUnit.SECONDS)
                            .build();
                    Retrofit retrofit = new Retrofit.Builder()
                            .baseUrl("https://www.baidu.com/")
                            //指定异步请求最大数
                            .callbackExecutor(Executors.newFixedThreadPool(taskThreshold))
                            .client(httpClient)
                            .build();
                    downloadApi = retrofit.create(DownloadApi.class);
                }
            }
            return downloadApi;
        }
    }

    public void download(Context context, String url, String saveName, DownloadListener listener) {
        if (db == null) {
            db = new SQLiteOpenHelper(context);
        }
        this.saveName = saveName;
        this.listener = listener;
        handleDownload(url);
    }

    private void handleDownload(String url) {
        if (!downloadingTask.containsKey(url)) {
            DownloadTask task = new DownloadTask();
            downloadingTask.put(url, task);
            task.download(this, url, savePath + File.separator + saveName, listener);
        } else {
            DownloadTask task = getTask(url);
            task.resume();
        }
    }

    public SQLiteOpenHelper getDB() {
        return db;
    }

    private DownloadTask getTask(String url) {
        if (downloadingTask != null) {
            return downloadingTask.get(url);
        }
        return null;
    }

    public boolean isDownloading(String url) {
        DownloadTask task = getTask(url);
        if (task != null && task.isDownloading()) {
            return true;
        }
        return false;
    }

    public void pause(String url) {
        DownloadTask task = getTask(url);
        if (task != null) {
            task.pause();
        }
    }

    /**
     * 取消下载
     *
     * @param url
     */
    public void cancel(String url) {
        DownloadTask task = getTask(url);
        if (task != null) {
            task.cancel();
            File file = new File(task.getEntity().getPath());
            if (file.exists()) {
                file.delete();
            }
        }
        removeTask(url);
    }

    private void removeTask(String url) {
        if (downloadingTask != null && downloadingTask.containsKey(url)) {
            downloadingTask.remove(url);
        }
    }


    public void onDestory() {
        if (db != null) {
            db.close();
        }
    }

    /**
     * 建造者模式创建Downloader实例
     */
    public static class Builder {
        private String savePath;
        private int connectTimeout;
        private int readTimeout;
        private int callTimeout;
        private int taskThreshold;


        public Builder savePath(String savePath) {
            this.savePath = savePath;
            return this;
        }


        public Builder connectTimeout(int connectTimeout) {
            this.connectTimeout = connectTimeout;
            return this;
        }

        public Builder readTimeout(int readTimeout) {
            this.readTimeout = readTimeout;
            return this;
        }

        public Builder callTimeout(int callTimeout) {
            this.callTimeout = callTimeout;
            return this;
        }

        public Builder taskThreshold(int taskThreshold) {
            this.taskThreshold = taskThreshold;
            return this;
        }

        public Downloader build() {
            return new Downloader(this);
        }
    }

}
