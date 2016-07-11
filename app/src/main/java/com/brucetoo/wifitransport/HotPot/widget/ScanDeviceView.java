package com.brucetoo.wifitransport.HotPot.widget;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.brucetoo.wifitransport.R;

/**
 * Created by Bruce Too
 * On 7/11/16.
 * At 10:35
 */
public class ScanDeviceView extends FrameLayout {

    private static final long ANIMATE_DURATION = 4000;
    private RelativeLayout mViewScan;
    private View mViewScanWheel;
    private View mViewScanDot;
    private View mViewScanBg;
    private TextView mTextState;
    private ObjectAnimator mRotateAnim;

    public ScanDeviceView(Context context) {
        super(context);
        initView();
    }

    public ScanDeviceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public ScanDeviceView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public ScanDeviceView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initView();
    }

    private void initView() {
        mViewScan = (RelativeLayout) LayoutInflater.from(getContext()).inflate(R.layout.pp_layout_scan_device, null);
        mViewScanWheel = mViewScan.findViewById(R.id.pp_view_scan_wheel);
        mViewScanDot = mViewScan.findViewById(R.id.pp_view_scan_dot);
        mViewScanBg = mViewScan.findViewById(R.id.pp_view_bg);
        mTextState = (TextView) mViewScan.findViewById(R.id.pp_text_state);
        addView(mViewScan);

        mTextState.setText("正在等待连接者");

        mRotateAnim = ObjectAnimator.ofFloat(mViewScanWheel, "rotation", 0, 360);
        mRotateAnim.setDuration(ANIMATE_DURATION);
        mRotateAnim.setInterpolator(new LinearInterpolator());
        mRotateAnim.setRepeatCount(ValueAnimator.INFINITE);
        mRotateAnim.setRepeatMode(ValueAnimator.RESTART);
        mRotateAnim.start();

        mTextState.postDelayed(new Runnable() {
            @Override
            public void run() {
                showDevices(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Toast.makeText(getContext(), v.getTag().toString(), Toast.LENGTH_SHORT).show();
                    }
                }, "Nexus 1", "Nexus 2", "Nexus 3", "Nexus 4");
            }
        }, 6000);//test

    }


    /**
     * show device in radar panel,and click to connect.
     * support 4 devices at most.
     *
     * @param onClickListener device click listener
     * @param deviceName      device names(Probably can to {@link android.net.wifi.ScanResult} instead)
     */
    public void showDevices(OnClickListener onClickListener, String... deviceName) {

        mTextState.setText("点击头像确认发送");
        mRotateAnim.cancel();
        mViewScanWheel.setVisibility(GONE);
        mViewScanDot.setVisibility(GONE);

        for (int i = 0; i < deviceName.length; i++) {
            if (i <= 3) {//only show 4 devices
                DeviceView view = new DeviceView(getContext());
                view.setDeviceName(deviceName[i]);
                int offset = 20;//probably 20
                RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                switch (i) {
                    case 0:
                        view.setTag(deviceName[0]);
                        params.addRule(RelativeLayout.ALIGN_LEFT, mViewScanBg.getId());
                        params.addRule(RelativeLayout.ALIGN_TOP, mViewScanBg.getId());
                        params.leftMargin = offset;
                        params.topMargin = offset;
                        break;
                    case 1:
                        view.setTag(deviceName[1]);
                        params.addRule(RelativeLayout.ALIGN_RIGHT, mViewScanBg.getId());
                        params.addRule(RelativeLayout.ALIGN_TOP, mViewScanBg.getId());
                        params.rightMargin = offset;
                        params.topMargin = offset;
                        break;
                    case 2:
                        view.setTag(deviceName[2]);
                        params.addRule(RelativeLayout.ALIGN_BOTTOM, mViewScanBg.getId());
                        params.addRule(RelativeLayout.ALIGN_LEFT, mViewScanBg.getId());
//                        params.bottomMargin = offset;//ignore text height
                        params.leftMargin = offset;
                        break;
                    case 3:
                        view.setTag(deviceName[3]);
                        params.addRule(RelativeLayout.ALIGN_RIGHT, mViewScanBg.getId());
                        params.addRule(RelativeLayout.ALIGN_BOTTOM, mViewScanBg.getId());
                        params.rightMargin = offset;
//                        params.bottomMargin = offset;
                        break;
                }
                mViewScan.addView(view, params);
                view.setOnClickListener(onClickListener);
            }
        }

    }


    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mRotateAnim.cancel();
        mRotateAnim = null;
    }
}
