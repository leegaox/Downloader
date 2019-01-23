package cn.lee.downloader;

import android.os.Environment;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
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
    Button startBtn, stopBtn, cancelBtn;
    Downloader downloader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        processTv = findViewById(R.id.process);
        startBtn = findViewById(R.id.startBtn);
        stopBtn = findViewById(R.id.stopBtn);
        cancelBtn = findViewById(R.id.cancelBtn);
        startBtn.setOnClickListener((v) -> startDownload());
        stopBtn.setOnClickListener((v) -> downloader.pause());
        cancelBtn.setOnClickListener((v) -> downloader.cancel());
    }


    private void startDownload() {
        downloader = new Downloader.Builder()
                .savePath(Environment.getExternalStorageDirectory().getPath())
                .downloadUrl("http://s.chengadx.com/big_screen_ad/upload/gx/cn.ycmedia.lcinstall4300.apk")
                .saveName("cn.ycmedia.lcinstall4300.apk")
                .connectTimeout(30)
                .callTimeout(30)
                .readTimeout(30)
                .build();
        downloader.start(this, new DownloadListener() {

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

            }
        });
    }
}
