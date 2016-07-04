package com.brucetoo.wifitransport.HotPot;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Bruce Too
 * On 7/1/16.
 * At 17:03
 */
public class Utils {

    public static List<ApplicationInfo> getInstalledAppPathList(Context context){

        List<ApplicationInfo> apps = new ArrayList<>();
        final PackageManager pm = context.getPackageManager();
        //get a list of installed apps.
        List<ApplicationInfo> packages = pm.getInstalledApplications(PackageManager.GET_META_DATA);

        for (ApplicationInfo packageInfo : packages) {
            if((packageInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0){
                apps.add(packageInfo);
//                Log.d(TAG, "Installed package :" + packageInfo.packageName);
//                Log.d(TAG, "Source dir : " + packageInfo.sourceDir);
//                Log.d(TAG, "Launch Activity :" + pm.getLaunchIntentForPackage(packageInfo.packageName));
            }
        }
        return apps;
    }
}
