package com.inprintech.mintpassrubbish.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;

import com.inprintech.mintpassrubbish.MyApplication;
import com.inprintech.mintpassrubbish.R;
import com.inprintech.mintpassrubbish.RunningEnvironment;
import com.inprintech.mintpassrubbish.utils.CountDownTimerUtils;
import com.inprintech.mintpassrubbish.utils.Utils;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;

import cn.wch.ch34xuartdriver.CH34xUARTDriver;
import interfaces.heweather.com.interfacesmodule.bean.Code;
import interfaces.heweather.com.interfacesmodule.bean.weather.now.Now;
import interfaces.heweather.com.interfacesmodule.bean.weather.now.NowBase;
import interfaces.heweather.com.interfacesmodule.view.HeConfig;
import interfaces.heweather.com.interfacesmodule.view.HeWeather;

public class DustbinTypeActivity extends BaseActivity {
    private static final String TAG = "DustbinTypeActivity";

    // 关门信号
    private static final int MSG_WHAT_CLOSE = 101;
    // 关门倒计时
    private static final int MSG_WHAT_CLOSE_TIME = 102;
    // 跳转到称重
    private static final int MSG_WHAT_TURN_NEXT = 103;
    // 关门默认倒计时
    private static final int CLOSE_COUNT_DEFAULT_TIMES = 15;
    // 是否发送了关门信号
    private boolean isSendCloseSignal;

    private TextView tvCountDown;
    private TextView tvTips;
    private TextView etHarmfulGarbage, etHarmfulGarbageDescribe;
    private TextView etRecyclableGarbage, etRecyclableGarbageDescribe;
    private TextView etWetGarbage, etWetGarbageDescribe;
    private TextView etDryGarbage, etDryGarbageDescribe;
    private LinearLayout llHarmfulGarbage, llRecyclableGarbage, llWetGarbage, llDryGarbage;
    /* 头部天气、定位、时间信息 */
    private TextView tvCity, tvCityen, tvWeather;
    private TextView tvTime;
    private ImageView imgWeathericon;
    /* 居民身份信息 */
    private TextView tvResidentInfo, tvName, tvcardId, tvPhone, tvJifen;

    private TimeThread timeThread = null;
    private CountDownTimerUtils countDownTimerUtils = null;

