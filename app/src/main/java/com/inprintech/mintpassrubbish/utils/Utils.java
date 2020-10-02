package com.inprintech.mintpassrubbish.utils;

import android.content.Context;
import android.hardware.usb.UsbManager;
import android.os.Build;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import com.inprintech.mintpassrubbish.MyApplication;

import java.net.NetworkInterface;
import java.util.Collections;
import java.util.List;

public class Utils {

    public static final String ACTION_USB_PERMISSION = "cn.wch.wchusbdriver.USB_PERMISSION";
    /*  USB配置参数 */
    public static final int baudRate = 115200;//波特率
    public static final byte stopBit = 1;//停止位
    public static final byte dataBit = 8;//数据位
    public static final byte parity = 0;//奇偶校验
    public static final byte flowControl = 0;
    /* 发送的指令 */
    public static final String OPEN_WEIGHT1 = "{\"open_1\":1,\"weight_1\":1}";
    public static final String OPEN_WEIGHT2 = "{\"open_2\":1,\"weight_2\":1}";
    public static final String OPEN_WEIGHT3 = "{\"open_3\":1,\"weight_3\":1}";
    public static final String OPEN_WEIGHT4 = "{\"open_4\":1,\"weight_4\":1}";
    public static final String CLOSE_WEIGHT = "{\"close_1\":1,\"close_2\":1,\"close_3\":1,\"close_4\":1,\"weight_1\":1,\"weight_2\":1,\"weight_3\":1,\"weight_4\":1,\"close_1\":1,\"close_2\":1,\"close_3\":1,\"close_4\":1}";
    public static final String WEIGHT1 = "{\"weight_1\":1}";
    public static final String WEIGHT2 = "{\"weight_2\":1}";
    public static final String WEIGHT3 = "{\"weight_3\":1}";
    public static final String WEIGHT4 = "{\"weight_4\":1}";

    private static String getMacFromHardware() {
        try {
            List<NetworkInterface> all = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface nif : all) {
                if (!nif.getName().equalsIgnoreCase("wlan0")) continue;

                byte[] macBytes = nif.getHardwareAddress();
                if (macBytes == null) {
                    return "";
                }

                StringBuilder res1 = new StringBuilder();
                for (byte b : macBytes) {
                    res1.append(String.format("%02X:", b));
                }

                if (res1.length() > 0) {
                    res1.deleteCharAt(res1.length() - 1);
                }
                return res1.toString();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "02:00:00:00:00:00";
    }

    /**
     * 获取MAC地址
     *
     * @return
     */
    public static String getMacAddress() {
        String mac = "02:00:00:00:00:00";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            mac = getMacFromHardware();
        }
        return mac;
    }

    /**
     * 将String转化为byte[]数组
     *
     * @param arg 需要转换的String对象
     * @return 转换后的byte[]数组
     */
    public static byte[] toByteArray2(String arg) {
        if (arg != null) {
            /* 1.先去除String中的' '，然后将String转换为char数组  */
            char[] NewArray = new char[1000];
            char[] array = arg.toCharArray();
            int length = 0;
            for (int i = 0; i < array.length; i++) {
                if (array[i] != ' ') {
                    NewArray[length] = array[i];
                    length++;
                }
            }

            byte[] byteArray = new byte[length];
            for (int i = 0; i < length; i++) {
                byteArray[i] = (byte) NewArray[i];
            }
            return byteArray;

        }
        return new byte[]{};
    }
}
