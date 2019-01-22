package cn.lee.downloader.util;

import java.text.DecimalFormat;

/**
 * @author yanfa
 * @Title: {标题}
 * @Description:{描述}
 * @date 2019/1/22
 */
public class Util {

    public static String getProcess(double process, double total) {
        //##.00%   百分比格式，后面不足2位的用0补齐
        DecimalFormat df1 = new DecimalFormat("##.00%");
        return df1.format(process / total);
    }
}
