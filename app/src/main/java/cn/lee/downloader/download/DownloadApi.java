package cn.lee.downloader.download;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Streaming;
import retrofit2.http.Url;

/**
 * @author Lee
 * @Title: {下载api}
 * @Description:{使用Retrofit将HTTP API转换为Java接口}
 * @date 2016/11/25
 */
public interface DownloadApi {

    /**
     * 下载文件
     *
     * @Streaming： 下载大文件时要加@Streaming ，不然会OOM，小文件可以忽略不注入。、
     * @Header("Range")： 如果想进行断点续传的话 可以在此加入header，但不建议直接在api中写死，每个下载的请求大小是不同的，在拦截器加入更为妥善。
     *
     * @param fileUrl  下载地址，由于地址是可变的，因此用 @URL 注解符号来进行指定。
     * @return
     */
    @Streaming
    @GET
    Call<ResponseBody> downloadFile(@Header("RANGE") String range, @Url String fileUrl);


}
