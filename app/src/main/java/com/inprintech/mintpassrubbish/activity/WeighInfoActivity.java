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
import com.inprintech.mintpassrubbish.RunningEnvironment;
import com.inprintech.mintpassrubbish.utils.Urls;
import com.inprintech.mintpassrubbish.utils.Utils;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.cache.CacheMode;
import com.lzy.okgo.callback.StringCallback;
import com.lzy.okgo.model.Response;
import com.lzy.okgo.request.base.Request;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import interfaces.heweather.com.interfacesmodule.bean.Code;
import interfaces.heweather.com.interfacesmodule.bean.weather.now.Now;
import interfaces.heweather.com.interfacesmodule.bean.weather.now.NowBase;
import interfaces.heweather.com.interfacesmodule.view.HeConfig;
import interfaces.heweather.com.interfacesmodule.view.HeWeather;

public class WeighInfoActivity extends BaseActivity {
    private static final String TAG = "WeighInfoActivity";

    private TextView tvWeightInfo;
    private TextView tvHarmfulGarbage, tvHarmfulWeigh;
    private TextView tvRecyclableGarbage, tvRecyclableWeigh;
    private TextView tvWetGarbage, tvWetWeigh;
    private TextView tvDryGarbage, tvDryWeigh;
    /**
     * 头部天气、定位、时间信息
     */
    private TextView tvCity, tvCityen, tvWeather;
    private TextView tvTime;
    private ImageView imgWeathericon;
    //居民身份信息
    private TextView tvResidentInfo, tvName, tvcardId, tvPhone, tvJifen;

    private String idCard;
    private int Weight_1 = 0, Weight_2 = 0, Weight_3 = 0, Weight_4 = 0;

    SharedPreferences weight_sp = null;
    private TimeThread timeThread;

