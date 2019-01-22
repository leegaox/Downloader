package cn.lee.downloader.retrofit;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Streaming;
import retrofit2.http.Url;

/**
 * @author Lee
 * @Title: {接口服务}
 * @Description:{使用Retrofit将HTTP API转换为Java接口}
 * TODO...
 * 1. 通用Get,Post API 请求封装
 * @date 2016/11/25
 */
public interface ServiceApi {

    /**
     * 下载文件
     *
     * @param fileUrl
     * @return
     */
    @Streaming //大文件时要加不然会OOM
    @GET
    Call<ResponseBody> downloadFile(@Url String fileUrl);



}
