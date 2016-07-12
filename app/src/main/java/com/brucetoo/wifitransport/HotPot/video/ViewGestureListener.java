package com.brucetoo.wifitransport.HotPot.video;

import android.content.Context;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;

import com.brucetoo.wifitransport.HotPot.Utils;

/**
 * Created by Brucetoo
 * On 2015/10/21
 * At 9:58
 */
public class ViewGestureListener extends GestureDetector.SimpleOnGestureListener {

    private static final String TAG = "ViewGestureListener";

    private static final int SWIPE_THRESHOLD = 60;//threshold of swipe
    public static final int SWIPE_LEFT = 1;
    public static final int SWIPE_RIGHT = 2;
    private VideoGestureListener listener;
    private Context context;

    public ViewGestureListener(Context context, VideoGestureListener listener) {
        this.context = context;
        this.listener = listener;
    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        listener.onSingleTap();
        return true;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        float deltaX = e1.getRawX() - e2.getRawX();
        float deltaY = e1.getRawY() - e2.getRawY();

        if (Math.abs(deltaX) > Math.abs(deltaY)) {
            if (Math.abs(deltaX) > SWIPE_THRESHOLD) {
                listener.onHorizontalScroll(deltaX < 0);
            }
        } else {
            if (Math.abs(deltaY) > SWIPE_THRESHOLD) {
                Log.i(TAG, "deltaY->" + deltaY);
                if (e1.getX() < Utils.getDeviceWidth(context) / 2) {//left edge
                    listener.onVerticalScroll(deltaY / Utils.getDeviceHeight(context), SWIPE_LEFT);
                } else if (e1.getX() > Utils.getDeviceWidth(context) / 2) {//right edge
                    listener.onVerticalScroll(deltaY / Utils.getDeviceHeight(context), SWIPE_RIGHT);
                }
            }
        }
        return true;
    }
}
