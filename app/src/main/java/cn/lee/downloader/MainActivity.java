package cn.lee.downloader;

import android.os.Environment;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import cn.lee.downloader.download.DownloadApi;
import cn.lee.downloader.download.DownloadListener;
import cn.lee.downloader.download.Downloader;
import cn.lee.downloader.retrofit.ServiceApi;
import cn.lee.downloader.retrofit.ServiceUtil;

public class MainActivity extends AppCompatActivity {

    private final static String TAG = "MainActivity";

    TextView processTv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        processTv = findViewById(R.id.process);

        String testUrl = "http://s.chengadx.com/big_screen_ad/upload/gx/cn.ycmedia.lcinstall4300.apk";
        String saveName = "cn.ycmedia.lcinstall4300.apk";
        startDownload(testUrl, saveName);

    }


    private void startDownload(String testUrl, String saveName) {
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
    }
}
