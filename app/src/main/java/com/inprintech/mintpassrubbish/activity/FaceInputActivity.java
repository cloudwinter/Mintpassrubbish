package com.inprintech.mintpassrubbish.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Point;
import android.hardware.Camera;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.arcsoft.face.AgeInfo;
import com.arcsoft.face.ErrorInfo;
import com.arcsoft.face.FaceEngine;
import com.arcsoft.face.FaceFeature;
import com.arcsoft.face.GenderInfo;
import com.arcsoft.face.LivenessInfo;
import com.arcsoft.face.VersionInfo;
import com.arcsoft.face.enums.DetectMode;
import com.inprintech.mintpassrubbish.R;
import com.inprintech.mintpassrubbish.activity.DustbinTypeActivity;
import com.inprintech.mintpassrubbish.faceserver.CompareResult;
import com.inprintech.mintpassrubbish.faceserver.FaceServer;
import com.inprintech.mintpassrubbish.model.DrawInfo;
import com.inprintech.mintpassrubbish.model.FacePreviewInfo;
import com.inprintech.mintpassrubbish.utils.BaseResponseBean;
import com.inprintech.mintpassrubbish.utils.ConfigUtil;
import com.inprintech.mintpassrubbish.utils.DrawHelper;
import com.inprintech.mintpassrubbish.utils.Urls;
import com.inprintech.mintpassrubbish.utils.Utils;
import com.inprintech.mintpassrubbish.utils.camera.CameraHelper;
import com.inprintech.mintpassrubbish.utils.camera.CameraListener;
import com.inprintech.mintpassrubbish.utils.face.FaceHelper;
import com.inprintech.mintpassrubbish.utils.face.FaceListener;
import com.inprintech.mintpassrubbish.utils.face.RequestFeatureStatus;
import com.inprintech.mintpassrubbish.widget.FaceRectView;
import com.inprintech.mintpassrubbish.widget.ShowFaceInfoAdapter;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.cache.CacheMode;
import com.lzy.okgo.callback.StringCallback;
import com.lzy.okgo.model.Response;
import com.lzy.okgo.request.base.Request;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class FaceInputActivity extends BaseActivity implements ViewTreeObserver.OnGlobalLayoutListener {
    private static final int COUNT_TIME_WHAT = 101;
    private int COUNT_TIME = 15;

    private static final String TAG = "FaceInputActivity";
    private static final int MAX_DETECT_NUM = 10;
    /**
     * 当FR成功，活体未成功时，FR等待活体的时间
     */
    private static final int WAIT_LIVENESS_INTERVAL = 50;
    private CameraHelper cameraHelper;
    private DrawHelper drawHelper;
    private Camera.Size previewSize;
    /**
     * 优先打开的摄像头
     */
    private Integer cameraID = Camera.CameraInfo.CAMERA_FACING_FRONT;
    private FaceEngine faceEngine;
    private FaceHelper faceHelper;
    private List<CompareResult> compareResultList;
    private ShowFaceInfoAdapter adapter;
    /**
     * 活体检测的开关
     */
    private boolean livenessDetect = true;

    /**
     * 注册人脸状态码，准备注册
     */
    private static final int REGISTER_STATUS_READY = 0;
    /**
     * 注册人脸状态码，注册中
     */
    private static final int REGISTER_STATUS_PROCESSING = 1;
    /**
     * 注册人脸状态码，注册结束（无论成功失败）
     */
    private static final int REGISTER_STATUS_DONE = 2;

    private int registerStatus = REGISTER_STATUS_DONE;
    // FIXME 测试注册时使用
//    private int registerStatus = REGISTER_STATUS_READY;

    private int afCode = -1;
    private ConcurrentHashMap<Integer, Integer> requestFeatureStatusMap = new ConcurrentHashMap<>();
    private ConcurrentHashMap<Integer, Integer> livenessMap = new ConcurrentHashMap<>();
    private CompositeDisposable getFeatureDelayedDisposables = new CompositeDisposable();
    /**
     * 相机预览显示的控件，可为SurfaceView或TextureView
     */
    private View previewView;
    /**
     * 绘制人脸框的控件
     */
    private FaceRectView faceRectView;

    private static final int ACTION_REQUEST_PERMISSIONS = 0x001;
    private static final float SIMILAR_THRESHOLD = 0.5F;
    /**
     * 所需的所有权限信息
     */
    private static final String[] NEEDED_PERMISSIONS = new String[]{
            Manifest.permission.CAMERA,
            Manifest.permission.READ_PHONE_STATE
    };

    private EditText etBind;
    private String idcard = null;
// FIXME 测试注册时使用
//    private String idcard = "0010738501";
    private int loginNum = 0;

    private String macCode = null;

    /**
     * 30s倒计时
     */
    private TextView tvCountTime;

    @SuppressLint("SourceLockedOrientationActivity")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_face_input);
        //保持亮屏
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            WindowManager.LayoutParams attributes = getWindow().getAttributes();
            attributes.systemUiVisibility = View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
            getWindow().setAttributes(attributes);
        }

        // Activity启动后就锁定为启动时的方向
        switch (getResources().getConfiguration().orientation) {
            case Configuration.ORIENTATION_PORTRAIT:
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                break;
            case Configuration.ORIENTATION_LANDSCAPE:
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                break;
            default:
                break;
        }
        //本地人脸库初始化
        FaceServer.getInstance().init(this);

        tvCountTime = findViewById(R.id.tv_count_time);

        previewView = findViewById(R.id.texture_preview);
        //在布局结束后才做初始化操作
        previewView.getViewTreeObserver().addOnGlobalLayoutListener(this);

        faceRectView = findViewById(R.id.face_rect_view);
        etBind = findViewById(R.id.et_bind_register);
        etBind.setFilters(new InputFilter[]{new InputFilter.LengthFilter(10)});
        etBind.addTextChangedListener(textWatcher);
        hideSoftInputFromWindow();
        RecyclerView recyclerShowFaceInfo = findViewById(R.id.recycler_view_person);
        compareResultList = new ArrayList<>();
        adapter = new ShowFaceInfoAdapter(compareResultList, this);
        recyclerShowFaceInfo.setAdapter(adapter);
        DisplayMetrics dm = getResources().getDisplayMetrics();
        int spanCount = (int) (dm.widthPixels / (getResources().getDisplayMetrics().density * 100 + 0.5f));
        Log.i(TAG, "onCreate:  spanCount--" + spanCount);
        recyclerShowFaceInfo.setLayoutManager(new GridLayoutManager(this, spanCount));
        recyclerShowFaceInfo.setItemAnimator(new DefaultItemAnimator());

        SharedPreferences sp_mac = getSharedPreferences("MAC_CODE", MODE_PRIVATE);
        macCode = sp_mac.getString("macCode", "");

        etHandler.sendEmptyMessage(COUNT_TIME_WHAT);
    }

    private void hideSoftInputFromWindow() {
        // TODO 待测试
        // 关闭软键盘
//        InputMethodManager manager = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
//        manager.hideSoftInputFromWindow(
//                etBind == null ? null : etBind.getWindowToken(),
//                InputMethodManager.HIDE_NOT_ALWAYS);
    }

    TextWatcher textWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            Log.d(TAG, "onTextChanged() " + charSequence);
        }

        @Override
        public void afterTextChanged(Editable editable) {
            Log.i(TAG, "afterTextChanged: " + editable.toString());
            etHandler.removeCallbacks(mRunnable);
            //600毫秒没有输入认为输入完毕
            etHandler.postDelayed(mRunnable, 500);
        }
    };

    private Handler etHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 1:
                    removeMessages(1);
                    Log.d(TAG, "handleMessage() returned:输入完成 ");
                    break;
                case COUNT_TIME_WHAT:
                    Log.d(TAG, "handleMessage() COUNT_TIME :"+COUNT_TIME);
                    COUNT_TIME--;
                    tvCountTime.setText(COUNT_TIME+"秒后未登录返回首页");
                    if (COUNT_TIME == 0) {
                        Intent intent = new Intent(FaceInputActivity.this, MainActivity.class);
                        startActivity(intent);
                        finish();
                        Log.d(TAG,"FaceInputActivity onFinish  跳转到MainActivity");
                    } else {
                        etHandler.sendEmptyMessageDelayed(COUNT_TIME_WHAT,1000);
                    }
                    break;
            }
        }
    };
    private Runnable mRunnable = new Runnable() {
        @Override
        public void run() {
            etHandler.sendEmptyMessage(1);
            String idCardTemp = etBind.getText().toString().trim();
            Log.i(TAG, "sendEmptyMessage: -----idCardTemp---" + idCardTemp);
            if (TextUtils.isEmpty(idCardTemp)) {
                // 未刷卡注册失败
                Toast.makeText(FaceInputActivity.this, "注册失败", Toast.LENGTH_SHORT).show();
                return;
            }
            idcard = idCardTemp;
            register();
            Toast.makeText(FaceInputActivity.this, "读卡完成，注册中......", Toast.LENGTH_SHORT).show();
//            postgetlogin(Urls.address, idCardTemp);

        }
    };

    /**
     * 初始化引擎
     */
    private void initEngine() {
        faceEngine = new FaceEngine();
        afCode = faceEngine.init(this, DetectMode.ASF_DETECT_MODE_VIDEO, ConfigUtil.getFtOrient(this),
                16, MAX_DETECT_NUM, FaceEngine.ASF_FACE_RECOGNITION | FaceEngine.ASF_FACE_DETECT | FaceEngine.ASF_LIVENESS);
        VersionInfo versionInfo = new VersionInfo();
        faceEngine.getVersion(versionInfo);
        Log.i(TAG, "initEngine:  init: " + afCode + "  version:" + versionInfo);

        if (afCode != ErrorInfo.MOK) {
            Log.i(TAG, "initEngine: 引擎初始化失败，错误码为 --" + afCode);
        }
    }

    /**
     * 销毁引擎
     */
    private void unInitEngine() {

        if (afCode == ErrorInfo.MOK) {
            afCode = faceEngine.unInit();
            Log.i(TAG, "unInitEngine: " + afCode);
        }
    }


    @Override
    void onPreFinish() {
        OkGo.getInstance().cancelTag(TAG);
        etHandler.removeCallbacksAndMessages(null);
        cancelFace();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
//        etHandler.removeCallbacksAndMessages(null);
//        cancelFace();
    }

    private void cancelFace() {
        if (cameraHelper != null) {
            cameraHelper.release();
            cameraHelper = null;
        }

        //faceHelper中可能会有FR耗时操作仍在执行，加锁防止crash
        if (faceHelper != null) {
            synchronized (faceHelper) {
                unInitEngine();
            }
            ConfigUtil.setTrackId(this, faceHelper.getCurrentTrackId());
            faceHelper.release();
        } else {
            unInitEngine();
        }
        if (getFeatureDelayedDisposables != null) {
            getFeatureDelayedDisposables.dispose();
            getFeatureDelayedDisposables.clear();
        }
        FaceServer.getInstance().unInit();
    }

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

    private void initCamera() {
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);

        final FaceListener faceListener = new FaceListener() {
            @Override
            public void onFail(Exception e) {
                Log.e(TAG, "onFail: " + e.getMessage());
            }

            //请求FR的回调
            @Override
            public void onFaceFeatureInfoGet(final FaceFeature faceFeature, final Integer requestId) {
                //FR成功
                if (faceFeature != null) {
//                    Log.i(TAG, "onPreview: fr end = " + System.currentTimeMillis() + " trackId = " + requestId);

                    //不做活体检测的情况，直接搜索
                    if (!livenessDetect) {
                        searchFace(faceFeature, requestId);
                        Log.i(TAG, "onFaceFeatureInfoGet: requestId--" + requestId);
                    }
                    //活体检测通过，搜索特征
                    else if (livenessMap.get(requestId) != null && livenessMap.get(requestId) == LivenessInfo.ALIVE) {
                        searchFace(faceFeature, requestId);
                        Log.i(TAG, "onFaceFeatureInfoGet: requestId--" + requestId);
                    }
                    //活体检测未出结果，延迟100ms再执行该函数
                    else if (livenessMap.get(requestId) != null && livenessMap.get(requestId) == LivenessInfo.UNKNOWN) {
                        getFeatureDelayedDisposables.add(Observable.timer(WAIT_LIVENESS_INTERVAL, TimeUnit.MILLISECONDS)
                                .subscribe(new Consumer<Long>() {
                                    @Override
                                    public void accept(Long aLong) {
                                        Log.i(TAG, "accept: ----未注册");
                                        onFaceFeatureInfoGet(faceFeature, requestId);
                                    }
                                }));
                    }
                    //活体检测失败
                    else {
                        requestFeatureStatusMap.put(requestId, RequestFeatureStatus.NOT_ALIVE);
                    }

                }
                //FR 失败
                else {
                    requestFeatureStatusMap.put(requestId, RequestFeatureStatus.FAILED);
                }
            }

        };


        CameraListener cameraListener = new CameraListener() {
            @Override
            public void onCameraOpened(Camera camera, int cameraId, int displayOrientation, boolean isMirror) {
                previewSize = camera.getParameters().getPreviewSize();
                drawHelper = new DrawHelper(previewSize.width, previewSize.height, previewView.getWidth(), previewView.getHeight(), displayOrientation
                        , cameraId, isMirror);

                faceHelper = new FaceHelper.Builder()
                        .faceEngine(faceEngine)
                        .frThreadNum(MAX_DETECT_NUM)
                        .previewSize(previewSize)
                        .faceListener(faceListener)
                        .currentTrackId(ConfigUtil.getTrackId(FaceInputActivity.this.getApplicationContext()))
                        .build();
            }


            @Override
            public void onPreview(final byte[] nv21, Camera camera) {
                if (faceRectView != null) {
                    faceRectView.clearFaceInfo();
                }
                List<FacePreviewInfo> facePreviewInfoList = faceHelper.onPreviewFrame(nv21);
                Log.i(TAG, "onPreview: --facePreviewInfoList--" + facePreviewInfoList.size());
                if (facePreviewInfoList != null && faceRectView != null && drawHelper != null) {
                    List<DrawInfo> drawInfoList = new ArrayList<>();
                    for (int i = 0; i < facePreviewInfoList.size(); i++) {
                        String name = faceHelper.getName(facePreviewInfoList.get(i).getTrackId());
                        Log.i(TAG, "onPreview: --name--" + name);
                        if (name != null) {
                            //loginNum = loginNum + 1;
//                            if (loginNum == 1) {
                                Log.i(TAG, "onPreview: -----loginNum--" + loginNum);
                                postgetlogin(Urls.address, name);
                                cancelFace();
//                            }
                        }
                        drawInfoList.add(new DrawInfo(facePreviewInfoList.get(i).getFaceInfo().getRect(), GenderInfo.UNKNOWN, AgeInfo.UNKNOWN_AGE, LivenessInfo.UNKNOWN,
                                name == null ? String.valueOf(facePreviewInfoList.get(i).getTrackId()) : name));
                    }
                    drawHelper.draw(faceRectView, drawInfoList);
                }
                if (registerStatus == REGISTER_STATUS_READY && facePreviewInfoList != null && facePreviewInfoList.size() > 0) {
                    registerStatus = REGISTER_STATUS_PROCESSING;
                    Observable.create(new ObservableOnSubscribe<Boolean>() {
                        @Override
                        public void subscribe(ObservableEmitter<Boolean> emitter) {
                            Log.i(TAG, "onPreview: -----Observable.subscribe--idcard : " + idcard);
                            if (!TextUtils.isEmpty(idcard)) {
                                // 只有获取到卡信息才去注册
                                boolean success = FaceServer.getInstance()
                                        .register(FaceInputActivity.this, nv21.clone(),
                                                previewSize.width, previewSize.height, idcard);
                                Log.i(TAG, "onPreview: -----Observable.subscribe--register result : " + success);
                                emitter.onNext(success);
                            }
                        }
                    })
                            .subscribeOn(Schedulers.computation())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(new Observer<Boolean>() {
                                @Override
                                public void onSubscribe(Disposable d) {

                                }

                                @Override
                                public void onNext(Boolean success) {
                                    String result = success ? "register success!" : "register failed!";
                                    Toast.makeText(FaceInputActivity.this, result, Toast.LENGTH_SHORT).show();
                                    if (success) {
                                        // 注册成功之后请求卡登陆
                                        // TODO
                                        //Toast.makeText(FaceInputActivity.this, "注册成功", Toast.LENGTH_SHORT).show();
                                        postgetlogin(Urls.address, idcard);
                                        registerStatus = REGISTER_STATUS_DONE;
                                    } else {
                                        // 如果识别失败重新识别
                                        registerStatus = REGISTER_STATUS_READY;
                                    }

                                }

                                @Override
                                public void onError(Throwable e) {
                                    Toast.makeText(FaceInputActivity.this, "register failed!", Toast.LENGTH_SHORT).show();
                                    //registerStatus = REGISTER_STATUS_DONE;
                                    registerStatus = REGISTER_STATUS_READY;
                                }

                                @Override
                                public void onComplete() {

                                }
                            });
                }
                clearLeftFace(facePreviewInfoList);

                if (facePreviewInfoList != null && facePreviewInfoList.size() > 0 && previewSize != null) {

                    for (int i = 0; i < facePreviewInfoList.size(); i++) {
                        if (livenessDetect) {
                            livenessMap.put(facePreviewInfoList.get(i).getTrackId(), facePreviewInfoList.get(i).getLivenessInfo().getLiveness());
                        }
                        /**
                         * 对于每个人脸，若状态为空或者为失败，则请求FR（可根据需要添加其他判断以限制FR次数），
                         * FR回传的人脸特征结果在{@link FaceListener#onFaceFeatureInfoGet(FaceFeature, Integer)}中回传
                         */
                        if (requestFeatureStatusMap.get(facePreviewInfoList.get(i).getTrackId()) == null
                                || requestFeatureStatusMap.get(facePreviewInfoList.get(i).getTrackId()) == RequestFeatureStatus.FAILED) {
                            requestFeatureStatusMap.put(facePreviewInfoList.get(i).getTrackId(), RequestFeatureStatus.SEARCHING);
                            faceHelper.requestFaceFeature(nv21, facePreviewInfoList.get(i).getFaceInfo(), previewSize.width, previewSize.height, FaceEngine.CP_PAF_NV21, facePreviewInfoList.get(i).getTrackId());
                            Log.i(TAG, "onPreview: fr start = " + System.currentTimeMillis() + " trackId = " + facePreviewInfoList.get(i).getTrackId());
                        }
                    }
                }
            }

            @Override
            public void onCameraClosed() {
                Log.i(TAG, "onCameraClosed: ");
            }

            @Override
            public void onCameraError(Exception e) {
                Log.i(TAG, "onCameraError: " + e.getMessage());
            }

            @Override
            public void onCameraConfigurationChanged(int cameraID, int displayOrientation) {
                if (drawHelper != null) {
                    drawHelper.setCameraDisplayOrientation(displayOrientation);
                }
                Log.i(TAG, "onCameraConfigurationChanged: " + cameraID + "  " + displayOrientation);
            }
        };

        cameraHelper = new CameraHelper.Builder()
                .previewViewSize(new Point(previewView.getMeasuredWidth(), previewView.getMeasuredHeight()))
                .rotation(getWindowManager().getDefaultDisplay().getRotation())
                .specificCameraId(cameraID != null ? cameraID : Camera.CameraInfo.CAMERA_FACING_FRONT)
                .isMirror(false)
                .previewOn(previewView)
                .cameraListener(cameraListener)
                .build();
        cameraHelper.init();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == ACTION_REQUEST_PERMISSIONS) {
            boolean isAllGranted = true;
            for (int grantResult : grantResults) {
                isAllGranted &= (grantResult == PackageManager.PERMISSION_GRANTED);
            }
            if (isAllGranted) {
                initEngine();
                initCamera();
                if (cameraHelper != null) {
                    cameraHelper.start();
                }
            } else {
                Log.i(TAG, "onRequestPermissionsResult:   权限被拒绝！");
            }
        }
    }

    /**
     * 删除已经离开的人脸
     *
     * @param facePreviewInfoList 人脸和trackId列表
     */
    private void clearLeftFace(List<FacePreviewInfo> facePreviewInfoList) {
        Set<Integer> keySet = requestFeatureStatusMap.keySet();
        if (compareResultList != null) {
            for (int i = compareResultList.size() - 1; i >= 0; i--) {
                if (!keySet.contains(compareResultList.get(i).getTrackId())) {
                    compareResultList.remove(i);
                    adapter.notifyItemRemoved(i);
                }
            }
        }
        if (facePreviewInfoList == null || facePreviewInfoList.size() == 0) {
            requestFeatureStatusMap.clear();
            livenessMap.clear();
            return;
        }

        for (Integer integer : keySet) {
            boolean contained = false;
            for (FacePreviewInfo facePreviewInfo : facePreviewInfoList) {
                if (facePreviewInfo.getTrackId() == integer) {
                    contained = true;
                    break;
                }
            }
            if (!contained) {
                requestFeatureStatusMap.remove(integer);
                livenessMap.remove(integer);
            }
        }

    }

    private void searchFace(final FaceFeature frFace, final Integer requestId) {
        Observable
                .create(new ObservableOnSubscribe<CompareResult>() {
                    @Override
                    public void subscribe(ObservableEmitter<CompareResult> emitter) {
                        Log.i(TAG, "subscribe: fr search start = " + System.currentTimeMillis() + " trackId = " + requestId);
                        CompareResult compareResult = FaceServer.getInstance().getTopOfFaceLib(frFace);
                        Log.i(TAG, "subscribe: fr search end = " + System.currentTimeMillis() + " trackId = " + requestId);
                        if (compareResult == null) {
                            emitter.onError(null);
                        } else {
                            emitter.onNext(compareResult);
                        }
                    }
                })
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<CompareResult>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(CompareResult compareResult) {
                        if (compareResult == null || compareResult.getUserName() == null) {
                            requestFeatureStatusMap.put(requestId, RequestFeatureStatus.FAILED);
                            Log.i(TAG, "onNext: ---" + requestId);
                            faceHelper.addName(requestId, "VISITOR " + idcard);
                            return;
                        }

                        Log.i(TAG, "onNext: fr search get result  = " + System.currentTimeMillis() + " trackId = " + requestId + "  similar = " + compareResult.getSimilar());
                        if (compareResult.getSimilar() > SIMILAR_THRESHOLD) {
                            boolean isAdded = false;
                            if (compareResultList == null) {
                                requestFeatureStatusMap.put(requestId, RequestFeatureStatus.FAILED);
                                faceHelper.addName(requestId, idcard);
                                return;
                            }
                            for (CompareResult compareResult1 : compareResultList) {
                                if (compareResult1.getTrackId() == requestId) {
                                    isAdded = true;
                                    break;
                                }
                            }
                            if (!isAdded) {
                                //对于多人脸搜索，假如最大显示数量为 MAX_DETECT_NUM 且有新的人脸进入，则以队列的形式移除
                                if (compareResultList.size() >= MAX_DETECT_NUM) {
                                    compareResultList.remove(0);
                                    adapter.notifyItemRemoved(0);
                                }
                                //添加显示人员时，保存其trackId
                                compareResult.setTrackId(requestId);
                                compareResultList.add(compareResult);
                                adapter.notifyItemInserted(compareResultList.size() - 1);
                            }
                            requestFeatureStatusMap.put(requestId, RequestFeatureStatus.SUCCEED);
                            Log.i(TAG, "onNext:   getUserName--" + compareResult.getUserName()+ " requestId:"+requestId);
                            faceHelper.addName(requestId, compareResult.getUserName());
                            // FIXME xiayundong modify 2020-04-07 pm 06:22
//                            faceHelper.addName(requestId, idcard);

                        } else {
                            requestFeatureStatusMap.put(requestId, RequestFeatureStatus.FAILED);
                            faceHelper.addName(requestId, idcard);
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        requestFeatureStatusMap.put(requestId, RequestFeatureStatus.FAILED);
                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }


    /**
     * 将准备注册的状态置为{@link #REGISTER_STATUS_READY}
     */
    public void register() {
        // 置为初始化状态
        registerStatus = REGISTER_STATUS_READY;
//        if (registerStatus == REGISTER_STATUS_DONE) {
//            registerStatus = REGISTER_STATUS_READY;
//        }
    }

    /**
     * 在{@link #previewView}第一次布局完成后，去除该监听，并且进行引擎和相机的初始化
     */
    @Override
    public void onGlobalLayout() {
        previewView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
        if (!checkPermissions(NEEDED_PERMISSIONS)) {
            ActivityCompat.requestPermissions(FaceInputActivity.this, NEEDED_PERMISSIONS, ACTION_REQUEST_PERMISSIONS);
        } else {
            initEngine();
            initCamera();
        }
    }

    /**
     * 刷卡登录
     *
     * @param url    刷卡接口
     * @param idCard 卡号
     */
    private void postgetlogin(String url, String idCard) {
        Log.i(TAG, "postgetlogin: -----idCard" + idCard);
        if (idCard == null) {
            return;
        }
        OkGo.<String>post(url)
                .tag(TAG)
                .cacheMode(CacheMode.DEFAULT)
                .params("C_type", "Card_test")
                .params("C_data1", idCard)
                .execute(new StringCallback() {
                    @Override
                    public void onSuccess(Response<String> response) {
                        Log.i(TAG, "onSuccess:  body---" + response.body());
                        if (isFinishing()) {
                            Log.i(TAG, "onSuccess:  当前activity已经finish了");
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
                            Intent intent = new Intent(FaceInputActivity.this, DustbinTypeActivity.class);
                            Log.d(TAG,"FaceInputActivity 跳转到DustbinTypeActivity");
                            startActivity(intent);
                            finish();
                            Toast.makeText(FaceInputActivity.this, "登录成功", Toast.LENGTH_SHORT).show();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onStart(Request<String, ? extends Request> request) {
                        super.onStart(request);
                        Log.i(TAG, "onStart: -----" + request.getUrl());
                    }

                    @Override
                    public void onFinish() {
                        super.onFinish();
                        Log.i(TAG, "onFinish: -----");
                        loginNum = 0;
                    }
                });
    }

    /**
     * 人脸数据注册
     *
     * @param url
     * @param faceSignature
     * @param idcard
     */
    private void postFaceRegister(String url, String macCode, String faceSignature, String idcard) {
        OkGo.<String>post(url)
                .tag(FaceInputActivity.this)
                .cacheMode(CacheMode.DEFAULT)
                .params("C_type", "Face_update")
                .params("C_data1", macCode)
                .params("C_data2", idcard)
                .params("C_data3", faceSignature)
                .execute(new StringCallback() {
                    @Override
                    public void onSuccess(Response<String> response) {
                        Log.i(TAG, "onSuccess:  body---" + response.body());
                        try {
                            JSONObject jsonObject = new JSONObject(response.body());
                            int Return_str = jsonObject.getInt("Return_str");
                            if (Return_str == 0) {
                                Toast.makeText(FaceInputActivity.this, "注册成功！", Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onStart(Request<String, ? extends Request> request) {
                        super.onStart(request);
                        Log.i(TAG, "onStart: -----" + request.getUrl());
                    }

                    @Override
                    public void onFinish() {
                        super.onFinish();
                        Log.i(TAG, "onFinish: -----");
                        loginNum = 0;
                    }
                });
    }

    /**
     * 人脸识别登录
     *
     * @param url
     * @param faceSignature
     * @param macCode
     */
    private void postFaceLogin(String url, String faceSignature, String macCode) {
        OkGo.<String>post(url)
                .tag(FaceInputActivity.this)
                .cacheMode(CacheMode.DEFAULT)
                .params("C_type", "Face_Test")
                .params("C_data1", macCode)
                .params("C_data2", faceSignature)
                .execute(new StringCallback() {
                    @Override
                    public void onSuccess(Response<String> response) {
                        Log.i(TAG, "onSuccess:  body---" + response.body());
                        try {
                            JSONObject jsonObject = new JSONObject(response.body());
                            int Return_str = jsonObject.getInt("Return_str");
                            if (Return_str == 0) {
                                String cardId = jsonObject.getString("Ljx_face_card_id");
                                postgetlogin(Urls.address,cardId);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onStart(Request<String, ? extends Request> request) {
                        super.onStart(request);
                        Log.i(TAG, "onStart: -----" + request.getUrl());
                    }

                    @Override
                    public void onFinish() {
                        super.onFinish();
                        Log.i(TAG, "onFinish: -----");
                        loginNum = 0;
                    }
                });
    }


}
