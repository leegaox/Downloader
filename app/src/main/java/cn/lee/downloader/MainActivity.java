package cn.lee.downloader;

import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import cn.lee.downloader.download.DownloadListener;
import cn.lee.downloader.download.Downloader;

public class MainActivity extends AppCompatActivity {

    private final static String TAG = "MainActivity";

    TextView processTv;
    ProgressBar process;
    Button startBtn, cancelBtn;

    /***下载器***/
    Downloader downloader;

    ListView listView;


    String url = "http://s.chengadx.com/big_screen_ad/upload/gx/cn.ycmedia.lcinstall4300.apk";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        listView = findViewById(R.id.list);
        listView.setAdapter(new MyAdapter(this, testData()));

//        process = findViewById(R.id.process);
//        processTv = findViewById(R.id.processTv);
//        startBtn = findViewById(R.id.startBtn);
//        cancelBtn = findViewById(R.id.cancelBtn);
//        startBtn.setOnClickListener((v) -> download(url,new DownloadListener() {
//
//            @Override
//            public void onProgress(final double progress) {
//                Log.e(TAG, "progress[" + progress + "% ]");
//                runOnUiThread(() -> {
//                    process.setProgress((int) Math.ceil(progress));
//                    processTv.setText(progress + "% ");
//                });
//            }
//
//            @Override
//            public void onStart(long startLocation) {
//                runOnUiThread(() -> {
//                    startBtn.setText("暂停");
//                    Toast.makeText(MainActivity.this, "start location: " + startLocation, Toast.LENGTH_SHORT).show();
//                });
//            }
//
//            @Override
//            public void onPause(long stopLocation) {
//                runOnUiThread(() -> {
//                    startBtn.setText("继续");
//                    Toast.makeText(MainActivity.this, "pause location: " + stopLocation, Toast.LENGTH_SHORT).show();
//                });
//            }
//
//            @Override
//            public void onResume(long resumeLocation) {
//                runOnUiThread(() -> {
//                    startBtn.setText("暂停");
//                    Toast.makeText(MainActivity.this, "resume location: " + resumeLocation, Toast.LENGTH_SHORT).show();
//
//                });
//            }
//
//            @Override
//            public void onCancel() {
//                runOnUiThread(() -> {
//                    process.setProgress(0);
//                    processTv.setText("0.0%");
//                    startBtn.setText("开始");
//                    Toast.makeText(MainActivity.this, "cancel download", Toast.LENGTH_SHORT).show();
//                });
//            }
//
//            @Override
//            public void onSuccess(String filPath) {
//                runOnUiThread(() -> {
//                    startBtn.setText("完成");
//                });
//                Log.e(TAG, "onSuccess --> file[" + filPath + "]");
//            }
//
//            @Override
//            public void onFail(String errorInfo) {
//                Log.e(TAG, "onFail --> " + errorInfo);
//            }
//
//            @Override
//            public void onError(String errorInfo) {
//                Log.e(TAG, "onError --> " + errorInfo);
//            }
//        }));
//        cancelBtn.setOnClickListener((v) -> cancel(url));
    }

    private List<Bean> testData() {
        List<Bean> testList = new ArrayList<>();
        Bean bean1 = new Bean("https://imtt.dd.qq.com/16891/371C7C353C7B87011FB3DE8B12BCBCA5.apk?fsname=com.tencent.mm_7.0.0_1380.apk&csr=1bbd", "wx.apk");
        Bean bean2 = new Bean("http://s.chengadx.com/big_screen_ad/upload/gx/cn.ycmedia.lcinstall4300.apk", "install.apk");
        testList.add(bean1);
        testList.add(bean2);
        return testList;
    }

    public void download(String url, String saveName, DownloadListener listener) {
        if (downloader != null && downloader.isDownloading(url)) {
            downloader.pause(url);
        } else {
            if (downloader == null) {
                downloader = new Downloader.Builder()
                        .savePath(Environment.getExternalStorageDirectory().getPath())
                        .connectTimeout(30)
                        .callTimeout(30)
                        .readTimeout(30)
                        .taskThreshold(5)
                        .build();
            }
            downloader.download(this, url, saveName, listener);
        }
    }


    public void cancel(String url) {
        if (downloader != null) {
            downloader.cancel(url);
        }
    }

}
