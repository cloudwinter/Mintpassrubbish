package com.inprintech.mintpassrubbish.activity;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.hardware.usb.UsbManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.provider.Settings;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.arcsoft.face.ErrorInfo;
import com.arcsoft.face.FaceEngine;
import com.inprintech.mintpassrubbish.MyApplication;
import com.inprintech.mintpassrubbish.R;
import com.inprintech.mintpassrubbish.RunningEnvironment;
import com.inprintech.mintpassrubbish.model.EntityVideo;
import com.inprintech.mintpassrubbish.utils.ConfigUtil;
import com.inprintech.mintpassrubbish.utils.Constants;
import com.inprintech.mintpassrubbish.utils.Urls;
import com.inprintech.mintpassrubbish.utils.Utils;
import com.inprintech.mintpassrubbish.utils.ZxingUtils;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.cache.CacheMode;
import com.lzy.okgo.callback.StringCallback;
import com.lzy.okgo.https.HttpsUtils;
import com.lzy.okgo.interceptor.HttpLoggingInterceptor;
import com.lzy.okgo.model.Response;
import com.lzy.okgo.request.base.Request;
import com.lzy.okgo.utils.OkLogger;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

import cn.wch.ch34xuartdriver.CH34xUARTDriver;
import interfaces.heweather.com.interfacesmodule.bean.Code;
import interfaces.heweather.com.interfacesmodule.bean.weather.now.Now;
import interfaces.heweather.com.interfacesmodule.bean.weather.now.NowBase;
import interfaces.heweather.com.interfacesmodule.view.HeConfig;
import interfaces.heweather.com.interfacesmodule.view.HeWeather;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import okhttp3.OkHttpClient;

public class MainActivity extends BaseActivity {

    private static final String TAG = "MainActivity";

    private static final int MSG_USB_KEY = 101;

    private VideoView videoTrailer;
    private TextView tvAddress;
    private TextView tvFacetips;
    private TextView tvNoCode;
    private EditText etIdcard;
    private ImageView imgQRCode;
    /**
     * 头部天气、定位、时间信息
     */
    private TextView tvCity, tvCityen, tvWeather;
    private TextView tvTime;
    private ImageView imgWeathericon;

    private int key = 0;
    private static final int EDIT_OK = 1;
    private static final int TIME_DATE = 2;
    private static final int QR_RETURN_LOGIN = 3;

    String city = null;
    String path = null;

    private AMapLocationClient mLocationClient = null;
    private AMapLocationClientOption mLocationOption = null;
    private TimeThread timeThread = null;

    private static final int ACTION_REQUEST_PERMISSIONS = 0x001;
    private static final String[] NEEDED_PERMISSIONS = new String[]{
            Manifest.permission.READ_PHONE_STATE
    };

    private boolean isOpen;
//    private Handler handler;

    private String macCode = null;

    private MainHandler etHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        etHandler = new MainHandler(this);


