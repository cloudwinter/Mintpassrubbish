package com.inprintech.mintpassrubbish.activity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.inprintech.mintpassrubbish.MyApplication;
import com.inprintech.mintpassrubbish.R;
import com.inprintech.mintpassrubbish.adapter.VideoListAdapter;
import com.inprintech.mintpassrubbish.model.EntityVideo;
import com.inprintech.mintpassrubbish.utils.BaseResponseBean;
import com.inprintech.mintpassrubbish.utils.Urls;
import com.inprintech.mintpassrubbish.utils.Utils;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.cache.CacheMode;
import com.lzy.okgo.callback.StringCallback;
import com.lzy.okgo.model.Response;
import com.lzy.okgo.request.base.Request;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import cn.wch.ch34xuartdriver.CH34xUARTDriver;

public class AdminActivity extends BaseActivity implements View.OnClickListener, AdapterView.OnItemClickListener {
    private static final String TAG = "AdminActivity";

    private EditText etMultiple;
    private Button btn1_ks, btn2_ks, btn3_ks, btn4_ks;
    private Button btn1_qr, btn2_qr, btn3_qr, btn4_qr;
    private Button btn_back;
    private TextView tv1, tv2, tv3, tv4;
    private ListView listVideo;
    private EditText etInput1, etInput2;
    private EditText etInputMAC;

    private String intput, name, pwd;
    private int input_num = 0;
    private long firstTime = 0;

    private VideoListAdapter adapter;

    private Handler handler;
    private boolean isOpen;
    private readThread readThread = null;

    private int l1 = 0, l2 = 0, l3 = 0, l4 = 0;
    private int i1 = 0, i2 = 0, i3 = 0, i4 = 0;
    private int j1 = 0, j2 = 0, j3 = 0, j4 = 0;
    private int k1 = 0, k2 = 0, k3 = 0, k4 = 0;

