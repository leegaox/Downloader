package cn.lee.downloader.retrofit;

import com.google.gson.annotations.SerializedName;

/**
 * @author Lee
 * @Title: {Api结果Bean}
 * @Description:{API返回的json字符串：{"key":1,"message":"成功","result":""}，result可能为POJI（Result<xxx>）、list（Result<List<xxx>>,因此使用泛型，对于result，需定义具体的POJI类}
 * @date 2016/12/14
 */
public class ApiResponseBody<T> {

    /**
     * 对于接口设计不严谨的情况下，同一属性，在不同接口中字段不一样时可使用alternate（备用字段，接收一个String数组.）
     * 注：当多种情况同时出时，以最后一个出现的值为准
     **/
    @SerializedName(value = "key", alternate = {"code"})
    private int key;
    @SerializedName(value = "message", alternate = {"mMessage"})
    private String message;
    @SerializedName(value = "result", alternate = {"data"})
    private T result;

    public int getKey() {
        return key;
    }

    public String getMessage() {
        return message;
    }

    /**
     * API是否请求成功
     * <p>
     * key ==1  或者key==3
     *
     * @return 失败返回true, 成功返回false
     */
    public boolean isOk() {
        return key == ApiException.SUCCESS;
    }
//
//    /**
//     * PAI token失效
//     *
//     * @return
//     */
//    public boolean isTokenInvalid() {
//        return key == TOKEN_EXPRIED;
//    }
//
//    /**
//     * PAI 是否数据为空
//     *
//     * @return
//     */
//    public boolean isDataEmpty() {
//        return key == NO_DATA;
//    }


    public void setKey(int key) {
        this.key = key;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getResult() {
        return result;
    }

    public void setResult(T result) {
        this.result = result;
    }
}