        requestReadExternalPermission();
        Log.i(TAG, "onCreate: ---MainActivity---");
        MyApplication.driver = new CH34xUARTDriver((UsbManager)
                getSystemService(Context.USB_SERVICE), MainActivity.this, Utils.ACTION_USB_PERMISSION);
        isOpen = false;
        initView();
        activeEngine();

    }



    private void requestReadExternalPermission() {
        if (Build.VERSION.SDK_INT >= 23) {
            int REQUEST_CODE_CONTACT = 101;
            String[] permissions = {
                    Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE};
            //验证是否许可权限
            for (String str : permissions) {
                if (checkSelfPermission(str) != PackageManager.PERMISSION_GRANTED) {
                    //申请权限
                    requestPermissions(permissions, REQUEST_CODE_CONTACT);
                    return;
                } else {
                    //这里就是权限打开之后自己要操作的逻辑
                }
            }
        }
    }

    /**
     * 初始化控件
     */
    private void initView() {

        ConfigUtil.setFtOrient(MainActivity.this, FaceEngine.ASF_OP_0_HIGHER_EXT);

        tvNoCode = findViewById(R.id.tv_no_qr_code);
        imgQRCode = findViewById(R.id.img_qr_code_login);
        etIdcard = findViewById(R.id.et_idcard);
        tvFacetips = findViewById(R.id.tv_face_tips);
        tvCity = findViewById(R.id.tv_location);
        tvCityen = findViewById(R.id.tv_location_en);
        tvWeather = findViewById(R.id.tv_weather);
        tvTime = findViewById(R.id.tv_date_time);
        videoTrailer = findViewById(R.id.video_trailer);
        tvAddress = findViewById(R.id.tv_address);
        imgWeathericon = findViewById(R.id.img_weather);

        tvTime.setOnClickListener(imgListener);
        videoTrailer.setOnPreparedListener(videoListener);

        SharedPreferences sp_mac = getSharedPreferences("MAC_CODE", MODE_PRIVATE);
        macCode = sp_mac.getString("macCode", "");

        if (macCode == null) {
            imgQRCode.setVisibility(View.GONE);
            tvNoCode.setVisibility(View.VISIBLE);
        } else {
            imgQRCode.setVisibility(View.VISIBLE);
            tvNoCode.setVisibility(View.GONE);
        }

        playVideo();//播放视频
        getTime();//获取时间
        postqrcode(Urls.address);//获取二维码

        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {//未开启定位权限
            //开启定位权限,200是标识码
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 200);
        } else {
            startLocaion();
            Log.i(TAG, "initView:   已开启定位权限");
        }
        Typeface typeface = ResourcesCompat.getFont(this, R.font.fangzheng_han_style);
        tvFacetips.setTypeface(typeface);

        etIdcard.setFilters(new InputFilter[]{new InputFilter.LengthFilter(10)});
        showSoftInputFromWindow(MainActivity.this, etIdcard);
        etIdcard.addTextChangedListener(textWatcher);

        openUsbDevice();