    private String macCode = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weigh_info);
        Log.i("aa", "onCreate: ---WeighInfoActivity---");
        initView();
    }

    private void initView() {
        tvWeightInfo = findViewById(R.id.tv_weight_info);
        tvHarmfulGarbage = findViewById(R.id.tv_harmful_weight);
        tvRecyclableGarbage = findViewById(R.id.tv_recyclable_weight);
        tvWetGarbage = findViewById(R.id.tv_wet_weight);
        tvDryGarbage = findViewById(R.id.tv_dry_weight);
        tvHarmfulWeigh = findViewById(R.id.tv_harmful_w);
        tvRecyclableWeigh = findViewById(R.id.tv_recyclable_w);
        tvWetWeigh = findViewById(R.id.tv_wet_w);
        tvDryWeigh = findViewById(R.id.tv_dry_w);
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
        tvWeightInfo.setTypeface(typeface);
        tvResidentInfo.setTypeface(typeface);

        getTime();

        SharedPreferences sp_mac = getSharedPreferences("MAC_CODE", MODE_PRIVATE);
        macCode = sp_mac.getString("macCode", "");

        SharedPreferences sp_city = getSharedPreferences("CITY", MODE_PRIVATE);
        String city = sp_city.getString("city", "");
        tvCity.setText(city);

        HeConfig.init("HE1908261119461144", "458fb44d20bb41c59ac0d35b72eea65f");
        HeConfig.switchToFreeServerNode();
        HeWeather.getWeatherNow(WeighInfoActivity.this, city, nowBeanListener);

        SharedPreferences user_sp = getSharedPreferences("USER_TOKEN", MODE_PRIVATE);
        String name = user_sp.getString("name", "");
        idCard = user_sp.getString("tid", "");
        String phone = user_sp.getString("phone", "");
        String jifen = user_sp.getString("jifen", "");
        tvName.setText(name);
        tvcardId.setText(idCard);
        tvPhone.setText(phone);
        tvJifen.setText(jifen);

        weight_sp = getSharedPreferences("garbage_weight", MODE_PRIVATE);
        Weight_1 = weight_sp.getInt("Weight_1", 0);
        Weight_2 = weight_sp.getInt("Weight_2", 0);
        Weight_3 = weight_sp.getInt("Weight_3", 0);
        Weight_4 = weight_sp.getInt("Weight_4", 0);
        Log.i(TAG, "initView: --Weight_1--" + Weight_1);
        Log.i(TAG, "initView: --Weight_2--" + Weight_2);
        Log.i(TAG, "initView: --Weight_3--" + Weight_3);
        Log.i(TAG, "initView: --Weight_4--" + Weight_4);
        int color1, color2;
        if (Weight_1 < 5) {
            Weight_1 = 0;
            tvHarmfulGarbage.setText("— —");
            color1 = getResources().getColor(R.color.huise);
            color2 = getResources().getColor(R.color.huise);
        } else if (Weight_1 > 200000) {
            Weight_1 = 200000;
            tvHarmfulGarbage.setText(Weight_1 + "g");
            color1 = getResources().getColor(R.color.hongse);
            color2 = getResources().getColor(R.color.lvse);
        } else {
            tvHarmfulGarbage.setText(Weight_1 + "g");
            color1 = getResources().getColor(R.color.hongse);
            color2 = getResources().getColor(R.color.lvse);
        }
        tvHarmfulGarbage.setTextColor(color1);
        tvHarmfulWeigh.setTextColor(color2);
        if (Weight_2 < 5) {
            Weight_2 = 0;
            tvRecyclableGarbage.setText("— —");
            color1 = getResources().getColor(R.color.huise);
            color2 = getResources().getColor(R.color.huise);
        } else if (Weight_2 > 200000) {
            Weight_2 = 200000;
            tvRecyclableGarbage.setText(Weight_2 + "g");
            color1 = getResources().getColor(R.color.hongse);
            color2 = getResources().getColor(R.color.lvse);
        } else {
            tvRecyclableGarbage.setText(Weight_2 + "g");
            color1 = getResources().getColor(R.color.hongse);
            color2 = getResources().getColor(R.color.lvse);
        }
        tvRecyclableGarbage.setTextColor(color1);
        tvRecyclableWeigh.setTextColor(color2);
        if (Weight_3 < 5) {
            Weight_3 = 0;
            tvWetGarbage.setText("— —");
            color1 = getResources().getColor(R.color.huise);
            color2 = getResources().getColor(R.color.huise);
        } else if (Weight_3 > 200000) {
            Weight_3 = 200000;
            tvWetGarbage.setText(Weight_3 + "g");
            color1 = getResources().getColor(R.color.hongse);
            color2 = getResources().getColor(R.color.lvse);
        } else {
            tvWetGarbage.setText(Weight_3 + "g");
            color1 = getResources().getColor(R.color.hongse);
            color2 = getResources().getColor(R.color.lvse);
        }
        tvWetGarbage.setTextColor(color1);
        tvWetWeigh.setTextColor(color2);

        if (Weight_4 < 5) {
            Weight_4 = 0;
            color1 = getResources().getColor(R.color.huise);
            color2 = getResources().getColor(R.color.huise);
            tvDryGarbage.setText("— —");
        } else if (Weight_4 > 200000) {
            Weight_4 = 200000;
            tvDryGarbage.setText(Weight_4 + "g");
            color1 = getResources().getColor(R.color.hongse);
            color2 = getResources().getColor(R.color.lvse);
        } else {
            tvDryGarbage.setText(Weight_4 + "g");
            color1 = getResources().getColor(R.color.hongse);
            color2 = getResources().getColor(R.color.lvse);
        }
        tvDryGarbage.setTextColor(color1);
        tvDryWeigh.setTextColor(color2);
        Log.i(TAG, "initView: --Weight_1--" + Weight_1);
        Log.i(TAG, "initView: --Weight_2--" + Weight_2);
        Log.i(TAG, "initView: --Weight_3--" + Weight_3);
        Log.i(TAG, "initView: --Weight_4--" + Weight_4);
        postgetTypeWeigh(Urls.address, String.valueOf(Weight_1), String.valueOf(Weight_2), String.valueOf(Weight_3), String.valueOf(Weight_4));
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

    @Override
    protected void onDestroy() {
        super.onDestroy();

    }

    private void getTime() {
        timeThread = new TimeThread();
        timeThread.start();
    }

    @Override
    void onPreFinish() {
        OkGo.getInstance().cancelTag(TAG);
        mHandler.removeCallbacksAndMessages(null);
        timeThread.close();
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

    private void postgetTypeWeigh(String url, String gan, String shi, String du, String other) {
        OkGo.<String>post(url)
                .tag(TAG)
                .cacheMode(CacheMode.DEFAULT)
                .params("C_type", "Laji_Update")
                .params("C_data1", macCode)
                .params("C_data2", idCard)
                .params("C_data3", gan)
                .params("C_data4", shi)
                .params("C_data5", du)
                .params("C_data6", other)
                .execute(new StringCallback() {
                    @Override
                    public void onStart(Request<String, ? extends Request> request) {
                        super.onStart(request);
                        Log.i(TAG, "onStart: **---" + request.getUrl() + request.toString());
                    }

                    @Override
                    public void onSuccess(Response<String> response) {
                        Log.i(TAG, "onSuccess: -----" + response.body());
                        try {
                            JSONObject jsonObject = new JSONObject(response.body());
                            String jifen = jsonObject.getString("Laji_Jifen");
                            String gan = jsonObject.getString("Laji_Jifen1");
                            String shi = jsonObject.getString("Laji_Jifen2");
                            String du = jsonObject.getString("Laji_Jifen3");
                            String other = jsonObject.getString("Laji_Jifen4");
                            Log.i(TAG, "onSuccess: ---gan--" + gan + "---jifen--" + jifen);
                            Log.i(TAG, "onSuccess: ---shi--" + shi);
                            Log.i(TAG, "onSuccess: ---du--" + du);
                            Log.i(TAG, "onSuccess: ---other--" + other);
                            SharedPreferences shared = RunningEnvironment.sAppContext.getSharedPreferences("JIFEN", MODE_PRIVATE);
                            SharedPreferences.Editor editor = shared.edit();
                            editor.putString("gan", gan);
                            editor.putString("shi", shi);
                            editor.putString("du", du);
                            editor.putString("other", other);
                            editor.commit();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onFinish() {
                        super.onFinish();
                        if (isFinishing()) {
                            return;
                        }
                        final Intent localIntent = new Intent(WeighInfoActivity.this, ScoringInfoActivity.class);
                        Timer timer = new Timer();
                        TimerTask tast = new TimerTask() {
                            @Override
                            public void run() {
                                startActivity(localIntent);
                                finish();
                            }
                        };
                        timer.schedule(tast, 4000);
                    }

                    @Override
                    public void onError(Response<String> response) {
                        super.onError(response);
                        Log.i(TAG, "onError: " + response.message());
                    }
                });
    }
}
