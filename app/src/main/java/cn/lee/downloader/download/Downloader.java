package cn.lee.downloader.download;

import android.content.Context;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import cn.lee.downloader.db.DownloadEntity;
import cn.lee.downloader.db.SQLiteOpenHelper;
import cn.lee.downloader.util.Util;
import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

/**
 * @author yanfa
 * @Title: {下载器}
 * @Description:{描述}
 * @date 2019/1/22
 */
public class Downloader {

    private final static String TAG = "Downloader";
    private static final int DEFAULT_TIMEOUT = 15;
    private static final int DEFAULT_THREAD_NUM = 3;

    private Call<ResponseBody> mCall;
    private DownloadApi downloadApi;
    private DownloadListener listener;
    private DownloadEntity entity;
    private SQLiteOpenHelper db;

    private String downloadUrl;
    /***下载文件存储路径***/
    private String savePath;
    private String saveName;
    private int threadNum = DEFAULT_THREAD_NUM;
    private int connectTimeout = DEFAULT_TIMEOUT;
    private int readTimeout = DEFAULT_TIMEOUT;
    private int callTimeout = DEFAULT_TIMEOUT;


    public static final int DEFAULT = 0;
    public static final int DOWNLOADING = 1;
    public static final int PAUSE = 2;
    public static final int CANCEL = 3;

    private int downloadState = DEFAULT;

    /**
     * 私有构造函数，通过Builder 进行构造。
     *
     * @param builder
     */
    private Downloader(Builder builder) {
        this.downloadUrl = builder.downloadUrl;
        this.savePath = builder.savePath;
        this.saveName = builder.saveName;
        this.connectTimeout = builder.connectTimeout == 0 ? DEFAULT_TIMEOUT : builder.connectTimeout;
        this.readTimeout = builder.readTimeout == 0 ? DEFAULT_TIMEOUT : builder.readTimeout;
        this.callTimeout = builder.callTimeout == 0 ? DEFAULT_TIMEOUT : builder.callTimeout;
        this.threadNum = builder.threadNum == 0 ? DEFAULT_THREAD_NUM : builder.threadNum;
    }

    private void init(Context context) {
        if (db == null) {
            db = new SQLiteOpenHelper(context);
        }
        if (downloadApi == null) {
            initDownloadApi();
        }

    }

    /**
     * 初始化httpClient
     */
    private void initDownloadApi() {
        // 重写ResponseBody监听请求
//        Interceptor interceptor = (chain) -> {
//            okhttp3.Response originalResponse = chain.proceed(chain.request());
//
//            return originalResponse.newBuilder()
//                    .body(new DownloadResponseBody(originalResponse, offLen, listener))
//                    .build();
//        };
        OkHttpClient httpClient = new OkHttpClient.Builder()
                .retryOnConnectionFailure(true)
//                .addNetworkInterceptor(interceptor)
                .connectTimeout(connectTimeout, TimeUnit.SECONDS)
                .readTimeout(readTimeout, TimeUnit.SECONDS)
                .callTimeout(callTimeout, TimeUnit.SECONDS)
                .build();


        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://s.chengadx.com")
                //通过线程池获取一个线程，指定callback在子线程中运行。
                .callbackExecutor(Executors.newSingleThreadExecutor())
                .client(httpClient)
                .build();
        downloadApi = retrofit.create(DownloadApi.class);
    }

    /**
     * 网络请求下载文件
     *
     * @param entity   下载实体
     * @param listener 下载监听回调
     */
    public void download(DownloadEntity entity, final DownloadListener listener) {

    }

    /**
     * 接受网络输入流写入本地文件
     *
     * @param entity      下載文件信息
     * @param inputStream 下載文件输入流
     */
    private void writeStream2Disk(DownloadEntity entity, InputStream inputStream) {
        try {
            OutputStream outputStream = null;
            BufferedInputStream bis = null;
            RandomAccessFile randomAccessFile = null;
            File saveFile = new File(entity.getPath());
            try {
                byte[] buff = new byte[2048];
                long fileSizeDownloaded = entity.getStartLocation();
                int len = 0;
//                outputStream = new FileOutputStream(saveFile, true);
//                while ((len = inputStream.read(buff)) != -1) {
//                    outputStream.write(buff, 0, len);
//                    fileSizeDownloaded += len;
//                    Log.d(TAG, "file download: " + fileSizeDownloaded + "/ " + contentLength);
//                    listener.onProgress(Util.getProcess(fileSizeDownloaded, contentLength));
//                }
//                outputStream.flush();


                bis = new BufferedInputStream(inputStream);

                // 随机访问文件，可以指定断点续传的起始位置
                randomAccessFile = new RandomAccessFile(saveFile, "rwd");
                randomAccessFile.seek(fileSizeDownloaded);
                while ((len = bis.read(buff)) != -1) {
                    randomAccessFile.write(buff, 0, len);
                    fileSizeDownloaded += len;
                    Log.d(TAG, "file download: " + fileSizeDownloaded + "/ " + entity.getFileSize());
                    //更新下载缓存下载偏移量
                    entity.setStartLocation(fileSizeDownloaded);
                    listener.onProgress(Util.getProcess(fileSizeDownloaded * 100, entity.getFileSize()));
                }
                //TODO...校验文件大小和md5,当前只校验文件大小。
                if (saveFile.length() == entity.getFileSize()) {
                    downloadState = DEFAULT;
                    listener.onSuccess(entity.getPath());
                    db.updateDownloadProgress(entity.getPath(), entity.getStartLocation());
                    db.updateDownloadState(entity.getPath(), 1);
                } else {
                    downloadState = DEFAULT;
                    listener.onFail("down file invalid.");
                    db.updateDownloadState(entity.getPath(), -1);
                }

            } catch (IOException e) {
                if (downloadState == CANCEL) {
                    if (listener != null) {
                        listener.onCancel();
                        downloadState = DEFAULT;
                    }
                }else{
                    onError(entity, e.toString(), listener);
                }
            } finally {
                if (inputStream != null) {
                    inputStream.close();
                }
                if (outputStream != null) {
                    outputStream.close();
                }
                if (bis != null) {
                    bis.close();
                }
                if (randomAccessFile != null) {
                    randomAccessFile.close();
                }
            }
        } catch (IOException e) {
            onError(entity, e.toString(), listener);
        }
    }

