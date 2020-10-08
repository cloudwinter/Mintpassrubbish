package com.inprintech.mintpassrubbish.widget;

import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

/**
 * Created by xiayundong on 2020/10/8.
 */
public class BarcodeInputWatcher implements View.OnFocusChangeListener, TextWatcher, TextView.OnEditorActionListener {
    //    private final ILogger logger = LoggerFactory.getLogger(BarcodeInputWatcher.class);
    private static final String TAG = "BarcodeInputWatcher";
    private static final long ONE_MILLION = 1000000;
    /**
     * 判定扫描枪输入的最小间隔，模拟器3000毫秒，真机300毫秒
     */
    private static final int BARCODE_INPUT_INTERVAL = 500;
    // 开始输入的时刻
    private long mBeginning;
    // 扫码监听器
    private OnBarcodeInputListener mOnBarcodeInputListener;
    // 替身 EditText，通过构造方法传入
    private EditText mEditText;

    public BarcodeInputWatcher(EditText editText) {
        editText.setOnEditorActionListener(this);
        editText.setOnFocusChangeListener(this);
        editText.addTextChangedListener(this);
        mEditText = editText;
    }

    public void setOnBarcodeInputListener(OnBarcodeInputListener onBarcodeInputListener) {
        mOnBarcodeInputListener = onBarcodeInputListener;
    }

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        // 监听回车键. 这里注意要作判断处理，ActionDown、ActionUp 都会回调到这里，不作处理的话就会调用两次
        boolean isEnter = event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN;
        if (isEnter || actionId == EditorInfo.IME_ACTION_DONE) {
            long duration = (System.nanoTime() - mBeginning) / ONE_MILLION;
            String text = v.getText().toString();
            Log.i("BarcodeInputWatcher", "点击了 Enter. 输入的字符:" + text + " 耗时" + duration + "ms");
            if (duration < BARCODE_INPUT_INTERVAL) {
                if (mOnBarcodeInputListener != null) {
                    mOnBarcodeInputListener.onBarcodeInput(text);
                }
            }
            mBeginning = 0;
            mEditText.setText("");
            // 如果返回 false，当输入字符回车时，会自动调用输入框的点击事件
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        Log.i(TAG, "onFocusChange. hasFocus:" + hasFocus);
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        Log.i(TAG, "beforeTextChanged. s:" + s + ", start:" + start + ", count:" + count + ", after:" + after + "");
        // 重新输入时，重置计时器
        if (TextUtils.isEmpty(s) || start == 0) {
            mBeginning = System.nanoTime();
        }
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        Log.i(TAG, "onTextChanged. s:" + s + ", start:" + start + ", count:" + count + ", before:" + before + "");
    }

    @Override
    public void afterTextChanged(Editable s) {
        Log.i(TAG, "afterTextChanged. s:" + s + "");
    }

    /**
     * 扫码输入监听器
     */
    public interface OnBarcodeInputListener {
        /**
         * 扫码输入完成
         *
         * @param barcode 输入的条码
         */
        void onBarcodeInput(String barcode);
    }
}
