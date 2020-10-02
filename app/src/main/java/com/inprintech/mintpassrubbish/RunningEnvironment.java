package com.inprintech.mintpassrubbish;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;

import com.squareup.leakcanary.LeakCanary;
import com.squareup.leakcanary.RefWatcher;

import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * Created by xiayundong on 2020/4/9.
 */
public class RunningEnvironment {

    /**
     * 全局静态context
     */
    public static Context sAppContext = null;

    /**
     *
     */
    public static RefWatcher refWatcher;

    /**
     * 移动设备的唯一标识
     */
    private static String IMEI = null;
    /**
     * MAC地址，用于模拟IMEI
     */
    public static String MAC = null;
    /**
     * SIM卡唯一标识
     */
    private static String IMSI = null;
    /**
     * 手机号码
     */
    private static String PHONE_NUMBER = null;

    /**
     * 全局线程池
     */
    public static ThreadPoolExecutor sThreadPool = (ThreadPoolExecutor) Executors
            .newCachedThreadPool();


    /**
     * 初始化方法
     *
     * @param app
     */
    public static void init(Application app) {
        if (sAppContext == null) {
            sAppContext = app.getApplicationContext();
        }
        if (LeakCanary.isInAnalyzerProcess(app)) {
            refWatcher = RefWatcher.DISABLED;
        } else {
            refWatcher = LeakCanary.install(app);
        }
        //initDeviceInfo();
    }


//    /**
//     * 初始化设备信息
//     *
//     * @return
//     */
//    @SuppressLint("MissingPermission")
//    public static void initDeviceInfo() {
//        TelephonyManager phoneManager = (TelephonyManager) sAppContext
//                .getSystemService(Context.TELEPHONY_SERVICE);
//
//        if (TextUtils.isEmpty(PHONE_NUMBER)) {
//            try {
//                PHONE_NUMBER = phoneManager.getLine1Number();
//            } catch (Exception e) {
//            }
//        }
//        if (TextUtils.isEmpty(IMEI)) {
//            try {
//                IMEI = phoneManager.getDeviceId();
//            } catch (Exception e) {
//            }
//        }
//        if (TextUtils.isEmpty(IMSI)) {
//            try {
//                IMSI = phoneManager.getSubscriberId();
//            } catch (Exception e) {
//            }
//        }
//
//        if (TextUtils.isEmpty(MAC)) {
//            try {
//                WifiManager wm = (WifiManager) sAppContext
//                        .getSystemService(Context.WIFI_SERVICE);
//                if (wm != null) {
//                    WifiInfo wi = wm.getConnectionInfo();
//                    if (wi != null) {
//                        String mac = wi.getMacAddress();
//                        if (mac != null) {
//                            MAC = mac.replace(":", "");
//                        }
//                    }
//                }
//            } catch (Exception e) {
//            }
//        }
//    }
}
