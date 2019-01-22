package cn.lee.downloader.retrofit;

/**
 * @author Lee
 * @Title: {基于MVP的Model 回调}
 * @Description:{Presenter 调用Model进行网络请求的回调}
 * @date 2018/12/3
 */
public interface ApiCallBack<T> {

    /**{@link ApiResponseBody} key==1 的情况**/
    void onSuccess(T response);


    /**其他的一些error 根据实际情况显示Toast及其他业务逻辑**/
    void onError(ApiException e);

//    /**token失效**/
//    void onTokenInvalid();
//
//    /**无网络连接**/
//    void onNoNetwork();
}
