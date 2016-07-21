package com.brucetoo.wifitransport.HotPot;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.ResultReceiver;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Bruce Too
 * On 6/30/16.
 * At 18:13
 */
public class WifiManagerUtils {

    private static final String TAG = WifiManagerUtils.class.getSimpleName();
    /**
     * Wifi ap server ip
     */
    public static final String SERVER_IP = "192.168.43.1";
    /**
     * Wifi ap server port use in {@link ReceiveService} and {@link TransferService}
     */
    public static final int SERVER_CONNECT_PORT = 7878;

    /**
     * Wifi ap server port use in {@link UploadService}
     */
    public static final int SERVER_UPLOAD_PORT = 7879;

    /**
     * Wifi ap server port retry offset
     */
    public static final int SERVER_PORT_RETRY_OFFSET = 10;

    /**
     * Request code of  ACCESS_COARSE_LOCATION permission
     * startActivityForResult() in FragmentActivity requires the requestCode to be of 16 bits,
     * meaning the range is from 0 to 65535.
     * <p/>
     * Also, validateRequestPermissionsRequestCode in FragmentActivity requires requestCode to be of 8 bits,
     * meaning the range is from 0 to 255.
     */
    public static final int PERMISSIONS_REQUEST_CODE_ACCESS_COARSE_LOCATION = 111;

    public static final int PERMISSIONS_REQUEST_CODE_WRITE_SETTING = 112;

    /**
     * Create or connect server retry count
     */
    public static final int RETRY_COUNT = 5;

    /**
     * Perfect buffer size for file system
     */
    public static final int PERFECT_BUFFER_SIZE = 4096;