    private String macCode = null;
    private int Return_str;
    private int Admin_test_Flag;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);
        MyApplication.driver = new CH34xUARTDriver((UsbManager) getSystemService(Context.USB_SERVICE), this, Utils.ACTION_USB_PERMISSION);
        initView();

        SharedPreferences sp_mac = getSharedPreferences("MAC_CODE", MODE_PRIVATE);
        macCode = sp_mac.getString("macCode", "");
        if (macCode.length() == 0 && macCode.equals("")) {
            AlertDialog.Builder dialogMac = new AlertDialog.Builder(AdminActivity.this);
            View viewMac = View.inflate(this, R.layout.dialog_mac_code, null);
            etInputMAC = viewMac.findViewById(R.id.et_input_mac);
            dialogMac.setView(viewMac);
            dialogMac.setTitle("请输入机器识别码并保存");
            dialogMac.setCancelable(false);
            dialogMac.setPositiveButton("保存", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();

                    String strMAC = etInputMAC.getText().toString().trim();
                    if (strMAC.equals("") && strMAC.length() == 0) {
                        Toast.makeText(AdminActivity.this, "MAC识别码不能为空！", Toast.LENGTH_SHORT).show();
                        return;
                    } else {
                        SharedPreferences shared = getSharedPreferences("MAC_CODE", MODE_PRIVATE);
                        SharedPreferences.Editor editor = shared.edit();
                        editor.putString("macCode", strMAC);
                        editor.commit();
                    }

                    AlertDialog.Builder dialogLogin = new AlertDialog.Builder(AdminActivity.this);
                    View view = View.inflate(AdminActivity.this, R.layout.admin_login, null);
                    etInput1 = view.findViewById(R.id.et_account);
                    etInput2 = view.findViewById(R.id.et_pwd);
                    dialogLogin.setView(view);
                    dialogLogin.setTitle("管理员登录");
                    dialogLogin.setCancelable(false);
                    dialogLogin.setPositiveButton("登 录", null);
                    dialogLogin.setNegativeButton("取 消", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            Intent intent = new Intent(AdminActivity.this, MainActivity.class);
                            startActivity(intent);
                            finish();
                        }
                    });
                    final AlertDialog alertDialog = dialogLogin.create();
                    alertDialog.show();
                    if (alertDialog.getButton(AlertDialog.BUTTON_POSITIVE) != null) {
                        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                name = etInput1.getText().toString().trim();
                                pwd = etInput2.getText().toString().trim();
                                if (!name.equals("") && !pwd.equals("")) {
                                    postgetAdminlogin(Urls.address, name, pwd, macCode);
                                    if (Return_str == 0 && Admin_test_Flag == 1) {
                                        Toast.makeText(AdminActivity.this, "登录成功", Toast.LENGTH_SHORT).show();
                                        alertDialog.dismiss();
                                    } else {
                                        Toast.makeText(AdminActivity.this, "登录失败", Toast.LENGTH_SHORT).show();
                                    }
                                } else {
                                    Toast.makeText(AdminActivity.this, "请输入账号！", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }
                }
            });
            dialogMac.setNegativeButton("取 消", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    Intent intent = new Intent(AdminActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                }
            });
            dialogMac.create();
            dialogMac.show();
        } else {
            AlertDialog.Builder dialogLogin = new AlertDialog.Builder(AdminActivity.this);
            View view = View.inflate(AdminActivity.this, R.layout.admin_login, null);
            etInput1 = view.findViewById(R.id.et_account);
            etInput2 = view.findViewById(R.id.et_pwd);
            dialogLogin.setView(view);
            dialogLogin.setTitle("管理员登录");
            dialogLogin.setCancelable(false);
            dialogLogin.setPositiveButton("登 录", null);
            dialogLogin.setNegativeButton("取 消", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    Intent intent = new Intent(AdminActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                }
            });
            final AlertDialog alertDialog = dialogLogin.create();
            alertDialog.show();
            if (alertDialog.getButton(AlertDialog.BUTTON_POSITIVE) != null) {
                alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        name = etInput1.getText().toString().trim();
                        pwd = etInput2.getText().toString().trim();
                        if (!name.equals("") && !pwd.equals("")) {
                            postgetAdminlogin(Urls.address, name, pwd, macCode);
                            if (Return_str == 0 && Admin_test_Flag == 1) {
                                Toast.makeText(AdminActivity.this, "登录成功", Toast.LENGTH_SHORT).show();
                                alertDialog.dismiss();
                            } else {
                                Toast.makeText(AdminActivity.this, "登录失败", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(AdminActivity.this, "请输入账号！", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        }


    }

    private void initView() {
        listVideo = findViewById(R.id.list_video);
        etMultiple = findViewById(R.id.et_multiple);
        btn1_ks = findViewById(R.id.btn1_start);
        btn2_ks = findViewById(R.id.btn2_start);
        btn3_ks = findViewById(R.id.btn3_start);
        btn4_ks = findViewById(R.id.btn4_start);
        btn1_qr = findViewById(R.id.btn1_submit);
        btn2_qr = findViewById(R.id.btn2_submit);
        btn3_qr = findViewById(R.id.btn3_submit);
        btn4_qr = findViewById(R.id.btn4_submit);
        tv1 = findViewById(R.id.tv_1);
        tv2 = findViewById(R.id.tv_2);
        tv3 = findViewById(R.id.tv_3);
        tv4 = findViewById(R.id.tv_4);

        btn_back = findViewById(R.id.btn_back);


        btn_back.setOnClickListener(this);
        btn1_ks.setOnClickListener(this);
        btn2_ks.setOnClickListener(this);
        btn3_ks.setOnClickListener(this);
        btn4_ks.setOnClickListener(this);
        btn1_qr.setOnClickListener(this);
        btn2_qr.setOnClickListener(this);
        btn3_qr.setOnClickListener(this);
        btn4_qr.setOnClickListener(this);
        listVideo.setOnItemClickListener(this);

        adapter = new VideoListAdapter(AdminActivity.this, getVideoFromSDCard(AdminActivity.this));
        listVideo.setAdapter(adapter);

        openSerialPort();
    }

    private void openSerialPort() {
        if (!isOpen) {
            int retval = MyApplication.driver.ResumeUsbPermission();
            Log.i(TAG, "openSerialPort:   retval--" + retval);
            if (retval == 0) {
                retval = MyApplication.driver.ResumeUsbList();
                Log.i(TAG, "openSerialPort:   retval--" + retval);
                if (retval == -1) {
                    MyApplication.driver.CloseDevice();
                } else if (retval == 0) {
                    if (MyApplication.driver.mDeviceConnection != null) {
                        if (!MyApplication.driver.UartInit()) {//对串口设备进行初始化操作
                            Log.i(TAG, "openSerialPort:  Initialization failed!");
                            return;
                        }
                        Log.i(TAG, "openSerialPort:  Device opened!");
                        if (MyApplication.driver.SetConfig(Utils.baudRate, Utils.dataBit, Utils.stopBit, Utils.parity, Utils.flowControl)) {
                            Log.i(TAG, "openSerialPort:  Config successfully");
                            isOpen = true;
                            readThreadStart();//开启读线程读取串口接收的数据
                        } else {
                            Log.i(TAG, "openSerialPort:  Config failed!");
                        }
                    } else {
                        Log.i(TAG, "openSerialPort:  Open failed!");
                    }
                }
            }
        } else {
            isOpen = false;
        }

        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                String strMsg = (String) msg.obj;
                Log.i(TAG, "handleMessage: ----str--" + strMsg);
                SharedPreferences shared = getSharedPreferences("calibration", MODE_PRIVATE);
                SharedPreferences.Editor editor = shared.edit();
                try {
                    if (strMsg.contains("Weight_1")) {
                        JSONObject jsonObject = new JSONObject(strMsg);
                        int one = jsonObject.getInt("Weight_1");
                        l1 = l1 + 1;
                        if (l1 == 1) {
                            i1 = one;
                            Log.i(TAG, "run:--ii1ii--" + i1);
                            tv1.setText("当前基数：" + i1);
                            if (i1 != 0) {
                                btn1_ks.setEnabled(false);
                                btn1_ks.setBackgroundResource(R.drawable.round_face_four_hui);
                            }
                        }
                        if (l1 == 2) {
                            j1 = one;
                            Log.i(TAG, "run: --jj1jj--" + j1);

                        }
                        Log.i(TAG, "run: --l1l--" + l1);
                        if (l1 == 2) {
                            l1 = 0;
                            btn1_ks.setEnabled(true);
                            btn1_ks.setBackgroundResource(R.drawable.round_face_four);
                            k1 = j1 - i1;
                            DecimalFormat df = new DecimalFormat("0.000");
                            Log.i(TAG, "run: --1--" + df.format((float) k1 / input_num));
                            String str_bs = df.format((float) k1 / input_num);
                            float bs = Float.parseFloat(str_bs);
                            tv1.setText("校准倍数：" + bs + " 砝码重量：" + (int) (k1 / bs));
                            editor.putFloat("one", bs);
                        }
                    } else if (strMsg.contains("Weight_2")) {
                        JSONObject jsonObject = new JSONObject(strMsg);
                        int two = jsonObject.getInt("Weight_2");
                        l2 = l2 + 1;
                        if (l2 == 1) {
                            i2 = two;
                            Log.i(TAG, "run:--ii2ii--" + i2);
                            tv2.setText("当前基数：" + i2);
                            if (i2 != 0) {
                                btn2_ks.setEnabled(false);
                                btn2_ks.setBackgroundResource(R.drawable.round_face_four_hui);
                            }
                        }
                        if (l2 == 2) {
                            j2 = two;
                            Log.i(TAG, "run: --jj2jj--" + j2);

                        }
                        Log.i(TAG, "run: --l2l--" + l2);
                        if (l2 == 2) {
                            btn2_ks.setEnabled(true);
                            btn2_ks.setBackgroundResource(R.drawable.round_face_four);
                            l2 = 0;
                            k2 = j2 - i2;
                            DecimalFormat df = new DecimalFormat("0.000");
                            Log.i(TAG, "run: --2--" + df.format((float) k2 / input_num));
                            String str_bs = df.format((float) k2 / input_num);
                            float bs = Float.parseFloat(str_bs);
                            tv2.setText("校准倍数：" + bs + " 砝码重量：" + (int) (k2 / bs));
                            editor.putFloat("two", bs);
                        }
                    } else if (strMsg.contains("Weight_3")) {
                        JSONObject jsonObject = new JSONObject(strMsg);
                        int three = jsonObject.getInt("Weight_3");
                        l3 = l3 + 1;
                        if (l3 == 1) {
                            i3 = three;
                            Log.i(TAG, "run:--ii3ii--" + i3);
                            tv3.setText("当前基数：" + i3);
                            if (i3 != 0) {
                                btn3_ks.setEnabled(false);
                                btn3_ks.setBackgroundResource(R.drawable.round_face_four_hui);
                            }
                        }
                        if (l3 == 2) {
                            j3 = three;
                            Log.i(TAG, "run: --jj3jj--" + j3);

                        }
                        Log.i(TAG, "run: --ll--" + l3);
                        if (l3 == 2) {
                            btn3_ks.setEnabled(true);
                            btn3_ks.setBackgroundResource(R.drawable.round_face_four);
                            l3 = 0;
                            k3 = j3 - i3;
                            DecimalFormat df = new DecimalFormat("0.000");
                            Log.i(TAG, "run: --3--" + df.format((float) k3 / input_num));
                            String str_bs = df.format((float) k3 / input_num);
                            float bs = Float.parseFloat(str_bs);
                            tv3.setText("校准倍数：" + bs + " 砝码重量：" + (int) (k3 / bs));
                            editor.putFloat("three", bs);
                        }
                    } else if (strMsg.contains("Weight_4")) {
                        JSONObject jsonObject = new JSONObject(strMsg);
                        int four = jsonObject.getInt("Weight_4");
                        l4 = l4 + 1;
                        if (l4 == 1) {
                            i4 = four;
                            Log.i(TAG, "run:--ii4ii--" + i4);
                            tv4.setText("当前基数：" + i4);
                            if (i4 != 0) {
                                btn4_ks.setEnabled(false);
                                btn4_ks.setBackgroundResource(R.drawable.round_face_four_hui);
                            }
                        }
                        if (l4 == 2) {
                            j4 = four;
                            Log.i(TAG, "run: --jj4jj--" + j4);

                        }
                        Log.i(TAG, "run: --l4l--" + l4);
                        if (l4 == 2) {
                            btn4_ks.setEnabled(true);
                            btn4_ks.setBackgroundResource(R.drawable.round_face_four);
                            l4 = 0;
                            k4 = j4 - i4;
                            Log.i(TAG, "run: ---k---" + k4);
                            DecimalFormat df = new DecimalFormat("0.000");
                            Log.i(TAG, "run: --4--" + df.format((float) k4 / input_num));
                            String str_bs = df.format((float) k4 / input_num);
                            float bs = Float.parseFloat(str_bs);
                            tv4.setText("校准倍数：" + bs + " 砝码重量：" + (int) (k4 / bs));
                            editor.putFloat("four", bs);
                        }
                    }
                    editor.commit();

//                    float one = shared.getFloat("one", (float) 0.00);
//                    float two = shared.getFloat("two", (float) 0.00);
//                    float three = shared.getFloat("three", (float) 0.00);
//                    float four = shared.getFloat("four", (float) 0.00);
//                    String shareStr = "one："+one+" two："+two+" three："+three+" four："+four;
//                    Toast.makeText(AdminActivity.this, shareStr, Toast.LENGTH_LONG).show();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_back:
                Intent intent = new Intent(AdminActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
                break;
            case R.id.btn1_start:
                intput = etMultiple.getText().toString().trim();
                Log.i(TAG, "onClick: ----" + intput);
                if (intput.equals("")) {
                    Toast.makeText(AdminActivity.this, "请输入砝码重量！", Toast.LENGTH_SHORT).show();
                    return;
                } else {
                    input_num = Integer.parseInt(intput);
                }
                onSend(AdminActivity.this, Utils.WEIGHT1);
                break;
            case R.id.btn2_start:
                intput = etMultiple.getText().toString().trim();
                Log.i(TAG, "onClick: ----" + intput);
                if (intput.equals("")) {
                    Toast.makeText(AdminActivity.this, "请输入砝码重量！", Toast.LENGTH_SHORT).show();
                    return;
                } else {
                    input_num = Integer.parseInt(intput);
                }
                onSend(AdminActivity.this, Utils.WEIGHT2);
                break;
            case R.id.btn3_start:
                intput = etMultiple.getText().toString().trim();
                Log.i(TAG, "onClick: ----" + intput);
                if (intput.equals("")) {
                    Toast.makeText(AdminActivity.this, "请输入砝码重量！", Toast.LENGTH_SHORT).show();
                    return;
                } else {
                    input_num = Integer.parseInt(intput);
                }
                onSend(AdminActivity.this, Utils.WEIGHT3);
                break;
            case R.id.btn4_start:
                intput = etMultiple.getText().toString().trim();
                Log.i(TAG, "onClick: ----" + intput);
                if (intput.equals("")) {
                    Toast.makeText(AdminActivity.this, "请输入砝码重量！", Toast.LENGTH_SHORT).show();
                    return;
                } else {
                    input_num = Integer.parseInt(intput);
                }
                onSend(AdminActivity.this, Utils.WEIGHT4);
                break;
            case R.id.btn1_submit:
                onSend(AdminActivity.this, Utils.WEIGHT1);
                break;
            case R.id.btn2_submit:
                onSend(AdminActivity.this, Utils.WEIGHT2);
                break;
            case R.id.btn3_submit:
                onSend(AdminActivity.this, Utils.WEIGHT3);
                break;
            case R.id.btn4_submit:
                onSend(AdminActivity.this, Utils.WEIGHT4);
                break;
        }
    }

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
        if (retval < 0)
            Toast.makeText(context, "Write failed!",
                    Toast.LENGTH_SHORT).show();
    }


    /**
     * 获取视频列表
     *
     * @param context
     * @return
     */
    private List<EntityVideo> getVideoFromSDCard(Context context) {
        List<EntityVideo> sysVideoList = new ArrayList<>();
        // MediaStore.Video.Thumbnails.DATA:视频缩略图的文件路径
        String[] thumbColumns = {MediaStore.Video.Thumbnails.DATA,
                MediaStore.Video.Thumbnails.VIDEO_ID};
        // 视频其他信息的查询条件
        String[] mediaColumns = {MediaStore.Video.Media._ID,
                MediaStore.Video.Media.DATA, MediaStore.Video.Media.DURATION};

        Cursor cursor = context.getContentResolver().query(MediaStore.Video.Media
                        .EXTERNAL_CONTENT_URI,
                mediaColumns, null, null, null);

        if (cursor == null) {
            return sysVideoList;
        }
        if (cursor.moveToFirst()) {
            do {
                EntityVideo info = new EntityVideo();
                int id = cursor.getInt(cursor
                        .getColumnIndex(MediaStore.Video.Media._ID));
                Cursor thumbCursor = context.getContentResolver().query(
                        MediaStore.Video.Thumbnails.EXTERNAL_CONTENT_URI,
                        thumbColumns, MediaStore.Video.Thumbnails.VIDEO_ID
                                + "=" + id, null, null);
                if (thumbCursor.moveToFirst()) {
                    info.setPath(thumbCursor.getString(thumbCursor
                            .getColumnIndex(MediaStore.Video.Thumbnails.DATA)));
                }
                String path = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA));
                info.setPath(path);
                info.setDuration(cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION)));
                sysVideoList.add(info);
            } while (cursor.moveToNext());
        }
        return sysVideoList;
    }

    /**
     * listView单击事件 传视频地址到MainActivity
     *
     * @param adapterView
     * @param view
     * @param i
     * @param l
     */
    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        TextView tvPath = view.findViewById(R.id.tv_path);
        String path = tvPath.getText().toString();
        Intent intent = new Intent(AdminActivity.this, MainActivity.class);
        intent.putExtra("path", path);
        startActivity(intent);
        finish();
    }

    /**
     * 管理员登录
     *
     * @param url   接口地址
     * @param phone 账号
     * @param pwd   密码
     */
    private void postgetAdminlogin(String url, String phone, String pwd, String macCodes) {
        OkGo.<String>post(url)
                .tag(TAG)
                .cacheMode(CacheMode.DEFAULT)
                .params("C_type", "Admin_test")
                .params("C_data1", macCodes)
                .params("C_data2", phone)
                .params("C_data3", pwd)
                .execute(new StringCallback() {
                    @Override
                    public void onSuccess(Response<String> response) {
                        Log.i(TAG, "onSuccess:  body---" + response.body());
                        if (isFinishing()) {
                            Log.d(TAG, "onSuccess: AdminActivity已销毁");
                            return;
                        }
                        try {
                            JSONObject jsonObject = new JSONObject(response.body());
                            Return_str = jsonObject.getInt("Return_str");
                            Admin_test_Flag = jsonObject.getInt("Admin_test_Flag");
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
                    }

                    @Override
                    public void onError(Response<String> response) {
                        super.onError(response);
                        Log.i(TAG, "onError: " + response.body());
                    }
                });
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                long secondTime = System.currentTimeMillis();
                if (secondTime - firstTime > 3000) {
                    Toast.makeText(AdminActivity.this, "再按一次退出程序", Toast.LENGTH_SHORT).show();
                    firstTime = secondTime;
                    return true;
                } else {
                    System.exit(0);
                }
                break;
        }
        return super.onKeyUp(keyCode, event);
    }

    private void readThreadStart() {
        readThread = new readThread();
        readThread.start();
    }

    @Override
    void onPreFinish() {
        // 关闭设备
        isOpen = false;
        MyApplication.driver.CloseDevice();
        if (readThread != null ){
            readThread.close();
        }
        if (handler != null) {
            handler.removeCallbacksAndMessages(null);
        }
        // 取消网络请求
        OkGo.getInstance().cancelTag(TAG);
    }

    private class readThread extends Thread {
        private boolean mRunning = false;

        @Override
        public void run() {
            mRunning = true;
            byte[] buffer = new byte[23];
            while (mRunning) {
                Message msg = Message.obtain();
                if (!isOpen) {
                    break;
                }
                int length = MyApplication.driver.ReadData(buffer, 23);
                if (length > 0) {
                    //以16进制输出
                    //String recv = toHexString(buffer, length);
                    //以字符串形式输出
                    String recv = new String(buffer, 0, length);
                    msg.obj = recv;
                    handler.sendMessage(msg);
                }
            }
        }

        public void close() {
            mRunning = false;
        }
    }
}