    public void download(Context context, DownloadListener listener) {
        init(context);
        String path = savePath + File.separator + saveName;
        DownloadEntity record = db.queryRecord(path);
        //请求文件偏移量
        long offLen = 0;
//        File saveFile = new File(path);
//        if (saveFile.exists()) {
//            offLen = saveFile.length();
//        }
        long fileSize = 0;
        if (record != null) {
            offLen = record.getStartLocation();
            fileSize = record.getFileSize();
            Log.e(TAG, "restart receive data to File[" + path + "], " + offLen + "/" + fileSize);
        } else {
            record = new DownloadEntity();
            record.setDownloadUrl(downloadUrl);
            record.setPath(path);
            record.setStartLocation(offLen);
            record.setState(0);
            Log.e(TAG, "new task --> receive data to File[" + path + "]");
        }
        //TODO...验证文件完整性
        if (record.getState() == 1 && fileSize == offLen) {
            Log.e(TAG, "file has downloaded,return.");
            //TODO...此时是否告知已经下载完成？
            return;
        }
        this.entity = record;
        this.listener = listener;
        if (downloadState == PAUSE) {
            listener.onResume(entity.getStartLocation());
        } else {
            listener.onStart(entity.getStartLocation());
        }
        downloadState = DOWNLOADING;
        mCall = downloadApi.downloadFile("bytes=" + entity.getStartLocation() + "-", entity.getDownloadUrl());
        mCall.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    ResponseBody body = response.body();
                    String contentType = body.contentType().toString();
                    long contentLength = body.contentLength();
                    if (entity.getFileSize() == 0) {
                        //首次下載，不知文件大小，更新文件大小
                        entity.setFileSize(contentLength);
                        db.insertRecord(entity);
                    }
                    //application/octet-stream 通用文件流
                    Log.e(TAG, "fileSize[" + entity.getFileSize() + "] --- contentLength[" + contentLength + "] -- contentType[" + contentType + "]");
                    writeStream2Disk(entity, body.byteStream());
                } else {
                    onError(entity, "server contact failed", listener);
                    Log.d(TAG, "server contact failed");
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                onError(entity, t.toString(), listener);
                Log.e(TAG, "onFailure");
            }
        });
    }

    public void onError(DownloadEntity entity, String errorMsg, DownloadListener listener) {
        listener.onError(errorMsg);
        downloadState = PAUSE;
        db.updateDownloadProgress(entity.getPath(), entity.getStartLocation());
    }

    public void pause() {
        if (mCall != null && !mCall.isCanceled()) {
            mCall.cancel();
        }
        downloadState = PAUSE;
        if (listener != null) {
            listener.onPause(entity.getStartLocation());
        }
        //TODO... 将info信息写入数据库
        db.updateDownloadProgress(entity.getPath(), entity.getStartLocation());
    }

    public void cancel() {
        //TODO...同步数据库
        db.deleteRecord(entity.getPath());
        if (mCall != null && !mCall.isCanceled()) {
            mCall.cancel();
        }
        downloadState = CANCEL;
    }


    /**
     * 建造者模式创建Downloader实例
     */
    public static class Builder {
        private String downloadUrl;
        private String savePath;
        private String saveName;
        private int connectTimeout;
        private int readTimeout;
        private int callTimeout;
        private int threadNum;

        public Builder downloadUrl(String downloadUrl) {
            this.downloadUrl = downloadUrl;
            return this;
        }

        public Builder savePath(String savePath) {
            this.savePath = savePath;
            return this;
        }

        public Builder saveName(String saveName) {
            this.saveName = saveName;
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

        public Builder threadNum(int threadNum) {
            this.threadNum = threadNum;
            return this;
        }


        public Downloader build() {
            return new Downloader(this);
        }
    }


    public void onDestory() {
        if (db != null) {
            db.close();
        }
    }


    public boolean isDownlonding() {
        return downloadState == DOWNLOADING;
    }


}
