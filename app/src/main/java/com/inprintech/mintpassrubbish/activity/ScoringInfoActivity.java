package com.inprintech.mintpassrubbish.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;

import com.inprintech.mintpassrubbish.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import interfaces.heweather.com.interfacesmodule.bean.Code;
import interfaces.heweather.com.interfacesmodule.bean.weather.now.Now;
import interfaces.heweather.com.interfacesmodule.bean.weather.now.NowBase;
import interfaces.heweather.com.interfacesmodule.view.HeConfig;
import interfaces.heweather.com.interfacesmodule.view.HeWeather;

public class ScoringInfoActivity extends AppCompatActivity {
    private static final String TAG = "ScoringInfoActivity";

    private TextView tvScoring;
    private TextView tvHarmfulGarbage, tvHarmfulIntegral;
    private TextView tvRecyclableGarbage, tvRecyclableIntegral;
    private TextView tvWetGarbage, tvWetIntegral;
    private TextView tvDryGarbage, tvDryIntegral;
    /**
     * 头部天气、定位、时间信息
     */
    private TextView tvCity, tvCityen, tvWeather;
    private TextView tvTime;
    private ImageView imgWeathericon;
    //居民身份信息
    private TextView tvResidentInfo, tvName, tvcardId, tvPhone, tvJifen;

    private String idcard;

