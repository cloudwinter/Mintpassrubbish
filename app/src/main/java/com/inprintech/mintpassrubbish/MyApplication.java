package com.inprintech.mintpassrubbish;

import android.app.Application;

import com.lzy.okgo.OkGo;
import com.lzy.okgo.cookie.CookieJarImpl;
import com.lzy.okgo.cookie.store.MemoryCookieStore;
import com.lzy.okgo.interceptor.HttpLoggingInterceptor;

import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

import cn.wch.ch34xuartdriver.CH34xUARTDriver;
import okhttp3.OkHttpClient;

public class MyApplication extends Application {
    private static final String TAG = "MyApplication";

    public static CH34xUARTDriver driver;

    @Override
    public void onCreate() {
        super.onCreate();
        // Normal app init code...
        //使用OkGo的拦截器
        RunningEnvironment.init(this);
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor("meee");
        //日志的打印范围F
        loggingInterceptor.setPrintLevel(HttpLoggingInterceptor.Level.BODY);
        //在logcat中的颜色
        loggingInterceptor.setColorLevel(Level.INFO);
        //默认是Debug日志类型
        builder.addInterceptor(loggingInterceptor);
        //使用内存保持cookie，app退出后，cookie消失
        builder.cookieJar(new CookieJarImpl(new MemoryCookieStore()));
        //设置请求超时时间,默认60秒
        builder.readTimeout(OkGo.DEFAULT_MILLISECONDS, TimeUnit.MILLISECONDS);      //读取超时时间
        builder.writeTimeout(OkGo.DEFAULT_MILLISECONDS, TimeUnit.MILLISECONDS);     //写入超时时间
        builder.connectTimeout(OkGo.DEFAULT_MILLISECONDS, TimeUnit.MILLISECONDS);   //连接超时时间
        OkGo.getInstance()
                .init(this)
                .setOkHttpClient(builder.build())//不设置则使用默认
                .setRetryCount(0);//请求超时重连次数,默认3次
    }
}
