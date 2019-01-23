package cn.lee.downloader.download;

/**
 * @author yanfa
 * @Title: {下载监听接口}
 * @Description:{描述}
 * @date 2019/1/22
 */
public interface DownloadListener {

    /**
     * 下载中回调
     *
     * @param progress 下载进度 00.00%
     */
    void onProgress(String progress);

    /**
     * 开始下载
     */
    void onStart(long startLocation);

    /**
     * 暂停下载
     */
    void onPause(long stopLocation);

    /**
     * 恢复位置
     */
    void onResume(long resumeLocation);

    /**
     * 取消下载
     */
    void onCancel();

    /**
     * 下载完成回调，
     *
     * @param filPath 返回文件路径
     */
    void onSuccess(String filPath);


    /**
     * 下载失败回调 文件校验失败
     *
     * @param errorInfo 失败原因信息
     */
    void onFail(String errorInfo);


    /**
     * 下载中断，网络等，可继续下载
     * @param errorInfo
     */
    void onError(String errorInfo);
}
