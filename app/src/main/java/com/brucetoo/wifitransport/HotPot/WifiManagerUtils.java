package com.brucetoo.wifitransport.HotPot;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.ResultReceiver;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.InetAddress;
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
     */
    public static final int PERMISSIONS_REQUEST_CODE_ACCESS_COARSE_LOCATION = 10001;

    /**
     * Create or connect server retry count
     */
    public static final int RETRY_COUNT = 5;


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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.System.canWrite(context)) {
                context.startActivity(new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS));
            }
        }

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

        config.SSID = ssid;
//        config.SSID = "\"" + ssid + "\"";
        Log.e(TAG, "createWifiApConfig ssid :" + config.SSID);
        config.wepKeys[0] = "\"" + pass + "\"";
        config.preSharedKey = "\"" + pass + "\"";
        config.wepTxKeyIndex = 0;
        config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
        config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
        return config;
    }


    /**
     * Connect wifi ap with scanResult and password
     *
     * @param context    context to get WifiManager
     * @param scanResult {@link ScanResult} scan by {@link WifiManagerUtils#startScanWifiList(Context)}
     * @param password   password of wifi ap
     */
    public static void connectToWiFiAp(Context context, ScanResult scanResult, String password) {

        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        try {
            Log.i(TAG, "Connect wifi -> SSID " + scanResult.SSID + " Security : " + scanResult.capabilities);

            String networkSSID = scanResult.SSID;
            String networkPass = "\"" + password + "\"";

            WifiConfiguration conf = new WifiConfiguration();
            //NOTE: if ssid not from ScanResult , quote must be added
//            conf.SSID = "\"" + networkSSID + "\"";   // Please note the quotes. String should contain ssid in quote

            conf.SSID = networkSSID;
            Log.e(TAG, "connectToWiFiAp ssid :" + conf.SSID);
            conf.status = WifiConfiguration.Status.ENABLED;
            conf.priority = 40;

            //handle security = WEP
            if (scanResult.capabilities.toUpperCase().contains("WEP")) {
                Log.i(TAG, "Connect wifi -> Configuring WEP");
                conf.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
                conf.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
                conf.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
                conf.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
                conf.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.SHARED);
                conf.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
                conf.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
                conf.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
                conf.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104);

                if (networkPass.matches("^[0-9a-fA-F]+$")) {
                    conf.wepKeys[0] = networkPass;
                } else {
                    conf.wepKeys[0] = "\"".concat(networkPass).concat("\"");
                }
                conf.wepTxKeyIndex = 0;

                //handle security = WEP
            } else if (scanResult.capabilities.toUpperCase().contains("WPA")) {
                Log.i(TAG, "Connect wifi -> Configuring WPA");

                conf.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
                conf.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
                conf.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
                conf.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
                conf.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
                conf.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
                conf.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104);
                conf.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
                conf.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);

                conf.preSharedKey = "\"" + networkPass + "\"";

            } else {//handle no security(password)
                Log.i(TAG, "Connect wifi ->  Configuring OPEN network");
                conf.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
                conf.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
                conf.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
                conf.allowedAuthAlgorithms.clear();
                conf.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
                conf.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
                conf.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
                conf.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104);
                conf.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
                conf.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
            }

            int networkId = wifiManager.addNetwork(conf);

            Log.i(TAG, "Connect wifi -> Add wifi result: " + networkId);

            List<WifiConfiguration> list = wifiManager.getConfiguredNetworks();
            for (WifiConfiguration i : list) {
                if (i.SSID != null && i.SSID.equals("\"" + networkSSID + "\"")) {
                    Log.i(TAG, "Connect wifi -> WifiConfiguration SSID " + i.SSID);

                    boolean isDisconnected = wifiManager.disconnect();
                    Log.i(TAG, "Connect wifi -> isDisconnected : " + isDisconnected);

                    boolean isEnabled = wifiManager.enableNetwork(i.networkId, true);
                    Log.i(TAG, "Connect wifi -> isEnabled : " + isEnabled);

                    boolean isReconnected = wifiManager.reconnect();
                    Log.i(TAG, "Connect wifi -> isReconnected : " + isReconnected);

                    break;
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
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
    public static void getWifiApClientList(Context context, boolean onlyReachable, ScanFinishListener finishListener) {
        getWifiApClientList(context, onlyReachable, 300, finishListener);
    }

    /**
     * @param reachableTimeout use in {@link InetAddress#isReachable(int)}
     */
    public static void getWifiApClientList(final Context context, final boolean onlyReachable, final int reachableTimeout, final ScanFinishListener finishListener) {

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
                                boolean isReachable = InetAddress.getByName(splitted[0]).isReachable(reachableTimeout);

                                if (!onlyReachable || isReachable) {
                                    result.add(new ClientScanResult(splitted[0], splitted[3], splitted[5], isReachable));
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    Log.e(TAG, e.toString());
                } finally {
                    try {
                        br.close();
                    } catch (IOException e) {
                        Log.e(TAG, e.getMessage());
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
     * @param serverIp       server ip
     * @param sendFile       file path string
     * @param resultReceiver ResultReceiver callback that handle send state
     */
    public static void checkAndSendFile(Context context, String serverIp, String sendFile, ResultReceiver resultReceiver) {

        if (!TextUtils.isEmpty(sendFile)) {
            Intent serviceIntent = new Intent(context, TransferService.class);
            serviceIntent.putExtra(TransferService.INTENT_SERVER_IP, serverIp);//server ip
            serviceIntent.putExtra(TransferService.INTENT_SERVER_PORT, WifiManagerUtils.SERVER_CONNECT_PORT);
            serviceIntent.putExtra(TransferService.INTENT_FILE_TO_SEND, sendFile);
            serviceIntent.putExtra(TransferService.INTENT_SEND_RESULT, resultReceiver);
            context.startService(serviceIntent);
        } else {
            Toast.makeText(context, "No file is chose!", Toast.LENGTH_SHORT).show();
        }
    }

}


