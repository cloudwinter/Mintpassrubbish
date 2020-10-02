package com.inprintech.mintpassrubbish.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.arcsoft.face.FaceEngine;
import com.arcsoft.face.enums.DetectFaceOrientPriority;

public class ConfigUtil {
    private static final String APP_NAME = "ArcFaceDemo";
    private static final String TRACK_ID = "trackID";
    private static final String FT_ORIENT = "ftOrient";

    public static void setTrackId(Context context, int trackId) {
        if (context == null) {
            return;
        }
        SharedPreferences sharedPreferences = context.getSharedPreferences(APP_NAME, Context.MODE_PRIVATE);
        sharedPreferences.edit()
                .putInt(TRACK_ID, trackId)
                .apply();
    }

    public static int getTrackId(Context context){
        if (context == null){
            return 0;
        }
        SharedPreferences sharedPreferences = context.getSharedPreferences(APP_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getInt(TRACK_ID,0);
    }
    public static void setFtOrient(Context context, int ftOrient) {
        if (context == null) {
            return;
        }
        SharedPreferences sharedPreferences = context.getSharedPreferences(APP_NAME, Context.MODE_PRIVATE);
        sharedPreferences.edit()
                .putInt(FT_ORIENT, ftOrient)
                .apply();
    }

    public static DetectFaceOrientPriority getFtOrient(Context context){
        if (context == null){
            return DetectFaceOrientPriority.ASF_OP_270_ONLY;
        }
        SharedPreferences sharedPreferences = context.getSharedPreferences(APP_NAME, Context.MODE_PRIVATE);
        int priority =  sharedPreferences.getInt(FT_ORIENT, DetectFaceOrientPriority.ASF_OP_270_ONLY.getPriority());
        if (priority == 1) {
            return DetectFaceOrientPriority.ASF_OP_0_ONLY;
        } else if (priority == 2) {
            return DetectFaceOrientPriority.ASF_OP_90_ONLY;
        } else if (priority == 3) {
            return DetectFaceOrientPriority.ASF_OP_270_ONLY;
        } else if (priority == 4) {
            return DetectFaceOrientPriority.ASF_OP_180_ONLY;
        } else if (priority == 5) {
            return DetectFaceOrientPriority.ASF_OP_ALL_OUT;
        }
        return DetectFaceOrientPriority.ASF_OP_270_ONLY;
    }
}
