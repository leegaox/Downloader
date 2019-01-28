package cn.lee.downloader.download;

import android.util.Log;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;

import cn.lee.downloader.db.DownloadEntity;
import cn.lee.downloader.util.Util;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * @author yanfa
 * @Title: {下载任务}
 * @Description:{描述}
 * @date 2019/1/22
 */
public class DownloadTask {

    private final static String TAG = "DownloadTask";

    private static final int DEFAULT_THREAD_NUM = 3;

    private Downloader downloader;
    private Call<ResponseBody> mCall;
    private DownloadListener listener;
    private DownloadEntity entity;

    private int threadNum = DEFAULT_THREAD_NUM;


    public static final int DEFAULT = 0;
    public static final int DOWNLOADING = 1;
    public static final int PAUSE = 2;
    public static final int CANCEL = 3;

    private int downloadState = DEFAULT;


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
                int len;
                bis = new BufferedInputStream(inputStream);
                // 随机访问文件，可以指定断点续传的起始位置
                randomAccessFile = new RandomAccessFile(saveFile, "rwd");
                randomAccessFile.seek(fileSizeDownloaded);
                while ((len = bis.read(buff)) != -1) {
                    randomAccessFile.write(buff, 0, len);
                    fileSizeDownloaded += len;
                    Log.d(TAG, "file[" + entity.getDownloadUrl() + "] download: " + fileSizeDownloaded + "/ " + entity.getFileSize());
                    //更新下载缓存下载偏移量
                    entity.setStartLocation(fileSizeDownloaded);
                    listener.onProgress(Util.getProcess(fileSizeDownloaded * 100, entity.getFileSize()));
                }
                //TODO...校验文件大小和md5,当前只校验文件大小。
                if (saveFile.length() == entity.getFileSize()) {
                    downloadState = DEFAULT;
                    listener.onSuccess(entity.getPath());
                    downloader.getDB().updateDownloadProgress(entity.getPath(), entity.getStartLocation());
                    downloader.getDB().updateDownloadState(entity.getPath(), 1);
                } else {
                    downloadState = DEFAULT;
                    listener.onFail(" file[" + entity.getDownloadUrl() + "] downloaded, but invalid.");
                    downloader.getDB().updateDownloadState(entity.getPath(), -1);
                }

            } catch (IOException e) {
                onError(entity, e.toString(), listener);
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

    /**
     * @param downloader  下载器实例
     * @param downloadUrl 下载链接
     * @param path        保存文件本地绝对路径
     * @param listener    下载监听
     */
    public void download(Downloader downloader, String downloadUrl, String path, DownloadListener listener) {
        this.downloader = downloader;
        DownloadEntity record = downloader.getDB().queryRecord(path);
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
//        if (downloadState == PAUSE) {
//            listener.onResume(entity.getStartLocation());
//        } else {
        listener.onStart(entity.getStartLocation());
//        }
        request();
    }

    private void request() {
        downloadState = DOWNLOADING;
        mCall = downloader.getDownloadApi().downloadFile("bytes=" + entity.getStartLocation() + "-", entity.getDownloadUrl());
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
                        downloader.getDB().insertRecord(entity);
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

    public void resume() {
        listener.onResume(entity.getStartLocation());
        request();
    }

    public void onError(DownloadEntity entity, String errorMsg, DownloadListener listener) {
        downloader.getDB().updateDownloadProgress(entity.getPath(), entity.getStartLocation());
        if (downloadState == DOWNLOADING) {
            //下载中网络问题导致链接终端
            downloadState = PAUSE;
            //TODO...主动下载重试
            listener.onError(errorMsg);
        } else if (downloadState == CANCEL) {
            //手动取消导致中断
            if (listener != null) {
                listener.onCancel();
            }
        } else if (downloadState == PAUSE) {
            //如果是手动暂停导致网络中断，则忽略本次中断onError回调
            return;
        }
    }

    public void pause() {
        downloadState = PAUSE;
        if (mCall != null && !mCall.isCanceled()) {
            mCall.cancel();
        }
        if (listener != null) {
            listener.onPause(entity.getStartLocation());
        }
        //TODO... 将info信息写入数据库
        downloader.getDB().updateDownloadProgress(entity.getPath(), entity.getStartLocation());
    }

    public void cancel() {
        downloadState = CANCEL;
        if (mCall != null) {
            if (!mCall.isCanceled()) {
                mCall.cancel();
            }
        }
        listener.onCancel();
        //同步数据库
        downloader.getDB().deleteRecord(entity.getPath());
    }

    public DownloadEntity getEntity() {
        return entity;
    }

    public Boolean isDownloading() {
        return downloadState == DOWNLOADING;
    }

}
