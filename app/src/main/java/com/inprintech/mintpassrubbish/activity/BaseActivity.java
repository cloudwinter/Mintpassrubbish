package com.inprintech.mintpassrubbish.activity;

import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

/**
 * Created by xiayundong on 2020/5/14.
 */
public abstract class BaseActivity extends AppCompatActivity {

    private static final String TAG = "BaseActivity";

    abstract void onPreFinish();

    @Override
    public void finish() {
        Log.d(TAG, "finish: 开始执行onPreFinish()");
        onPreFinish();
        Log.d(TAG, "finish: 执行finish");
        super.finish();
    }


}
