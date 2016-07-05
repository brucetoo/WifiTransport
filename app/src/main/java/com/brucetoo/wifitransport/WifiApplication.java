package com.brucetoo.wifitransport;

import android.app.Application;

import com.karumi.dexter.Dexter;

/**
 * Created by Bruce Too
 * On 7/5/16.
 * At 11:54
 */
public class WifiApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Dexter.initialize(this);
    }
}