//        handler = new Handler() {
//            @Override
//            public void handleMessage(Message msg) {
//
//            }
//        };
    }

    private void openUsbDevice() {
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
                            Log.i(TAG, "onResume:  Initialization failed!");
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
                        Log.i(TAG, "onResume:  Open failed!");
                    }
                }
            }
        } else {
            isOpen = false;
        }
    }

    @Override
    void onPreFinish() {
        OkGo.getInstance().cancelTag(TAG);
        preFinish();
    }

    private class readThread extends Thread {
        @Override
        public void run() {
            byte[] buffer = new byte[23];
            while (true) {
                Message msg = Message.obtain();
                if (!isOpen) {
                    break;
                }
                int length = MyApplication.driver.ReadData(buffer, 23);
                if (length > 0) {
                    Log.d(TAG, "run: 接收到信号录入");
                    //以16进制输出
                    //String recv = toHexString(buffer, length);
                    //以字符串形式输出
                    String recv = new String(buffer, 0, length);
                    msg.obj = recv;
                    msg.what = MSG_USB_KEY;
                    etHandler.sendMessage(msg);
                }
            }
        }
    }

    private void closeDevice() {
        isOpen = false;
        MyApplication.driver.CloseDevice();
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause: 执行onPause 当前usb是否打开：" + isOpen);
        //closeDevice();
    }

    @Override
    protected void onRestart() {
        Log.d(TAG, "onRestart: 执行onRestart 当前usb是否打开：" + isOpen);
        openUsbDevice();
        super.onRestart();
    }

    public void activeEngine() {
        if (!checkPermissions(NEEDED_PERMISSIONS)) {
            ActivityCompat.requestPermissions(this, NEEDED_PERMISSIONS, ACTION_REQUEST_PERMISSIONS);
            return;
        }

        Observable.create(observableOnSubscribe)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(observer);

    }

    /**
     * 被观察者消息订阅
     */
    private ObservableOnSubscribe observableOnSubscribe = new ObservableOnSubscribe<Integer>(){

        @Override
        public void subscribe(ObservableEmitter<Integer> emitter) throws Exception {
            FaceEngine faceEngine = new FaceEngine();
            int activeCode = faceEngine.active(MainActivity.this, Constants.APP_ID, Constants.SDK_KEY);
            emitter.onNext(activeCode);
        }
    };


    /**
     * 观察者
     */
    private Observer observer = new Observer<Integer>(){

        @Override
        public void onSubscribe(Disposable d) {

        }

        @Override
        public void onNext(Integer activeCode) {
            if (activeCode == ErrorInfo.MOK) {
                Log.i(TAG, "onNext: 激活人脸引擎成功！");
            } else if (activeCode == ErrorInfo.MERR_ASF_ALREADY_ACTIVATED) {
                Log.i(TAG, "onNext: 引擎已激活，无需再次激活！");
            } else {
                Toast.makeText(MainActivity.this, "引擎激活失败，错误码为:" + activeCode, Toast.LENGTH_SHORT).show();
                Log.i(TAG, "onNext: 引擎激活失败，错误码为 --" + activeCode);
            }
        }

        @Override
        public void onError(Throwable e) {

        }

        @Override
        public void onComplete() {

        }
    };

    private boolean checkPermissions(String[] neededPermissions) {
        if (neededPermissions == null || neededPermissions.length == 0) {
            return true;
        }
        boolean allGranted = true;
        for (String neededPermission : neededPermissions) {
            allGranted &= ContextCompat.checkSelfPermission(this, neededPermission) == PackageManager.PERMISSION_GRANTED;
        }
        return allGranted;
    }

    private TextWatcher textWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            etHandler.removeCallbacks(mRunnable);
            //600毫秒没有输入认为输入完毕
            etHandler.postDelayed(mRunnable, 600);
        }

        @Override
        public void afterTextChanged(Editable editable) {
            Log.d(TAG, "afterTextChanged() -- " + editable.toString());
        }
    };

    /**
     * 播放视频
     */
    private void playVideo() {
        Intent intentPath = getIntent();
        path = intentPath.getStringExtra("path");
        if (path == null) {
            // 播放视频
            String path = getList().get(0).getPath();
            Log.i(TAG, "playVideo: ---" + path);
            videoTrailer.setVideoPath(path);
            videoTrailer.start();
        } else {
            videoTrailer.setVideoPath(path);
            videoTrailer.start();
        }
    }

    /**
     * 获取所有视频
     *
     * @return
     */
    private List<EntityVideo> getList() {
        List<EntityVideo> list = null;
        if (this != null) {
            Cursor cursor = this.getContentResolver().query(
                    MediaStore.Video.Media.EXTERNAL_CONTENT_URI, null, null,
                    null, null);
            if (cursor != null) {
                list = new ArrayList<>();
                while (cursor.moveToNext()) {
                    String path = cursor
                            .getString(cursor
                                    .getColumnIndexOrThrow(MediaStore.Video.Media.DATA));
                    int duration = cursor
                            .getInt(cursor
                                    .getColumnIndexOrThrow(MediaStore.Video.Media.DURATION));
                    EntityVideo video = new EntityVideo();
                    video.setPath(path);
                    video.setDuration(duration);
                    list.add(video);
                }
                cursor.close();
            }
        }
        return list;
    }



    private static class MainHandler extends Handler {

        WeakReference<MainActivity> weakReference = null;

        public MainHandler(MainActivity mainActivity) {
            weakReference = new WeakReference<>(mainActivity);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            MainActivity mainActivity = weakReference.get();
            if (mainActivity == null || mainActivity.isFinishing()) {
                return;
            }
            switch (msg.what) {
                case EDIT_OK:
                    removeMessages(EDIT_OK);
                    Log.d(TAG, "handleMessage() returned:输入完成 ");
                    break;
                case TIME_DATE:
                    removeMessages(TIME_DATE);
                    long time = System.currentTimeMillis();
                    Date date = new Date(time);
                    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    String dateTime = format.format(date);
                    mainActivity.tvTime.setText(dateTime);
                    break;
                case QR_RETURN_LOGIN:
                    removeMessages(QR_RETURN_LOGIN);
                    mainActivity.postqrcodelogin(Urls.address);
                    break;
                case MSG_USB_KEY:
                    String strMsg = (String) msg.obj;
                    Log.i(TAG, "handleMessage: ----str--" + strMsg);
                    if (strMsg.contains("key_5")) {
                        mainActivity.key = mainActivity.key + 1;
                        if (mainActivity.key == 1) {
                            mainActivity.startActivity(new Intent(mainActivity, FaceInputActivity.class));
                            mainActivity.finish();
                        }
                    }
                    if (strMsg.contains("full_1")) {
                        mainActivity.postgetisFull(Urls.address, "1", "0", "0", "0");
                    }
                    if (strMsg.contains("full_2")) {
                        mainActivity.postgetisFull(Urls.address, "0", "1", "0", "0");
                    }
                    if (strMsg.contains("full_3")) {
                        mainActivity.postgetisFull(Urls.address, "0", "0", "1", "0");
                    }
                    if (strMsg.contains("full_4")) {
                        mainActivity.postgetisFull(Urls.address, "0", "0", "0", "1");
                    }
                    break;
            }
        }
    }


