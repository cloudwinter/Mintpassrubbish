package com.inprintech.mintpassrubbish.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.inprintech.mintpassrubbish.activity.MainActivity;

public class SelfStartingReceiver extends BroadcastReceiver {
    private static final String TAG = "SelfStartingReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(TAG, "onReceive intent = " + intent.toString());
        Intent welcomeIntent = new Intent(context, MainActivity.class);
        welcomeIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(welcomeIntent);
        Log.d(TAG,"SelfStartingReceiver 跳转到MainActivity");
    }
}
