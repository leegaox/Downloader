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
     * @param process 下载进度 00.0%
     */
    void onProcess(String process);

    /**
     * 下载完成回调，
     *
     * @param filPath 返回文件路径
     */
    void onSuccess(String filPath);

    /**
     * 下载失败回调
     *
     * @param errorInfo 失败原因信息
     */
    void onFail(String errorInfo);
}
