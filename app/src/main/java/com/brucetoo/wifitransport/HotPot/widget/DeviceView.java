package com.brucetoo.wifitransport.HotPot.widget;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.brucetoo.wifitransport.R;

/**
 * Created by Bruce Too
 * On 7/11/16.
 * At 13:39
 */
public class DeviceView extends LinearLayout {

    private ImageView mDeviceImage;
    private TextView mDeviceName;

    public DeviceView(Context context) {
        super(context);
        initView();
    }

    public DeviceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public DeviceView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public DeviceView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initView();
    }

    private void initView() {

        setOrientation(VERTICAL);
        LayoutParams params = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        setLayoutParams(params);
        mDeviceImage = new ImageView(getContext());
        mDeviceImage.setBackgroundResource(R.drawable.pp_img_connect_icon);
        params.gravity = Gravity.CENTER_HORIZONTAL;
        params.bottomMargin = (int) (10 * getContext().getResources().getDisplayMetrics().density);// dp -> px
        mDeviceImage.setLayoutParams(params);
        addView(mDeviceImage);

        mDeviceName = new TextView(getContext());
        mDeviceName.setTextSize(13);
        mDeviceName.setTextColor(Color.WHITE);
        params.bottomMargin = 0;
        mDeviceName.setLayoutParams(params);
        addView(mDeviceName);

    }

    public void setDeviceName(String name) {
        mDeviceName.setText(name);
    }

}

