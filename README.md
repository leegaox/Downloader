# Downloader

Android环境下的断点下载器

## 调用示例


````java
Downloader downloader = new Downloader.Builder()
        .savePath(Environment.getExternalStorageDirectory().getPath())
        .connectTimeout(30)
        .callTimeout(30)
        .readTimeout(30)
        .build();
downloader.download(this, testUrl, saveName, new DownloadListener() {

    @Override
    public void onProcess(final String process) {
        Log.e(TAG, "process[" + process + "]");
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                processTv.setText(process );
            }
        });
    }

    @Override
    public void onSuccess(String filPath) {
        Log.e(TAG, "onSuccess --> file[" + filPath + "]");
    }

    @Override
    public void onFail(String errorInfo) {
        Log.e(TAG, "onFail --> " + errorInfo);
    }
});      
````

## TODO 
1. db替代sp，支持下载监听增加pause、restart；
2. 多线程下载替换单线程；
3. 兼容6.0+，权限动态申请。