    private int num = 0;
    private int key_one = 0;
    private int key_two = 0;
    private int key_three = 0;
    private int key_four = 0;
    private int key1 = 0, key2 = 0, key3 = 0, key4 = 0;
    private int Weight_1 = 0, Weight_2 = 0, Weight_3 = 0, Weight_4 = 0;
    private int l1 = 0, l2 = 0, l3 = 0, l4 = 0;
    private int i1 = 0, i2 = 0, i3 = 0, i4 = 0;
    private int j1 = 0, j2 = 0, j3 = 0, j4 = 0;
    private int k1 = 0, k2 = 0, k3 = 0, k4 = 0;
    private float one = (float) 0.00;
    private float two = (float) 0.00;
    private float three = (float) 0.00;
    private float four = (float) 0.00;
    private long intentTime = 16000L;
    private DustbinTypeActivity activity;
    private boolean isOpen;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate: ");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dustbin_type);
        MyApplication.driver = new CH34xUARTDriver((UsbManager)
                getSystemService(Context.USB_SERVICE), RunningEnvironment.sAppContext, Utils.ACTION_USB_PERMISSION);
        initView();
        activity = this;
    }

    /**
     * 初始化控件
     */
    private void initView() {
        tvCountDown = findViewById(R.id.tv_count_down);
        tvTips = findViewById(R.id.tv_tips);
        etHarmfulGarbage = findViewById(R.id.et_harmful_garbage);
        etHarmfulGarbageDescribe = findViewById(R.id.et_harmful_garbage_describe);
        etRecyclableGarbage = findViewById(R.id.et_recyclable_garbage);
        etRecyclableGarbageDescribe = findViewById(R.id.et_recyclable_garbage_describe);
        etWetGarbage = findViewById(R.id.et_wet_garbage);
        etWetGarbageDescribe = findViewById(R.id.et_wet_garbage_describe);
        etDryGarbage = findViewById(R.id.et_dry_garbage);
        etDryGarbageDescribe = findViewById(R.id.et_dry_garbage_describe);
        llHarmfulGarbage = findViewById(R.id.ll_harmful_garbage);
        llRecyclableGarbage = findViewById(R.id.ll_recyclable_garbage);
        llWetGarbage = findViewById(R.id.ll_wet_garbage);
        llDryGarbage = findViewById(R.id.ll_dry_garbage);
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
        tvTips.setTypeface(typeface);
        tvResidentInfo.setTypeface(typeface);

        //获取时间
        getTime();
        //15s不操作倒计时
        countDownTimerUtils = new CountDownTimerUtils(tvCountDown, 15000, 1000,
                DustbinTypeActivity.this);
        countDownTimerUtils.start();

        SharedPreferences sp = getSharedPreferences("USER_TOKEN", MODE_PRIVATE);
        String name = sp.getString("name", "");
        String idcard = sp.getString("tid", "");
        String phone = sp.getString("phone", "");
        String jifen = sp.getString("jifen", "");
        tvName.setText(name);
        tvcardId.setText(idcard);
        tvPhone.setText(phone);
        tvJifen.setText(jifen);

        SharedPreferences sp_city = getSharedPreferences("CITY", MODE_PRIVATE);
        String city = sp_city.getString("city", "");
        tvCity.setText(city);

        SharedPreferences sp_calibration = getSharedPreferences("calibration", MODE_PRIVATE);
        one = sp_calibration.getFloat("one", (float) 0.00);
        two = sp_calibration.getFloat("two", (float) 0.00);
        three = sp_calibration.getFloat("three", (float) 0.00);
        four = sp_calibration.getFloat("four", (float) 0.00);

//        String shareStr = "one："+one+" two："+two+" three："+three+" four："+four;
//        Toast.makeText(DustbinTypeActivity.this, shareStr, Toast.LENGTH_LONG).show();

        HeConfig.init("HE1908261119461144", "458fb44d20bb41c59ac0d35b72eea65f");
        HeConfig.switchToFreeServerNode();
        HeWeather.getWeatherNow(DustbinTypeActivity.this, city, nowBeanListener);

        openDevice();

    }


    private void openDevice() {
        if (!isOpen) {
            int retval = MyApplication.driver.ResumeUsbPermission();
            Log.i(TAG, "initView:   retval--" + retval);
            if (retval == 0) {
                retval = MyApplication.driver.ResumeUsbList();
                Log.i(TAG, "initView:   retval--" + retval);
                if (retval == -1) {
                    MyApplication.driver.CloseDevice();
                } else if (retval == 0) {
                    if (MyApplication.driver.mDeviceConnection != null) {
                        if (!MyApplication.driver.UartInit()) {//对串口设备进行初始化操作
                            Log.i(TAG, "initView:  Initialization failed!");
                            return;
                        }
                        Log.i(TAG, "initView:  Device opened!");
                        if (MyApplication.driver.SetConfig(Utils.baudRate, Utils.dataBit, Utils.stopBit, Utils.parity, Utils.flowControl)) {
                            Log.i(TAG, "initView:  Config successfully");
                            isOpen = true;
                            new readThread().start();//开启读线程读取串口接收的数据
                        } else {
                            Log.i(TAG, "initView:  Config failed!");
                        }
                    } else {
                        Log.i(TAG, "initView:  Open failed!");
                    }
                }
            }
        } else {
            isOpen = false;
        }
    }


    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            SharedPreferences shared = getSharedPreferences("garbage_weight", MODE_PRIVATE);
            SharedPreferences.Editor editor = shared.edit();
            int color1 = getResources().getColor(R.color.baise);
            String strMsg = (String) msg.obj;
            Log.i(TAG, "handleMessage: ----str--" + strMsg);
            if (containKeySignal(strMsg)) {
                // 每次收到信号都重新计时10S
                countTimes = CLOSE_COUNT_DEFAULT_TIMES;
                num = num + 1;
                if (num == 1) {
                    cancelCountDownUtils();
                    openCloseRunnable = true;
                    RunningEnvironment.sThreadPool.execute(mCloseRunnable);
                }
            }
            if (strMsg.contains("key_1")) {
                llHarmfulGarbage.setBackgroundResource(R.drawable.round_face_four_one);
                etHarmfulGarbage.setTextColor(color1);
                etHarmfulGarbageDescribe.setTextColor(color1);
                key_one = key_one + 1;
                if (key_one == 1) {
                    onSend(DustbinTypeActivity.this, Utils.OPEN_WEIGHT1);
                }
            }
            if (strMsg.contains("key_2")) {
                llRecyclableGarbage.setBackgroundResource(R.drawable.round_face_four_three);
                etRecyclableGarbage.setTextColor(color1);
                etRecyclableGarbageDescribe.setTextColor(color1);
                key_two = key_two + 1;
                if (key_two == 1) {
                    onSend(DustbinTypeActivity.this, Utils.OPEN_WEIGHT2);
                }
            }
            if (strMsg.contains("key_3")) {
                llWetGarbage.setBackgroundResource(R.drawable.round_face_four_four);
                etWetGarbage.setTextColor(color1);
                etWetGarbageDescribe.setTextColor(color1);
                key_three = key_three + 1;
                if (key_three == 1) {
                    onSend(DustbinTypeActivity.this, Utils.OPEN_WEIGHT3);
                }
            }
            if (strMsg.contains("key_4")) {
                llDryGarbage.setBackgroundResource(R.drawable.round_face_four_two);
                etDryGarbage.setTextColor(color1);
                etDryGarbageDescribe.setTextColor(color1);
                key_four = key_four + 1;
                if (key_four == 1) {
                    onSend(DustbinTypeActivity.this, Utils.OPEN_WEIGHT4);
                }
            }
            try {
                if (strMsg.contains("Weight_1")) {
                    JSONObject jsonObject = new JSONObject(strMsg);
                    Weight_1 = jsonObject.getInt("Weight_1");
                    Log.i(TAG, "run: --Weight_1--" + Weight_1);
                    l1 = l1 + 1;
                    Log.i(TAG, "run: ----Weight_1---l1l--" + l1);
                    if (l1 == 1) {
                        i1 = Weight_1;
                        Log.i(TAG, "run:--ii1ii--" + i1);
                    }
                    if (l1 == 2) {
                        j1 = Weight_1;
                        Log.i(TAG, "run: --jj1jj--" + j1);

                    }
                    if (l1 == 2) {
                        l1 = 0;
                        if (one == 0) {
                            one = 1;
                            Log.i(TAG, "run: --one--" + one);
                        }
                        Log.i(TAG, "run: --one校准倍数--" + one);
                        int ji1 = j1 - i1;
                        if (ji1 <= 0) {
                            ji1 = 0;
                        }
                        Log.i(TAG, "run: 1号垃圾桶--  " + ji1);
                        k1 = (int) (ji1 / one);
                        if (k1 > 200000) {
                            k1 = 200000;
                            Log.i(TAG, "run: --k1--" + k1);
                        }
                        Log.i(TAG, "run: --k1--" + k1);
                    }
                }
                if (strMsg.contains("Weight_2")) {
                    JSONObject jsonObject = new JSONObject(strMsg);
                    Weight_2 = jsonObject.getInt("Weight_2");
                    Log.i(TAG, "run: --Weight_2--" + Weight_2);
                    l2 = l2 + 1;
                    Log.i(TAG, "run: ----Weight_2---l2l--" + l2);
                    if (l2 == 1) {
                        i2 = Weight_2;
                        Log.i(TAG, "run:--ii2ii--" + i2);
                    }
                    if (l2 == 2) {
                        j2 = Weight_2;
                        Log.i(TAG, "run: --jj2jj--" + j2);

                    }
                    if (l2 == 2) {
                        l2 = 0;
                        if (two == 0) {
                            two = 1;
                            Log.i(TAG, "run: --two--" + two);
                        }
                        Log.i(TAG, "run: --two校准倍数--" + two);
                        int ji2 = j2 - i2;
                        if (ji2 <= 0) {
                            ji2 = 0;
                        }
                        Log.i(TAG, "run: 2号垃圾桶--  " + ji2);
                        k2 = (int) (ji2 / two);
                        if (k2 > 200000) {
                            k2 = 200000;
                            Log.i(TAG, "run: --k2--" + k2);
                        }
                        Log.i(TAG, "run: --k2--" + k2);
                    }
                }
                if (strMsg.contains("Weight_3")) {
                    JSONObject jsonObject = new JSONObject(strMsg);
                    Weight_3 = jsonObject.getInt("Weight_3");
                    Log.i(TAG, "run: --Weight_3--" + Weight_3);
                    l3 = l3 + 1;
                    Log.i(TAG, "run: ----Weight_3---l3l--" + l3);
                    if (l3 == 1) {
                        i3 = Weight_3;
                        Log.i(TAG, "run:--ii3ii--" + i3);
                    }
                    if (l3 == 2) {
                        j3 = Weight_3;
                        Log.i(TAG, "run: --jj3jj--" + j3);
                    }
                    if (l3 == 2) {
                        l3 = 0;
                        if (three == 0) {
                            three = 1;
                            Log.i(TAG, "run: --three--" + three);
                        }
                        Log.i(TAG, "run: --three校准倍数--" + three);
                        int ji3 = j3 - i3;
                        if (ji3 <= 0) {
                            ji3 = 0;
                        }
                        Log.i(TAG, "run: 3号垃圾桶--  " + ji3);
                        k3 = (int) (ji3 / three);
                        if (k3 > 200000) {
                            k3 = 200000;
                            Log.i(TAG, "run: --k3--" + k3);
                        }
                        Log.i(TAG, "run: --k3--" + k3);
                    }
                }
                if (strMsg.contains("Weight_4")) {
                    JSONObject jsonObject = new JSONObject(strMsg);
                    Weight_4 = jsonObject.getInt("Weight_4");
                    Log.i(TAG, "run: ----Weight_4---" + Weight_4);
                    l4 = l4 + 1;
                    Log.i(TAG, "run: ----Weight_4---l4l--" + l4);
                    if (l4 == 1) {
                        i4 = Weight_4;
                        Log.i(TAG, "run:--ii4ii--" + i4);
                    }
                    if (l4 == 2) {
                        j4 = Weight_4;
                        Log.i(TAG, "run: --jj4jj--" + j4);

                    }
                    if (l4 == 2) {
                        l4 = 0;
                        if (four == 0) {
                            four = 1;
                            Log.i(TAG, "run: --four--" + four);
                        }
                        Log.i(TAG, "run: --four校准倍数--" + four);
                        int ji4 = j4 - i4;
                        if (ji4 <= 0) {
                            ji4 = 0;
                        }
                        Log.i(TAG, "run: 4号垃圾桶--  " + ji4);
                        k4 = (int) (ji4 / four);
                        if (k4 > 200000) {
                            k4 = 200000;
                            Log.i(TAG, "run: --44k--" + k4);
                        }
                        Log.i(TAG, "run: --44k--" + k4);
                    }
                }
                editor.putInt("Weight_1", k1);
                editor.putInt("Weight_2", k2);
                editor.putInt("Weight_3", k3);
                editor.putInt("Weight_4", k4);
                editor.commit();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };


    private Handler mCloseHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == MSG_WHAT_CLOSE) {
                // 发送关门信号
                onSend(DustbinTypeActivity.this, Utils.CLOSE_WEIGHT);
                sendEmptyMessageDelayed(MSG_WHAT_TURN_NEXT, 1500L);
            } else if (msg.what == MSG_WHAT_TURN_NEXT) {
                // 1.5S后跳转到下一个界面
                isSendCloseSignal = true;
                Intent intent = new Intent(DustbinTypeActivity.this, WeighInfoActivity.class);
                startActivity(intent);
                finish();
            } else if (msg.what == MSG_WHAT_CLOSE_TIME) {
                int closeLeftTime = msg.arg1;
                tvCountDown.setText(closeLeftTime + "后关门");

            }
        }
    };

    // 倒计时
    private int countTimes = 15;
    private boolean openCloseRunnable = false;

    private Runnable mCloseRunnable = new Runnable() {
        @Override
        public void run() {
            while (openCloseRunnable) {
                if (countTimes == 0) {
                    // 倒计时结束发送关门信号
                    mCloseHandler.sendEmptyMessage(MSG_WHAT_CLOSE);
                    break;
                }
                Message msg = mCloseHandler.obtainMessage(MSG_WHAT_CLOSE_TIME);
                msg.arg1 = countTimes;
                mCloseHandler.sendMessage(msg);
                try {
                    Thread.sleep(1000L);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                countTimes--;

            }
        }
    };


    private void cancelCountDownUtils() {
        if (countDownTimerUtils != null) {
            countDownTimerUtils.cancel();
            // 置空倒计时
            tvCountDown.setText("");
        }
    }

    private CountDownTimer countDownTimer = new CountDownTimer(intentTime, 1000) {
        @Override
        public void onTick(long l) {
            int remainTime = (int) (l / 1000L);
            Log.i(TAG, "onTick: ---" + remainTime);
            if (remainTime == 2) {
                onSend(DustbinTypeActivity.this, Utils.CLOSE_WEIGHT);
            }
        }

        @Override
        public void onFinish() {
            Intent intent = new Intent(DustbinTypeActivity.this, WeighInfoActivity.class);
            startActivity(intent);
            finish();
        }
    };

    /**
     * 发送消息
     *
     * @param context
     * @param sendContent
     */
    public static void onSend(Context context, String sendContent) {
        byte[] to_send = Utils.toByteArray2(sendContent);
        //写数据，第一个参数为需要发送的字节数组，第二个参数为需要发送的字节长度，返回实际发送的字节长度
        int retval = MyApplication.driver.WriteData(to_send, to_send.length);
        if (retval < 0) {
            Toast.makeText(context, "Write failed!", Toast.LENGTH_SHORT).show();
            MyApplication.driver.WriteData(to_send, to_send.length);
            MyApplication.driver.WriteData(to_send, to_send.length);
            MyApplication.driver.WriteData(to_send, to_send.length);
        } else {
            Log.i(TAG, "onSend: --发送成功--");
        }
    }

    public void preFinish() {
        openCloseRunnable = false;
        timeThread.close();
        mHandler.removeCallbacksAndMessages(null);
        mCloseHandler.removeCallbacksAndMessages(null);
        handler.removeCallbacksAndMessages(null);
        countDownTimerUtils.cancel();
        countDownTimerUtils = null;
        countDownTimer.cancel();
        closeDevice();
    }


    @Override
    void onPreFinish() {
        preFinish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "onDestroy: ");

    }

    private void closeDevice() {
        isOpen = false;
        MyApplication.driver.CloseDevice();
    }


    private HeWeather.OnResultWeatherNowBeanListener nowBeanListener = new HeWeather.OnResultWeatherNowBeanListener() {
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
                    removeMessages(1);
                    long time = System.currentTimeMillis();
                    Date date = new Date(time);
                    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    String data = format.format(date);
                    tvTime.setText(data);
                    break;
            }
        }
    };

    private class readThread extends Thread {
        @Override
        public void run() {
            byte[] buffer = new byte[23];
            while (true) {
                Message msg = Message.obtain();
                //Log.i(TAG, "run:   isopen--" + isOpen);
                if (!isOpen) {
                    break;
                }
                int length = MyApplication.driver.ReadData(buffer, 23);
                if (length > 0) {
                    //以16进制输出
                    //String recv = toHexString(buffer, length);
                    //以字符串形式输出
                    String recv = new String(buffer, 0, length);
                    Log.i(TAG, "run:  recv--" + recv);
                    msg.obj = recv;
                    if (!isSendCloseSignal) {
                        // 未发送关门信号可以处理消息
                        handler.sendMessage(msg);
                    }
                }
            }
        }
    }


    private boolean containKeySignal(String msg) {
        if (TextUtils.isEmpty(msg)) {
            return false;
        }
        if (msg.contains("key_1")
                || msg.contains("key_2")
                || msg.contains("key_3")
                || msg.contains("key_4")) {
            return true;
        }
        return false;
    }

}