    /**
     * Check if wifi ap is enable
     *
     * @param wifiManager WifiManager
     * @return if wifi ap is enable
     */
    public static boolean isWifiApEnabled(WifiManager wifiManager) {
        try {
            Method method = wifiManager.getClass().getMethod("isWifiApEnabled");
            method.setAccessible(true);
            return (Boolean) method.invoke(wifiManager);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Scan wifi list
     * handle result in broadcast action {@link WifiManager#SCAN_RESULTS_AVAILABLE_ACTION}
     *
     * @see com.brucetoo.wifitransport.HotPot.HotpotActivity.WifiReceiver
     */
    public static void startScanWifiList(Context context) {

        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        if (isWifiApEnabled(wifiManager)) {
            closeWifiAp(wifiManager);
        }
        if (!wifiManager.isWifiEnabled()) {
            wifiManager.setWifiEnabled(true);
        }
        wifiManager.startScan();
    }


    /**
     * Create wifi ap by different android version
     * Need permission android.permission.WRITE_SETTINGS
     *
     * @param context context
     * @param ssid    wifi ap name
     * @param pass    wifi ap password
     * @return if create ap successful
     */
    public static boolean createWifiAp(Context context, String ssid, String pass) {

        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);

        WifiConfiguration wificonfiguration = createWifiApConfig(ssid, pass);
        try {
            if (isWifiApEnabled(wifiManager)) {
                closeWifiAp(wifiManager);
            }
            if (wifiManager.isWifiEnabled()) {
                wifiManager.setWifiEnabled(false);
            }
            Method method = wifiManager.getClass().getMethod("setWifiApEnabled", WifiConfiguration.class, boolean.class);
            method.invoke(wifiManager, wificonfiguration, true);
            Log.i(TAG, "create ap successful");
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "create ap error:" + e.getMessage());
        }
        return false;
    }

    /**
     * Close wifi ap with Context
     *
     * @param context context to get WifiManager
     * @return close success or not
     */
    public static boolean closeWifiAp(Context context) {
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        return closeWifiAp(wifiManager);
    }

    /**
     * Close wifi ap with WifiManager
     *
     * @param wifiManager WifiManager
     * @return close success or not
     */
    public static boolean closeWifiAp(WifiManager wifiManager) {
        boolean ret = false;
        if (isWifiApEnabled(wifiManager)) {
            try {
                Method method = wifiManager.getClass().getMethod(
                        "getWifiApConfiguration");
                method.setAccessible(true);
                WifiConfiguration config = (WifiConfiguration) method
                        .invoke(wifiManager);
                Method method2 = wifiManager.getClass().getMethod(
                        "setWifiApEnabled", WifiConfiguration.class,
                        boolean.class);
                ret = (Boolean) method2.invoke(wifiManager, config, false);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return ret;
    }

    /**
     * Create a {@link WifiConfiguration} with ssid and password to start a wifi ap
     *
     * @param ssid wifi ap name
     * @param pass wifi ap password
     * @return {@link WifiConfiguration}
     */
    private static WifiConfiguration createWifiApConfig(String ssid, String pass) {

        WifiConfiguration config = new WifiConfiguration();

        config.allowedAuthAlgorithms.clear();
        config.allowedGroupCiphers.clear();
        config.allowedKeyManagement.clear();
        config.allowedPairwiseCiphers.clear();
        config.allowedProtocols.clear();
//        config.SSID = "\"" + ssid + "\"";
        config.SSID = ssid;
        Log.e(TAG, "createWifiApConfig ssid :" + config.SSID);
        return config;
    }


    private static WifiConfiguration clearExistConfigure(WifiManager wifiManager, String SSID) {

        List<WifiConfiguration> existingConfigs = wifiManager.getConfiguredNetworks();

        for (WifiConfiguration existingConfig : existingConfigs) {

            if (existingConfig.SSID.equals("\"" + SSID + "\"")) {

                return existingConfig;
            }
        }
        return null;
    }


    /**
     * Connect wifi ap with scanResult and password
     * Note:we need call this method in sub thread,and do call this again
     * util {@link WifiManager#NETWORK_STATE_CHANGED_ACTION} callback
     *
     * @param context    context to get WifiManager
     * @param scanResult {@link ScanResult} scan by {@link WifiManagerUtils#startScanWifiList(Context)}
     * @param password   password of wifi ap
     */
    public static void connectToWiFiAp(Context context, ScanResult scanResult, String password) {

        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);

//        try {
        Log.i(TAG, "Connect wifi -> Result item: " + scanResult.toString());

        WifiConfiguration tempConfig = clearExistConfigure(wifiManager, scanResult.SSID);
        if (tempConfig != null) {
            Log.i(TAG, "connectToWiFiAp clearExistConfigure");
            wifiManager.removeNetwork(tempConfig.networkId);
        }

        WifiConfiguration wifiConfiguration = new WifiConfiguration();
        wifiConfiguration.SSID = "\"" + scanResult.SSID + "\"";
        wifiConfiguration.BSSID = scanResult.BSSID;
        wifiConfiguration.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
        wifiConfiguration.hiddenSSID = false;

        int netID = wifiManager.addNetwork(wifiConfiguration);

        Log.i(TAG, "connectToWiFiAp netID :" + netID);
        if(netID != -1){
            boolean isDisconnected = wifiManager.disconnect();
            Log.i(TAG, "Connect wifi -> isDisconnected : " + isDisconnected);

            boolean isEnabled = wifiManager.enableNetwork(netID, true);
            Log.i(TAG, "Connect wifi -> isEnabled : " + isEnabled);

            boolean isReconnected = wifiManager.reconnect();
            Log.i(TAG, "Connect wifi -> isReconnected : " + isReconnected);
        }
    }

    /**
     * Format int ip(1778493632) to *.*.*.*
     *
     * @param ipAddress int ip address
     * @return return *.*.*.*
     */
    public static String formatIpAddress(int ipAddress) {

        return (ipAddress & 0xFF) + "." +
                ((ipAddress >> 8) & 0xFF) + "." +
                ((ipAddress >> 16) & 0xFF) + "." +
                (ipAddress >> 24 & 0xFF);
    }

    /**
     * NOTE: this method is not available in Lenovo
     * Start a thread to read access point to local device when opening ap.
     * File location: "/proc/net/arp"
     * File structure:
     * IP address       HW type     Flags       HW address            Mask     Device
     * 10.63.253.193    0x1         0x2         00:11:92:06:85:3f     *        eth0
     *
     * @param context        to get {@link Looper#getMainLooper()}
     * @param onlyReachable  filter if keep reachable devices
     * @param finishListener callback when scan file finished
     */
    public static void getWifiApClientList(final Context context, final boolean onlyReachable, final ScanFinishListener finishListener) {

        Runnable runnable = new Runnable() {
            public void run() {

                BufferedReader br = null;
                final ArrayList<ClientScanResult> result = new ArrayList<>();

                try {
                    br = new BufferedReader(new FileReader("/proc/net/arp"));
                    String line;
                    while ((line = br.readLine()) != null) {
                        String[] splitted = line.split(" +");

                        if ((splitted != null) && (splitted.length >= 4)) {
                            // Basic sanity check
                            String mac = splitted[3];

                            if (mac.matches("..:..:..:..:..:..")) {
                                boolean isReachable = checkIpReachable((splitted[0]));

                                if (!onlyReachable || isReachable) {
                                    result.add(new ClientScanResult(splitted[0], splitted[3], splitted[5], isReachable));
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    Log.e(TAG, "getWifiApClientList:" + e.toString());
                } finally {
                    try {
                        br.close();
                    } catch (IOException e) {
                        Log.e(TAG, "getWifiApClientList:" + e.getMessage());
                    }
                }

                // Get a handler that can be used to post to the main thread
                Handler mainHandler = new Handler(context.getMainLooper());
                Runnable myRunnable = new Runnable() {
                    @Override
                    public void run() {
                        finishListener.onFinishScan(result);
                    }
                };
                mainHandler.post(myRunnable);
            }
        };

        new Thread(runnable).start();
    }


    /**
     * check ip address is reachable or not
     *
     * @param ip ip need be pinged
     * @return reachable
     */
    public static boolean checkIpReachable(String ip) {
        Process p1 = null;
        try {
            //-c indicate Unix system,
            //-n indicate Window system
            p1 = Runtime.getRuntime().exec("ping -c 1 " + ip);
            return p1.waitFor() == 0;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        } catch (InterruptedException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Remove duplicate wifi ap according {@link ScanResult#level}
     *
     * @param scanResults wifi scan results
     * @return list of clearing duplicate wifi
     */
    public static List<ScanResult> removeDuplicateWifiAp(List<ScanResult> scanResults) {
        List<ScanResult> results = new ArrayList<>();
        HashMap<String, Integer> signalStrength = new HashMap<String, Integer>();
        try {
            for (int i = 0; i < scanResults.size(); i++) {
                ScanResult result = scanResults.get(i);
                if (!result.SSID.isEmpty()) {
                    String key = result.SSID;
                    if (!signalStrength.containsKey(key)) {
                        results.add(result);
                        signalStrength.put(key, results.size() - 1);
                    } else {
                        int position = signalStrength.get(key);
                        ScanResult updateItem = results.get(position);
                        //keep high level
                        if (updateItem.level < result.level) {
                            results.set(position, updateItem);
                        }
                    }
                }
            }

            return results;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Check file if is exist,and send file to server
     *
     * @param context        context
     * @param currentIp      only use in client connect to server
     * @param serverIp       server ip
     * @param sendFile       file path string
     * @param resultReceiver ResultReceiver callback that handle send state
     */
    public static void checkAndSendFile(Context context, String currentIp, String serverIp, String sendFile, ResultReceiver resultReceiver) {

        if (!TextUtils.isEmpty(sendFile)) {
            Intent serviceIntent = new Intent(context, TransferService.class);
            serviceIntent.putExtra(TransferService.INTENT_SERVER_IP, serverIp);//server ip
            serviceIntent.putExtra(TransferService.INTENT_SERVER_PORT, WifiManagerUtils.SERVER_CONNECT_PORT);
            serviceIntent.putExtra(TransferService.INTENT_FILE_TO_SEND, sendFile);
            serviceIntent.putExtra(TransferService.INTENT_CURRENT_IP, currentIp);
            serviceIntent.putExtra(TransferService.INTENT_SEND_RESULT, resultReceiver);
            context.startService(serviceIntent);
        } else {
            Toast.makeText(context, "No file is chose!", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Check file if is exist,and send file to server
     *
     * @param context        context
     * @param serverIp       server ip
     * @param sendFile       file path string
     * @param resultReceiver ResultReceiver callback that handle send state
     */
    public static void checkAndSendFile(Context context, String serverIp, String sendFile, ResultReceiver resultReceiver) {

        checkAndSendFile(context, "", serverIp, sendFile, resultReceiver);
    }

    /**
     * Check android version,if > M handle permission callback in
     * {@link Activity#onRequestPermissionsResult(int, String[], int[])}
     *
     * @param context activity that handle onRequestPermissionsResult
     * @see HotpotActivity#onRequestPermissionsResult(int, String[], int[])
     */
    public static void checkAndScanWifiList(Activity context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            context.requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                    PERMISSIONS_REQUEST_CODE_ACCESS_COARSE_LOCATION);
        } else {
            Log.i(TAG, "SDK_INT < 22 start scan wifi list");
            WifiManagerUtils.startScanWifiList(context);
        }
    }

    public static void checkAndScanWifiList(Fragment fragment) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && ActivityCompat.checkSelfPermission(fragment.getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            fragment.requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                    PERMISSIONS_REQUEST_CODE_ACCESS_COARSE_LOCATION);
        } else {
            Log.i(TAG, "SDK_INT < 22 start scan wifi list");
            WifiManagerUtils.startScanWifiList(fragment.getActivity());
        }
    }


    /**
     * Before create wifi ap ,need check android version and decide request WRITE_SETTING or not,
     * if version >= 22 and not be granted,need handle grant result in
     * {@link HotpotActivity#onActivityResult(int, int, Intent)
     * if version < 22 handle result in
     * {@link HotpotActivity#onCreateWifi(View)}}
     *
     * @param activity Activity to startActivityForResult
     * @param ssid     wifi ap ssid
     * @param pass     wifi ap password
     * @return create wifi ap successful or not
     */
    public static boolean checkAndCreateWifiAp(Activity activity, String ssid, String pass) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.System.canWrite(activity)) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS,
                    Uri.parse("package:" + activity.getPackageName()));
            activity.startActivityForResult(intent, WifiManagerUtils.PERMISSIONS_REQUEST_CODE_WRITE_SETTING);
        } else {
            return WifiManagerUtils.createWifiAp(activity, ssid, pass);
        }

        return false;
    }

    /**
     * Create wifi ap from fragment
     *
     * @param fragment from fragment
     */
    public static boolean checkAndCreateWifiAp(Fragment fragment, String ssid, String pass) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.System.canWrite(fragment.getActivity())) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS,
                    Uri.parse("package:" + fragment.getActivity().getPackageName()));
            fragment.startActivityForResult(intent, WifiManagerUtils.PERMISSIONS_REQUEST_CODE_WRITE_SETTING);
        } else {
            return WifiManagerUtils.createWifiAp(fragment.getActivity(), ssid, pass);
        }

        return false;
    }


    /**
     * Open system default view app by file MIME type
     *
     * @param context  context for start activity
     * @param filePath file to explore
     */
    public static void openSystemDefaultViewApp(Context context, String filePath) {

        Intent intent = new Intent();
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setAction(android.content.Intent.ACTION_VIEW);
        File file = new File(filePath);
        intent.setDataAndType(Uri.fromFile(file), getFileMIMEType(file));
        context.startActivity(intent);

    }

    public static final String[][] MIME_TABLE = {
            {".3gp", "video/3gpp"},
            {".apk", "application/vnd.android.package-archive"},
            {".asf", "video/x-ms-asf"},
            {".avi", "video/x-msvideo"},
            {".bin", "application/octet-stream"},
            {".bmp", "image/bmp"},
            {".c", "text/plain"},
            {".class", "application/octet-stream"},
            {".conf", "text/plain"},
            {".cpp", "text/plain"},
            {".doc", "application/msword"},
            {".docx", "application/vnd.openxmlformats-officedocument.wordprocessingml.document"},
            {".xls", "application/vnd.ms-excel"},
            {".xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"},
            {".exe", "application/octet-stream"},
            {".gif", "image/gif"},
            {".gtar", "application/x-gtar"},
            {".gz", "application/x-gzip"},
            {".h", "text/plain"},
            {".htm", "text/html"},
            {".html", "text/html"},
            {".jar", "application/java-archive"},
            {".java", "text/plain"},
            {".jpeg", "image/jpeg"},
            {".jpg", "image/jpeg"},
            {".js", "application/x-javascript"},
            {".log", "text/plain"},
            {".m3u", "audio/x-mpegurl"},
            {".m4a", "audio/mp4a-latm"},
            {".m4b", "audio/mp4a-latm"},
            {".m4p", "audio/mp4a-latm"},
            {".m4u", "video/vnd.mpegurl"},
            {".m4v", "video/x-m4v"},
            {".mov", "video/quicktime"},
            {".mp2", "audio/x-mpeg"},
            {".mp3", "audio/x-mpeg"},
            {".mp4", "video/mp4"},
            {".mpc", "application/vnd.mpohun.certificate"},
            {".mpe", "video/mpeg"},
            {".mpeg", "video/mpeg"},
            {".mpg", "video/mpeg"},
            {".mpg4", "video/mp4"},
            {".mpga", "audio/mpeg"},
            {".msg", "application/vnd.ms-outlook"},
            {".ogg", "audio/ogg"},
            {".pdf", "application/pdf"},
            {".png", "image/png"},
            {".pps", "application/vnd.ms-powerpoint"},
            {".ppt", "application/vnd.ms-powerpoint"},
            {".pptx", "application/vnd.openxmlformats-officedocument.presentationml.presentation"},
            {".prop", "text/plain"},
            {".rc", "text/plain"},
            {".rmvb", "audio/x-pn-realaudio"},
            {".rtf", "application/rtf"},
            {".sh", "text/plain"},
            {".tar", "application/x-tar"},
            {".tgz", "application/x-compressed"},
            {".txt", "text/plain"},
            {".wav", "audio/x-wav"},
            {".wma", "audio/x-ms-wma"},
            {".wmv", "audio/x-ms-wmv"},
            {".wps", "application/vnd.ms-works"},
            {".xml", "text/plain"},
            {".z", "application/x-compress"},
            {".zip", "application/x-zip-compressed"},
            {"", "*/*"}
    };

    public static String getFileMIMEType(File file) {

        String type = "*/*";//default type
        String fName = file.getName();
        int dotIndex = fName.lastIndexOf(".");
        if (dotIndex < 0) {//do not have suffix
            return type;
        }

        String suffix = fName.substring(dotIndex, fName.length()).toLowerCase();
        if (suffix.equals(""))
            return type;
        for (int i = 0; i < MIME_TABLE.length; i++) {
            if (suffix.equals(MIME_TABLE[i][0]))
                type = MIME_TABLE[i][1];
        }
        return type;
    }
}


