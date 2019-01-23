package cn.lee.downloader.db;

import java.io.File;

/**
 * @author yanfa
 * @Title: {标题}
 * @Description:{描述}
 * @date 2019/1/23
 */
public class DownloadEntity {
    /***文件总长度***/
    long fileSize;
    /***下载链接***/
    String downloadUrl;
    /***起始下载位置***/
    long startLocation;
    /***结束下载的位置***/
    long endLocation;
    /***存储路径***/
    String path;
    /***下载文件***/
    File tempFile;
    /***下载状态***/
    int state;

    public DownloadEntity() {
    }

    public DownloadEntity(long fileSize, String downloadUrl, String path, long startLocation, long endLocation, int state) {
        this.fileSize = fileSize;
        this.downloadUrl = downloadUrl;
        this.path = path;
        this.startLocation = startLocation;
        this.endLocation = endLocation;
        this.state = state;
    }

    public long getFileSize() {
        return fileSize;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }

    public String getDownloadUrl() {
        return downloadUrl;
    }

    public void setDownloadUrl(String downloadUrl) {
        this.downloadUrl = downloadUrl;
    }

    public long getStartLocation() {
        return startLocation;
    }

    public void setStartLocation(long startLocation) {
        this.startLocation = startLocation;
    }

    public long getEndLocation() {
        return endLocation;
    }

    public void setEndLocation(long endLocation) {
        this.endLocation = endLocation;
    }


    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public File getTempFile() {
        return tempFile;
    }

    public void setTempFile(File tempFile) {
        this.tempFile = tempFile;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }
}
