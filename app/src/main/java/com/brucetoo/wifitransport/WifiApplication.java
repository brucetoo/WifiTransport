package com.brucetoo.wifitransport;

import android.app.Application;
import android.content.Context;

import com.karumi.dexter.Dexter;

/**
 * Created by Bruce Too
 * On 7/5/16.
 * At 11:54
 */
public class WifiApplication extends Application {

   static WifiApplication application;
    @Override
    public void onCreate() {
        super.onCreate();
        Dexter.initialize(this);
        application = this;
    }

    public static Context getApplication(){
        return application;
    }

}