//    private Handler etHandler = new Handler() {
//        @Override
//        public void handleMessage(Message msg) {
//            super.handleMessage(msg);
//            switch (msg.what) {
//                case EDIT_OK:
//                    removeMessages(EDIT_OK);
//                    Log.d(TAG, "handleMessage() returned:输入完成 ");
//                    break;
//                case TIME_DATE:
//                    removeMessages(TIME_DATE);
//                    long time = System.currentTimeMillis();
//                    Date date = new Date(time);
//                    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//                    String dateTime = format.format(date);
//                    tvTime.setText(dateTime);
//                    break;
//                case QR_RETURN_LOGIN:
//                    removeMessages(QR_RETURN_LOGIN);
//                    postqrcodelogin(Urls.address);
//                    break;
//            }
//        }
//    };

    private Runnable mRunnable = new Runnable() {
        @Override
        public void run() {
            etHandler.sendEmptyMessage(EDIT_OK);
            String idcard = etIdcard.getText().toString().trim();
            postgetlogin(Urls.address, idcard);
            Log.i(TAG, "sendEmptyMessage: -----idcard---" + idcard);
        }
    };

    /**
     * EditText获取焦点并显示软键盘
     */
    public static void showSoftInputFromWindow(Activity activity, EditText editText) {
        editText.setFocusable(true);
        editText.setFocusableInTouchMode(true);
        editText.requestFocus();

        // TODO 待测试
//        // 关闭软键盘
//        InputMethodManager manager = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
//        manager.hideSoftInputFromWindow(
//                editText == null ? null : editText.getWindowToken(),
//                InputMethodManager.HIDE_NOT_ALWAYS);
    }

    View.OnClickListener imgListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.tv_date_time:
                    //点击系统时间进入到管理员界面
                    Intent intent = new Intent(MainActivity.this, AdminActivity.class);
                    startActivity(intent);
                    finish();
                    break;
            }
        }
    };

    /**
     * 打开定位
     */
    public void startLocaion() {
        mLocationClient = new AMapLocationClient(getApplicationContext());
        mLocationClient.setLocationListener(mLocationListener);
        mLocationOption = new AMapLocationClientOption();
        //设置定位模式为AMapLocationMode.Hight_Accuracy，高精度模式。
        mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
        //设置是否返回地址信息（默认返回地址信息）
        mLocationOption.setNeedAddress(true);
        //获取一次定位结果：
        //该方法默认为false。
        mLocationOption.setOnceLocation(true);
        //设置是否允许模拟位置,默认为false，不允许模拟位置
        mLocationOption.setMockEnable(false);
        //给定位客户端对象设置定位参数
        mLocationClient.setLocationOption(mLocationOption);
        //启动定位
        mLocationClient.startLocation();
    }

    /**
     * 高德地图获取定位信息
     */
    private AMapLocationListener mLocationListener = new AMapLocationListener() {
        @Override
        public void onLocationChanged(AMapLocation aMapLocation) {
            if (isFinishing()) {
                return;
            }
            if (aMapLocation != null) {
                if (aMapLocation.getErrorCode() == 0) {
                    //定位成功回调信息，设置相关消息
                    Log.i(TAG, "当前定位结果来源-----" + aMapLocation.getLocationType());//获取当前定位结果来源，如网络定位结果，详见定位类型表
                    Log.i(TAG, "纬度 ----------------" + aMapLocation.getLatitude());//获取纬度
                    Log.i(TAG, "经度-----------------" + aMapLocation.getLongitude());//获取经度
                    Log.i(TAG, "精度信息-------------" + aMapLocation.getAccuracy());//获取精度信息
                    Log.i(TAG, "地址-----------------" + aMapLocation.getAddress());//地址，如果option中设置isNeedAddress为false，则没有此结果，网络定位结果中会有地址信息，GPS定位不返回地址信息。
                    Log.i(TAG, "国家信息-------------" + aMapLocation.getCountry());//国家信息
                    Log.i(TAG, "省信息---------------" + aMapLocation.getProvince());//省信息
                    Log.i(TAG, "城市信息-------------" + aMapLocation.getCity());//城市信息
                    Log.i(TAG, "城区信息-------------" + aMapLocation.getDistrict());//城区信息
                    Log.i(TAG, "街道信息-------------" + aMapLocation.getStreet());//街道信息
                    Log.i(TAG, "街道门牌号信息-------" + aMapLocation.getStreetNum());//街道门牌号信息
                    Log.i(TAG, "城市编码-------------" + aMapLocation.getCityCode());//城市编码
                    Log.i(TAG, "地区编码-------------" + aMapLocation.getAdCode());//地区编码
                    Log.i(TAG, "获取海拔高度---------" + aMapLocation.getAltitude());
                    city = aMapLocation.getCity();
                    tvCity.setText(city);
                    tvAddress.setText(aMapLocation.getAddress());
                    HeConfig.init("HE1908261119461144", "458fb44d20bb41c59ac0d35b72eea65f");
                    HeConfig.switchToFreeServerNode();
                    HeWeather.getWeatherNow(MainActivity.this, city, nowBeanListener);
                    SharedPreferences shared = getSharedPreferences("CITY", MODE_PRIVATE);
                    SharedPreferences.Editor editor = shared.edit();
                    editor.putString("city", city);
                    editor.commit();
                } else {
                    //显示错误信息ErrCode是错误码，errInfo是错误信息，详见错误码表。
                    Log.i("AmapError", "location Error, ErrCode:"
                            + aMapLocation.getErrorCode() + ", errInfo:"
                            + aMapLocation.getErrorInfo());
                }
            }
        }
    };

    /**
     * 视频循环播放
     */
    MediaPlayer.OnPreparedListener videoListener = new MediaPlayer.OnPreparedListener() {
        @Override
        public void onPrepared(MediaPlayer mediaPlayer) {
            mediaPlayer.start();
            mediaPlayer.setLooping(true);
        }
    };


    public void preFinish() {
        if (mLocationClient != null) {
            mLocationClient.onDestroy();
        }
        if (timeThread != null) {
            timeThread.close();
        }
        closeDevice();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy: 执行onDestroy");
        RunningEnvironment.refWatcher.watch(this);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 200://刚才的识别码
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {//用户同意权限,执行我们的操作
                    startLocaion();
                } else {//用户拒绝之后,当然我们也可以弹出一个窗口,直接跳转到系统设置页面
                    Toast.makeText(MainActivity.this, "未开启定位权限,请手动到设置去开启权限", Toast.LENGTH_LONG).show();
                }
                break;
            case ACTION_REQUEST_PERMISSIONS:
                boolean isAllGranted = true;
                for (int grantResult : grantResults) {
                    isAllGranted &= (grantResult == PackageManager.PERMISSION_GRANTED);
                }
                if (isAllGranted) {
                    activeEngine();
                } else {
                    Log.i(TAG, "onRequestPermissionsResult: 权限被拒绝！");
                }
                break;
            case 101:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {//用户同意权限,执行我们的操作
                    // 授权成功
                } else {//用户拒绝之后,当然我们也可以弹出一个窗口,直接跳转到系统设置页面
                    Toast.makeText(MainActivity.this, "未开启文件读取权限,请手动到设置去开启权限", Toast.LENGTH_LONG).show();
                    Intent intent =  new Intent(Settings.ACTION_SETTINGS);
                    startActivity(intent);
                    finish();
                }
                break;
            default:
                break;
        }
    }




    /**
     * 获取天气信息
     */
    private HeWeather.OnResultWeatherNowBeanListener nowBeanListener = new HeWeather.OnResultWeatherNowBeanListener() {
        @Override
        public void onError(Throwable throwable) {
            Log.i(TAG, "onError: ---" + throwable.toString());
        }

        @Override
        public void onSuccess(Now now) {
            if (isFinishing()) {
                return;
            }
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

    /**
     * 时间线程
     */
    private class TimeThread extends Thread {
        private boolean mRunning = false;

        @Override
        public void run() {
            mRunning = true;
            super.run();
            do {
                try {
                    Thread.sleep(1000);
                    etHandler.sendEmptyMessage(TIME_DATE);
                    etHandler.sendEmptyMessage(QR_RETURN_LOGIN);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } while (mRunning);
        }

        public void close() {
            mRunning = false;
        }
    }

    /**
     * 刷卡登录
     *
     * @param url    刷卡接口
     * @param idCard 卡号
     */
    private void postgetlogin(String url, String idCard) {
        if (TextUtils.isEmpty(idCard)) {
            return;
        }
        OkGo.<String>post(url)
                .tag(MainActivity.this)
                .cacheMode(CacheMode.DEFAULT)
                .params("C_type", "Card_test")
                .params("C_data1", idCard)
                .execute(new StringCallback() {
                    @Override
                    public void onSuccess(Response<String> response) {
                        Log.i(TAG, "onSuccess:  body---" + response.body());
                        if (isFinishing()) {
                            return;
                        }
                        try {
                            JSONObject jsonObject = new JSONObject(response.body());
                            String token = jsonObject.getString("Juming_mac");
                            String name = jsonObject.getString("Juming_name");
                            String tid = jsonObject.getString("Juming_card_id");
                            String jifen = jsonObject.getString("Laji_Jifen");
                            String phone = jsonObject.getString("Juming_mob");
                            String strP = "**** *** " + phone.substring(7, 11);
                            SharedPreferences shared = getSharedPreferences("USER_TOKEN", MODE_PRIVATE);
                            SharedPreferences.Editor editor = shared.edit();
                            editor.putString("token", token);
                            editor.putString("name", name);
                            editor.putString("tid", tid);
                            editor.putString("jifen", jifen);
                            editor.putString("phone", strP);
                            editor.commit();
                            Intent intent = new Intent(MainActivity.this, DustbinTypeActivity.class);
                            Log.d(TAG,"MainActivity 跳转到DustbinTypeActivity");
                            startActivity(intent);
                            finish();
                        } catch (JSONException e) {
                            // TODO
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onStart(Request<String, ? extends Request> request) {
                        super.onStart(request);
                        Log.i(TAG, "onStart: -----" + request.getUrlParam("C_type"));
                    }

                    @Override
                    public void onFinish() {
                        super.onFinish();
                        Log.i(TAG, "onFinish: -----");
                    }

                    @Override
                    public void onError(Response<String> response) {
                        super.onError(response);
                        Log.i(TAG, "onError: " + response.body());
                    }
                });
    }

    /**
     * 获取登录二维码
     *
     * @param url 二维码接口地址
     */
    private void postqrcode(String url) {
        OkGo.<String>post(url)
                .tag(TAG)
                .cacheMode(CacheMode.DEFAULT)
                .params("C_type", "Ljx_ewm_make")
                .params("C_data1", macCode)
                .execute(new StringCallback() {
                    @Override
                    public void onSuccess(Response<String> response) {
                        Log.i(TAG, "onSuccess:  body---" + response.body());
                        if (isFinishing()) {
                            return;
                        }
                        try {
                            JSONObject jsonObject = new JSONObject(response.body());
                            String ImgUrl = jsonObject.getString("Ewm_pic_url");
                            int Return_str = jsonObject.getInt("Return_str");
                            if (Return_str == 0) {
                                Bitmap bitmap = ZxingUtils.createQRImage(ImgUrl, 300, 300);
                                imgQRCode.setImageBitmap(bitmap);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onStart(Request<String, ? extends Request> request) {
                        super.onStart(request);
                        Log.i(TAG, "onStart: --获取二维码地址---" + request.getUrl());
                    }

                    @Override
                    public void onFinish() {
                        super.onFinish();
                        Log.i(TAG, "onFinish: -----");
                    }

                    @Override
                    public void onError(Response<String> response) {
                        super.onError(response);
                        Log.i(TAG, "onError: ---" + response.body());
                    }
                });
    }

    /**
     * 扫码返回登录
     *
     * @param url 二维码接口地址
     */
    private void postqrcodelogin(String url) {
        if (TextUtils.isEmpty(macCode)) {
            return;
        }
        OkGo.<String>post(url)
                .tag(TAG)
                .cacheMode(CacheMode.DEFAULT)
                .params("C_type", "Ljx_ewm_Flag")
                .params("C_data1", macCode)
                .execute(new StringCallback() {
                    @Override
                    public void onSuccess(Response<String> response) {
                        Log.i(TAG, "onSuccess:  body---" + response.body());
                        if (isFinishing()) {
                            return;
                        }
                        try {
                            JSONObject jsonObject = new JSONObject(response.body());
                            int Return_str = jsonObject.getInt("Return_str");
                            if (Return_str == 1) {
                                return;
                            } else if (Return_str == 0) {
                                int Ljx_ewm_Flag = jsonObject.getInt("Ljx_ewm_Flag");
                                if (Ljx_ewm_Flag == 1) {
                                    String Ljx_ewm_card_id = jsonObject.getString("Ljx_ewm_card_id");
                                    postgetlogin(Urls.address, Ljx_ewm_card_id);
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onStart(Request<String, ? extends Request> request) {
                        super.onStart(request);
                        Log.i(TAG, "onStart: -----" + request.getUrlParam("C_type") + "--扫码登录返回");
                    }

                    @Override
                    public void onFinish() {
                        super.onFinish();
                        Log.i(TAG, "onFinish: -----");
                    }
                });
    }

    /**
     * 垃圾桶是否装满
     *
     * @param url
     * @param full1
     * @param full2
     * @param full3
     * @param full4
     */
    private void postgetisFull(String url, String full1, String full2, String full3, String full4) {
        OkGo.<String>post(url)
                .tag(TAG)
                .cacheMode(CacheMode.DEFAULT)
                .params("C_type", "Ljx_full_update")
                .params("C_data1", macCode)
                .params("C_data2", full1)
                .params("C_data3", full2)
                .params("C_data4", full3)
                .params("C_data5", full4)
                .execute(new StringCallback() {
                    @Override
                    public void onSuccess(Response<String> response) {
                        Log.i(TAG, "onSuccess: --response--" + response.body());
                        if (isFinishing()) {
                            return;
                        }
                        try {
                            JSONObject jsonObject = new JSONObject(response.body());
                            int Return_str = jsonObject.getInt("Return_str");
                            if (Return_str == 0) {
                                Toast.makeText(MainActivity.this, "已通知后台工作人员！", Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
    }


    private String idCardTest = null;

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        return super.dispatchKeyEvent(event);
    }


}
