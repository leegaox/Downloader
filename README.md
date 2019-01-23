# Downloader

Android环境下的断点下载器

## 调用示例


````java
new Downloader.Builder()
                .savePath(Environment.getExternalStorageDirectory().getPath())
                .downloadUrl("")
                .saveName("")
                .connectTimeout(30)
                .callTimeout(30)
                .readTimeout(30)
                .build().start(this, new DownloadListener() {

            @Override
            public void onProgress(final String progress) {
                Log.e(TAG, "progress[" + progress + "]");
                runOnUiThread(() -> processTv.setText(progress));
            }

            @Override
            public void onStart(long startLocation) {

            }

            @Override
            public void onPause(long stopLocation) {

            }

            @Override
            public void onResume(long resumeLocation) {

            }

            @Override
            public void onCancel() {

            }

            @Override
            public void onSuccess(String filPath) {
                Log.e(TAG, "onSuccess --> file[" + filPath + "]");
            }

            @Override
            public void onFail(String errorInfo) {
                Log.e(TAG, "onFail --> " + errorInfo);
            }

            @Override
            public void onError(String errorInfo) {
                Log.e(TAG, "onError --> " + errorInfo);
            }
        });
````

## TODO 
1. 多线程下载替换单线程；
2. 兼容6.0+，权限动态申请。
