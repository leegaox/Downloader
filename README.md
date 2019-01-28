# Downloader

Android环境下的断点下载器Downloader，支持多任务下载，暂停、取消、继续等操作。

## 调用示例

### 初始化下载器
> 保证单一实例，无需实例化多个Downloader。
````java
Downloader downloader = new Downloader.Builder()
                        .savePath(Environment.getExternalStorageDirectory().getPath())//文件存储路径
                        .connectTimeout(30)
                        .callTimeout(30)
                        .readTimeout(30)
                        .taskThreshold(5) //任务数阈值，默认是3
                        .build();
````
### 下载
> 下载器Downloader自动判断当前下载任务来开启新的下载任务还是继续暂停的下载任务。
````java
 downloader.download(this, url, saveName, listener);
 ````
 
 ### 暂停下载任务
 ````java
if (downloader != null && downloader.isDownloading(url)) {
    downloader.pause(url);
}
````

### 取消下载任务
 ````java
if (downloader != null) {
    downloader.cancel(url);
}
````