    private TimeThread timeThread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scoring_info);
        Log.i("aa", "onCreate: ----ScoringInfoActivity----");
        initView();
    }

    private void initView() {
        tvScoring = findViewById(R.id.tv_scoring_info);
        tvHarmfulGarbage = findViewById(R.id.tv_harmful_integral);
        tvRecyclableGarbage = findViewById(R.id.tv_recyclable_integral);
        tvWetGarbage = findViewById(R.id.tv_wet_integral);
        tvDryGarbage = findViewById(R.id.tv_dry_integral);
        tvHarmfulIntegral = findViewById(R.id.tv_harmful_s);
        tvRecyclableIntegral = findViewById(R.id.tv_recyclable_s);
        tvWetIntegral = findViewById(R.id.tv_wet_s);
        tvDryIntegral = findViewById(R.id.tv_dry_s);
        tvCity = findViewById(R.id.tv_location);
        tvCityen = findViewById(R.id.tv_location_en);
        tvWeather = findViewById(R.id.tv_weather);
        tvTime = findViewById(R.id.tv_date_time);
        imgWeathericon = findViewById(R.id.img_weather);
        //居民身份信息控件
        tvResidentInfo = findViewById(R.id.tv_resident_info);
        tvName = findViewById(R.id.tv_name);
        tvcardId = findViewById(R.id.tv_idcard);
        tvPhone = findViewById(R.id.tv_phone);
        tvJifen = findViewById(R.id.tv_allintegral);

        Typeface typeface = ResourcesCompat.getFont(this, R.font.fangzheng_han_style);
        tvScoring.setTypeface(typeface);
        tvResidentInfo.setTypeface(typeface);

        getTime();

        SharedPreferences sp_city = getSharedPreferences("CITY", MODE_PRIVATE);
        String city = sp_city.getString("city", "");
        tvCity.setText(city);

        HeConfig.init("HE1908261119461144", "458fb44d20bb41c59ac0d35b72eea65f");
        HeConfig.switchToFreeServerNode();
        HeWeather.getWeatherNow(ScoringInfoActivity.this, city, nowBeanListener);

        SharedPreferences sp_usser = getSharedPreferences("USER_TOKEN", MODE_PRIVATE);
        String name = sp_usser.getString("name", "");
        idcard = sp_usser.getString("tid", "");
        String phone = sp_usser.getString("phone", "");
        String jifen = sp_usser.getString("jifen", "");
        tvName.setText(name);
        tvcardId.setText(idcard);
        tvPhone.setText(phone);
        tvJifen.setText(jifen);

        int color1, color2;
        SharedPreferences sp_jifen = getSharedPreferences("JIFEN", MODE_PRIVATE);
        String gan = sp_jifen.getString("gan", "");
        String shi = sp_jifen.getString("shi", "");
        String du = sp_jifen.getString("du", "");
        String other = sp_jifen.getString("other", "");
        if (gan.equals("0")) {
            tvHarmfulGarbage.setText("— —");
            color1 = getResources().getColor(R.color.huise);
            color2 = getResources().getColor(R.color.huise);
        } else {
            tvHarmfulGarbage.setText(gan + "分");
            color1 = getResources().getColor(R.color.hongse);
            color2 = getResources().getColor(R.color.lvse);
        }
        tvHarmfulGarbage.setTextColor(color1);
        tvHarmfulIntegral.setTextColor(color2);
        if (shi.equals("0")) {
            tvRecyclableGarbage.setText("— —");
            color1 = getResources().getColor(R.color.huise);
            color2 = getResources().getColor(R.color.huise);
        } else {
            tvRecyclableGarbage.setText(shi + "分");
            color1 = getResources().getColor(R.color.hongse);
            color2 = getResources().getColor(R.color.lvse);
        }
        tvRecyclableGarbage.setTextColor(color1);
        tvRecyclableIntegral.setTextColor(color2);
        if (du.equals("0")) {
            tvWetGarbage.setText("— —");
            color1 = getResources().getColor(R.color.huise);
            color2 = getResources().getColor(R.color.huise);
        } else {
            tvWetGarbage.setText(du + "分");
            color1 = getResources().getColor(R.color.hongse);
            color2 = getResources().getColor(R.color.lvse);
        }
        tvWetGarbage.setTextColor(color1);
        tvWetIntegral.setTextColor(color2);

        if (other.equals("0")) {
            tvDryGarbage.setText("— —");
            color1 = getResources().getColor(R.color.huise);
            color2 = getResources().getColor(R.color.huise);
        } else {
            tvDryGarbage.setText(other + "分");
            color1 = getResources().getColor(R.color.hongse);
            color2 = getResources().getColor(R.color.lvse);
        }
        tvDryGarbage.setTextColor(color1);
        tvDryIntegral.setTextColor(color1);

        final Intent localIntent = new Intent(ScoringInfoActivity.this, MainActivity.class);
        Timer timer = new Timer();
        TimerTask tast = new TimerTask() {
            @Override
            public void run() {
                startActivity(localIntent);//执行
                finish();
                Log.d(TAG,"ScoringInfoActivity onFinish  跳转到MainActivity");
            }
        };
        timer.schedule(tast, 5000);//5秒后
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mHandler.removeCallbacksAndMessages(null);
        timeThread.close();
    }

    HeWeather.OnResultWeatherNowBeanListener nowBeanListener = new HeWeather.OnResultWeatherNowBeanListener() {
        @Override
        public void onError(Throwable throwable) {
            Log.i(TAG, "onError: ---" + throwable.toString());
        }

        @Override
        public void onSuccess(Now now) {
            if (Code.OK.getCode().equalsIgnoreCase(now.getStatus())) {
                NowBase nowBase = now.getNow();
                String cond = nowBase.getCond_txt();//天气描述
                String tmp = nowBase.getTmp() + "℃";//温度
                Log.i(TAG, "onSuccess: cond:" + cond);
                Log.i(TAG, "onSuccess: tmp:" + tmp);
                tvWeather.setText(cond + "  " + tmp);
                if (cond.equals("阴")) {
                    imgWeathericon.setImageResource(R.drawable.overcast);
                }
                if (cond.equals("晴")) {
                    imgWeathericon.setImageResource(R.drawable.sunny);
                }
                if (cond.equals("多云")) {
                    imgWeathericon.setImageResource(R.drawable.cloudy);
                }
                if (cond.equals("小雨")) {
                    imgWeathericon.setImageResource(R.drawable.light_rain);
                }
                if (cond.equals("雷阵雨")) {
                    imgWeathericon.setImageResource(R.drawable.thunder_shower);
                }
                if (cond.equals("中雨")) {
                    imgWeathericon.setImageResource(R.drawable.moderate_rain);
                }
                if (cond.equals("大雨")) {
                    imgWeathericon.setImageResource(R.drawable.heavy_rain);
                }
            } else {
                String status = now.getStatus();
                Code code = Code.toEnum(status);
                Log.i(TAG, "onSuccess: failed code---" + code);
            }
        }
    };

    private void getTime() {
        timeThread = new TimeThread();
        timeThread.start();
    }

    private class TimeThread extends Thread {
        private boolean mRunning = false;
        @Override
        public void run() {
            mRunning = true;
            super.run();
            do {
                try {
                    Thread.sleep(1000);
                    Message msg = new Message();
                    msg.what = 1;
                    mHandler.sendMessage(msg);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } while (mRunning);
        }
        public void close() {
            mRunning = false;
        }
    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 1:
                    long time = System.currentTimeMillis();
                    Date date = new Date(time);
                    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    String data = format.format(date);
                    tvTime.setText(data);
                    break;
            }
        }
    };
}
