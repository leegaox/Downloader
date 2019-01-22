package cn.lee.downloader.download;

import android.content.Context;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import cn.lee.downloader.util.SharedPreferencesUtil;
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

    private DownloadApi downloadApi;
    private DownloadListener listener;
    private SharedPreferencesUtil sp;
    /***下载文件存储路径***/
    private String savePath;
    private int connectTimeout = DEFAULT_TIMEOUT;
    private int readTimeout = DEFAULT_TIMEOUT;
    private int callTimeout = DEFAULT_TIMEOUT;

    /**
     * 私有构造函数，通过Builder 进行构造。
     *
     * @param builder
     */
    private Downloader(Builder builder) {
        this.savePath = builder.savePath;
        this.connectTimeout = builder.connectTimeout == 0 ? DEFAULT_TIMEOUT : builder.connectTimeout;
        this.readTimeout = builder.readTimeout == 0 ? DEFAULT_TIMEOUT : builder.readTimeout;
        this.callTimeout = builder.callTimeout == 0 ? DEFAULT_TIMEOUT : builder.callTimeout;
    }

    private void init(Context context, DownloadListener listener) {
        if (sp == null) {
            sp = new SharedPreferencesUtil(context);
        }
        if (downloadApi == null) {
            initDownloadApi();
        }
        this.listener = listener;
    }

    /**
     * 初始化httpClient
     */
    private void initDownloadApi() {
        OkHttpClient httpClient = new OkHttpClient.Builder()
                .retryOnConnectionFailure(true)
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
     * @param url      下载地址
     * @param saveName 下载下来本地保存文件名
     * @param listener 下载监听回调
     */
    public void download(Context context, String url, final String saveName, final DownloadListener listener) {
        init(context, listener);
        String path = savePath + File.separator + saveName;
        final File saveFile = new File(path);
        //请求文件偏移量
        long offLen = 0;
        long fileTotalLen = sp.getInt(saveFile.getAbsolutePath(), 0);
        if (saveFile.exists()) {
            offLen = saveFile.length();
        }
        //TODO...验证文件完整性
        if (fileTotalLen > 0 && offLen >= fileTotalLen) {
            Log.e(TAG, "file has downloaded,return.");
            //TODO...此时是否告知已经下载完成？
            return;
        }
        if (fileTotalLen > 0) {
            Log.e(TAG, "start receive data to File[" + path + "], " + offLen + "/" + fileTotalLen);
        } else {
            Log.e(TAG, "start receive data to File[" + path + "]");
        }
        //断电下载指定需要下载的文件偏移量
        String range = "bytes=" + offLen + "-";
        Call<ResponseBody> call = downloadApi.downloadFile(range, url);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    ResponseBody body = response.body();
                    String contentType = body.contentType().toString();
                    long contentLength = body.contentLength();
                    long fileTotalLength;
                    if (!saveFile.exists() || saveFile.length() == 0) {
                        fileTotalLength = contentLength;
                        //存储文件总大小
                        sp.putInt(saveFile.getAbsolutePath(), (int) contentLength);
                    } else {
                        //读取需要下载的文件总大小
                        fileTotalLength = sp.getInt(saveFile.getAbsolutePath(), (int) contentLength);
                    }
                    //application/octet-stream 通用文件流
                    Log.e(TAG, "fileTotalLength[" + fileTotalLength + "] --- contentLength[" + contentLength + "] -- contentType[" + contentType + "]");
                    writeStream2Disk(saveFile.length(), fileTotalLength, body.byteStream(), saveFile);
                } else {
                    listener.onFail("server contact failed");
                    Log.d(TAG, "server contact failed");
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                listener.onFail(t.toString());
                Log.e(TAG, "onFailure");
            }
        });
    }

    /**
     * 接受网络输入流写入本地文件
     *
     * @param contentLength 文件大小
     * @param inputStream   下載文件输入流
     * @param saveFile      本地保存的文件
     */
    private void writeStream2Disk(long offLen, long contentLength, InputStream inputStream, File saveFile) {
        try {

            OutputStream outputStream = null;
            try {
                byte[] fileReader = new byte[4096];
                long fileSizeDownloaded = offLen;
                outputStream = new FileOutputStream(saveFile, true);
                int len;
                while ((len = inputStream.read(fileReader)) != -1) {
                    outputStream.write(fileReader, 0, len);
                    fileSizeDownloaded += len;
                    Log.d(TAG, "file download: " + fileSizeDownloaded + "/ " + contentLength);
                    listener.onProcess(Util.getProcess(fileSizeDownloaded, contentLength));
                }
                outputStream.flush();
                listener.onSuccess(saveFile.getAbsolutePath());
                sp.remove(saveFile.getAbsolutePath());
            } catch (IOException e) {
                listener.onFail(e.toString());
            } finally {
                if (inputStream != null) {
                    inputStream.close();
                }
                if (outputStream != null) {
                    outputStream.close();
                }
            }
        } catch (IOException e) {
            listener.onFail(e.toString());
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


        public Downloader build() {
            return new Downloader(this);
        }
    }


}
