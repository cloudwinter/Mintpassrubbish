package com.inprintech.mintpassrubbish.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.CountDownTimer;
import android.util.Log;
import android.widget.TextView;

import com.inprintech.mintpassrubbish.activity.DustbinTypeActivity;
import com.inprintech.mintpassrubbish.activity.MainActivity;

import java.lang.ref.WeakReference;

public class CountDownTimerUtils extends CountDownTimer {

    private final static String TAG = "CountDownTimerUtils";

    WeakReference<TextView> mTextView;//显示倒计时的文字  用弱引用 防止内存泄漏
    private Context mContext;

    public CountDownTimerUtils(TextView textView, long millisInFuture, long countDownInterval, Context context) {
        super(millisInFuture, countDownInterval);
        this.mContext = context;
        this.mTextView = new WeakReference(textView);
    }

    @Override
    public void onTick(long l) {
        int remainTime = (int) (l / 1000L);
        mTextView.get().setText(String.valueOf(remainTime));
    }

    @Override
    public void onFinish() {
        Intent intent = new Intent(mContext, MainActivity.class);
        mContext.startActivity(intent);
        Activity activity = (Activity) mContext;
        if (activity != null) {
            activity.finish();
        }
        Log.d(TAG,activity.getClass().getName()+".onFinish 跳转到MainActivity");
    }
}
