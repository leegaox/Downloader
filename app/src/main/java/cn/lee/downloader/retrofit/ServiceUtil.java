package cn.lee.downloader.retrofit;


import android.util.Log;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;

/**
 * @author Lee
 * @Title: {标题}
 * @Description:{}
 * @date 2016/12/14
 */
public class ServiceUtil {
    private static volatile ServiceApi serviceApi;

    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    public static final MediaType MULTIPART = MediaType.parse("multipart/form-data");
    private static final MediaType MEDIA_TYPE_PNG = MediaType.parse("image/png");


    public static ServiceApi getDefaultApi() {
        if (serviceApi == null) {
            serviceApi = getStringInstance("", ServiceApi.class);
        }
        return serviceApi;
    }


    public static <S> S getStringInstance(String url, Class<S> serviceClass) {
        Retrofit.Builder retrofitBuilder = new Retrofit.Builder()
                .baseUrl(url)
                //通过RxJavaCallAdapterFactory为Retrofit添加RxJava支持
//                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                //通过CustomGsonConverterFactory为Retrofit添加Gson支持
                .addConverterFactory(StringConverterFactory.create());
        Retrofit retrofit = retrofitBuilder.client(getOkHttpClient()).build();
        return retrofit.create(serviceClass);

    }

    public static <S> S createStringConvertService(String baseUrl, Class<S> serviceClass) {
        return createService(baseUrl, serviceClass);
    }

    public static <S> S createService(String baseUrl, Class<S> serviceClass) {
//        OkHttpClient client = new OkHttpClient.Builder().build();
        Retrofit.Builder retrofitBuilder = new Retrofit.Builder()
                .baseUrl(baseUrl)
                //通过RxJavaCallAdapterFactory为Retrofit添加RxJava支持
//                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                //通过CustomGsonConverterFactory为Retrofit添加Gson支持
                .addConverterFactory(CustomGsonConverterFactory.create());
        Retrofit retrofit = retrofitBuilder.client(getOkHttpClient()).build();
        return retrofit.create(serviceClass);
    }


    /**
     * 自定义client打印请求参数与结果参数
     *
     * @return 自定义client
     */
    public static OkHttpClient getOkHttpClient() {
        //日志显示级别
        HttpLoggingInterceptor.Level level = HttpLoggingInterceptor.Level.BODY;
        //新建log拦截器
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor(new HttpLoggingInterceptor.Logger() {
            @Override
            public void log(String message) {
                Log.e("HTTP", "OkHttp ======== Message:" + message);
            }
        });
        loggingInterceptor.setLevel(level);
        //定制OkHttp
        OkHttpClient.Builder httpClientBuilder = new OkHttpClient
                .Builder();
        //OkHttp进行添加拦截器loggingInterceptor
        httpClientBuilder.addInterceptor(loggingInterceptor);
        return httpClientBuilder.build();
    }

    /**
     * 设置默认参数
     *
     * @param bodyMap
     * @param type
     */
    public static void setDefaultParams(Map<String, RequestBody> bodyMap, MediaType type) {
//        String dev= MyApplication.getDeviceId();
//        bodyMap.put("device_id",RequestBody.create(type,dev));
//        bodyMap.put("channel",RequestBody.create(type,Constant.ANDROID_CHANNEL));
//        bodyMap.put("version",RequestBody.create(type,Constant.VERSION));
//        bodyMap.put("signature",RequestBody.create(type,Util.encodeMD5(dev.substring(0,5)+dev+Constant.ANDROID_CHANNEL)));
    }

    /**
     * 将文件路径数组封装为{@link List < MultipartBody.Part>}
     *
     * @param key         对应请求正文中name的值。目前服务器给出的接口中，所有图片文件使用<br>
     *                    同一个name值，实际情况中有可能需要多个
     * @param filePaths   文件路径数组
     * @param contentType 文件类型  "multipart/form-data"
     */
    public static List<MultipartBody.Part> files2Parts(String key, Object[] filePaths, MediaType contentType) {
        List<MultipartBody.Part> parts = new ArrayList<>(filePaths.length);
        for (Object filePath : filePaths) {
            File file = new File((String) filePath);
            // 根据类型及File对象创建RequestBody（okhttp的类）
            RequestBody requestBody = RequestBody.create(contentType, file);
            // 将RequestBody封装成MultipartBody.Part类型（同样是okhttp的）
            MultipartBody.Part part = MultipartBody.Part.
                    createFormData(key, file.getName(), requestBody);
            // 添加进集合
            parts.add(part);
        }
        return parts;
    }

    /**
     * 其实也是将File封装成RequestBody，然后再封装成Part，<br>
     * 不同的是使用MultipartBody.Builder来构建MultipartBody
     *
     * @param key       同上
     * @param filePaths 同上
     * @param imageType 同上
     */
    public static MultipartBody filesToMultipartBody(String key, String[] filePaths, MediaType imageType) {
        MultipartBody.Builder builder = new MultipartBody.Builder();
        for (String filePath : filePaths) {
            File file = new File(filePath);
            RequestBody requestBody = RequestBody.create(imageType, file);
            builder.addFormDataPart(key, file.getName(), requestBody);
        }
        builder.setType(MultipartBody.FORM);
        return builder.build();
    }


}
