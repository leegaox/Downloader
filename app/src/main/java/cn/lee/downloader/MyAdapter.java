package cn.lee.downloader;

import android.content.Context;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import cn.lee.downloader.download.DownloadListener;
import cn.lee.downloader.download.Downloader;

/**
 * @author yanfa
 * @Title: {标题}
 * @Description:{描述}
 * @date 2019/1/24
 */
public class MyAdapter extends BaseAdapter {

    private final static String TAG = "MyAdapter";
    private MainActivity context;
    private LayoutInflater mInflater;
    private List<Bean> mDatas;

    //MyAdapter需要一个Context，通过Context获得Layout.inflater，然后通过inflater加载item的布局
    public MyAdapter(MainActivity context, List<Bean> datas) {
        this.context = context;
        mInflater = LayoutInflater.from(context);
        mDatas = datas;
    }

    //返回数据集的长度
    @Override
    public int getCount() {
        return mDatas.size();
    }

    @Override
    public Object getItem(int position) {
        return mDatas.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    //这个方法才是重点，我们要为它编写一个ViewHolder
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final ViewHolder holder;
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.item_list, parent, false); //加载布局
            holder = new ViewHolder();

            holder.processTv = convertView.findViewById(R.id.processTv);
            holder.process = convertView.findViewById(R.id.process);
            holder.startBtn = convertView.findViewById(R.id.startBtn);
            holder.cancelBtn = convertView.findViewById(R.id.cancelBtn);

            convertView.setTag(holder);
        } else {   //else里面说明，convertView已经被复用了，说明convertView中已经设置过tag了，即holder
            holder = (ViewHolder) convertView.getTag();
        }

        Bean bean = mDatas.get(position);
        holder.startBtn.setOnClickListener((v) -> context.download(bean.getUrl(), bean.getSaveName(), new DownloadListener() {

            @Override
            public void onProgress(final double progress) {
                Log.e(TAG, "progress[" + progress + "% ]");
                context.runOnUiThread(() -> {
                    holder.process.setProgress((int) Math.ceil(progress));
                    holder.processTv.setText(progress + "% ");
                });
            }

            @Override
            public void onStart(long startLocation) {
                context.runOnUiThread(() -> {
                    holder.startBtn.setText("暂停");
                    Toast.makeText(context, "start location: " + startLocation, Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onPause(long stopLocation) {
                context.runOnUiThread(() -> {
                    holder.startBtn.setText("继续");
                    Toast.makeText(context, "pause location: " + stopLocation, Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onResume(long resumeLocation) {
                context.runOnUiThread(() -> {
                    holder.startBtn.setText("暂停");
                    Toast.makeText(context, "resume location: " + resumeLocation, Toast.LENGTH_SHORT).show();

                });
            }

            @Override
            public void onCancel() {
                context.runOnUiThread(() -> {
                    holder.process.setProgress(0);
                    holder.processTv.setText("0.0%");
                    holder.startBtn.setText("开始");
                    Toast.makeText(context, "cancel download", Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onSuccess(String filPath) {
                context.runOnUiThread(() -> {
                    holder.startBtn.setText("完成");
                });
                Log.e(TAG, "onSuccess --> file[" + filPath + "]");
            }

            @Override
            public void onFail(String errorInfo) {
                Log.e(TAG, "onFail --> " + errorInfo);
            }

            @Override
            public void onError(String errorInfo) {
                context.runOnUiThread(() -> {
                    Log.e(TAG, "onError --> " + errorInfo);
                    holder.startBtn.setText("继续");
                });

            }
        }));
        holder.cancelBtn.setOnClickListener((v) -> context.cancel(bean.getUrl()));

        return convertView;
    }

    //这个ViewHolder只能服务于当前这个特定的adapter，因为ViewHolder里会指定item的控件，不同的ListView，item可能不同，所以ViewHolder写成一个私有的类
    private class ViewHolder {
        TextView processTv;
        ProgressBar process;
        Button startBtn, cancelBtn;
    }

}
