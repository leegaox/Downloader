package cn.lee.downloader;

import android.os.Environment;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ProgressBar;
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
    ProgressBar process;
    Button startBtn, cancelBtn;
    Downloader downloader;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        process = findViewById(R.id.process);
        processTv = findViewById(R.id.processTv);
        startBtn = findViewById(R.id.startBtn);
        cancelBtn = findViewById(R.id.cancelBtn);
        startBtn.setOnClickListener((v) -> {
            if (downloader != null && downloader.isDownlonding()) {
                downloader.pause();
            } else {
                startDownload();
            }
        });
        cancelBtn.setOnClickListener((v) -> downloader.cancel());
    }


    private void startDownload() {
        if (downloader == null) {
            downloader = new Downloader.Builder()
                    .savePath(Environment.getExternalStorageDirectory().getPath())
                    .downloadUrl("https://imtt.dd.qq.com/16891/371C7C353C7B87011FB3DE8B12BCBCA5.apk?fsname=com.tencent.mm_7.0.0_1380.apk&csr=1bbd")
                    .saveName("wx.apk")
                    .connectTimeout(30)
                    .callTimeout(30)
                    .readTimeout(30)
                    .build();
        }
        downloader.download(this, new DownloadListener() {

            @Override
            public void onProgress(final double progress) {
                Log.e(TAG, "progress[" + progress + "% ]");
                runOnUiThread(() -> {
                    process.setProgress((int) Math.ceil(progress));
                    processTv.setText(progress + "% ");
                });
            }

            @Override
            public void onStart(long startLocation) {
                runOnUiThread(() -> {
                    startBtn.setText("暂停");
                    Toast.makeText(MainActivity.this, "start location: " + startLocation, Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onPause(long stopLocation) {
                runOnUiThread(() -> {
                    startBtn.setText("继续");
                    Toast.makeText(MainActivity.this, "pause location: " + stopLocation, Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onResume(long resumeLocation) {
                runOnUiThread(() -> {
                    startBtn.setText("暂停");
                    Toast.makeText(MainActivity.this, "resume location: " + resumeLocation, Toast.LENGTH_SHORT).show();

                });
            }

            @Override
            public void onCancel() {
                runOnUiThread(() -> {
                    process.setProgress(0);
                    processTv.setText("0.0%");
                    startBtn.setText("开始");
                    Toast.makeText(MainActivity.this, "cancel download", Toast.LENGTH_SHORT).show();
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

            @Override
            public void onError(String errorInfo) {
                Log.e(TAG, "onError --> " + errorInfo);
            }
        });
    }
}
