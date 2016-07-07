package com.brucetoo.wifitransport.HotPot;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.os.StrictMode;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.brucetoo.wifitransport.R;
import com.brucetoo.wifitransport.materialfilepicker.MaterialFilePicker;
import com.brucetoo.wifitransport.materialfilepicker.ui.FilePickerActivity;
import com.joanzapata.android.BaseAdapterHelper;
import com.joanzapata.android.QuickAdapter;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Bruce Too
 * On 6/30/16.
 * At 18:18
 */
public class HotpotActivity extends Activity {

    public static final String TAG = "hots_pot";
    public static final int FILE_REQUEST_ID = 1000;
    private ListView mListWifi;
    private ListView mListClient;
    private ProgressBar mProgressBar;
    private TextView mSendFile;
    private TextView mListStatus;

    private QuickAdapter<ScanResult> mWifiAdapter;
    private QuickAdapter<ClientScanResult> mClientAdapter;
    private WifiReceiver mWifiReceiver;
    private WifiManager mWifiManger;
    private String mFile2Send;

    private Handler mHandler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_hotpot);
        mListWifi = (ListView) findViewById(R.id.list_wifi);
        mListClient = (ListView) findViewById(R.id.list_client);
        mProgressBar = (ProgressBar) findViewById(R.id.progress);
        mSendFile = (TextView) findViewById(R.id.text_file_send);
        mListStatus = (TextView) findViewById(R.id.text_status);

        mWifiManger = (WifiManager) getSystemService(WIFI_SERVICE);
        initWifiList();
        registerReceiver();
        startReceiveService();
//        correctXiaomiNet();
    }

    private void correctXiaomiNet() {

        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                .detectDiskReads()
                .detectDiskWrites()
                .detectNetwork()
                .penaltyLog()
                .build());

    }

    /**
     * For test c2s situation
     */
    private void startReceiveService() {
        Intent intent = new Intent(this, ReceiveService.class);
        intent.putExtra(ReceiveService.INTENT_RECEIVER_RESULT, mReceiveResult);
        startService(intent);
    }


    /**
     * For test download file in browser.
     *
     * @see HotpotActivity#mFinishScannerListener
     */
    private void startUploadService(String serverIp) {
        Intent intent = new Intent(this, UploadService.class);
        intent.putExtra(UploadService.SERVER_IP, serverIp);
        startService(intent);
    }


    private void registerReceiver() {
        mWifiReceiver = new WifiReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        registerReceiver(mWifiReceiver, filter);
    }

    private void initWifiList() {
        mWifiAdapter = new QuickAdapter<ScanResult>(this, R.layout.item_list_wifi) {
            @Override
            protected void convert(BaseAdapterHelper helper, ScanResult item) {
                helper.setText(R.id.text_wifi_name, item.SSID);
            }
        };
        mListWifi.setAdapter(mWifiAdapter);
        mListWifi.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ScanResult item = mWifiAdapter.getItem(position);
                WifiManagerUtils.connectToWiFiAp(HotpotActivity.this, item, "");
            }
        });

        mClientAdapter = new QuickAdapter<ClientScanResult>(this, R.layout.item_list_wifi) {
            @Override
            protected void convert(BaseAdapterHelper helper, ClientScanResult item) {
                helper.setText(R.id.text_wifi_name, item.getDevice());
            }
        };

        mListClient.setAdapter(mClientAdapter);
        mListClient.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                sendFile(mClientAdapter.getItem(position));
            }
        });
    }

    /**
     * if sdk >= 23 we need setting permission
     * 1.Have the <uses-permission> element in the manifest as normal.
     * 2.Call Settings.System.canWrite() to see if you are eligible to write out settings.
     * 3.If canWrite() returns false, start up the ACTION_MANAGE_WRITE_SETTINGS activity
     * then allow your app to actually write to settings.
     */
    public void onCreateWifi(View view) {

        mListClient.setVisibility(View.VISIBLE);
        mListWifi.setVisibility(View.GONE);
        mListStatus.setText("Connected Client list,Click to send file:");
        mProgressBar.setVisibility(View.VISIBLE);

        if (WifiManagerUtils.checkAndCreateWifiAp(this,"brucetoo","")) {
            Log.i(TAG, "create wifi app successful");
            mHandler.postDelayed(mCheckWifiClientRunnable, 1000);
        }
    }


    public void onScanWifi(View view) {

        mListClient.setVisibility(View.GONE);
        mListWifi.setVisibility(View.VISIBLE);
        mListStatus.setText("Wifi Hotspot list,choose one to connect:");
        mProgressBar.setVisibility(View.VISIBLE);
        WifiManagerUtils.checkAndScanWifiList(this);

    }

    @TargetApi(Build.VERSION_CODES.M)
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {
        if (requestCode == WifiManagerUtils.PERMISSIONS_REQUEST_CODE_ACCESS_COARSE_LOCATION
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Log.i(TAG, "SDK_INT >= 22 start scan wifi list");
            WifiManagerUtils.startScanWifiList(this);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (resultCode == Activity.RESULT_OK && requestCode == FILE_REQUEST_ID) {
            mFile2Send = data.getStringExtra(FilePickerActivity.RESULT_FILE_PATH);
            mSendFile.setText("File is already : " + mFile2Send);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && requestCode == WifiManagerUtils.PERMISSIONS_REQUEST_CODE_WRITE_SETTING) {
            if (Settings.System.canWrite(this)) {
                Log.i(TAG, "SDK_INT >= 22 WRITE_SETTING grant");
                if (WifiManagerUtils.checkAndCreateWifiAp(this,"brucetoo","")) {
                    Log.i(TAG, "create wifi app successful");
                    mHandler.postDelayed(mCheckWifiClientRunnable, 1000);
                }
            }
        }
    }



    private Runnable mCheckWifiClientRunnable = new Runnable() {
        @Override
        public void run() {
            WifiManagerUtils.getWifiApClientList(HotpotActivity.this, true, mFinishScannerListener);
            mHandler.postDelayed(this, 2000);//wait 2 sec to scan again
        }
    };

    private ScanFinishListener mFinishScannerListener = new ScanFinishListener() {
        @Override
        public void onFinishScan(ArrayList<ClientScanResult> clients) {
            //update ui
            Log.i(TAG, "scan client over,info = " + clients.toString());

            if (clients.size() != 0) {
                mProgressBar.setVisibility(View.GONE);
                mClientAdapter.replaceAll(clients);
//                startUploadService(clients.get(0).getIpAddr());
                //TODO ? if client device is not empty,stop mCheckWifiClientRunnable
                mHandler.removeCallbacks(mCheckWifiClientRunnable);
            }
        }
    };

    public void browseFile(View view) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            Dexter.checkPermissions(new MultiplePermissionsListener() {
                @Override
                public void onPermissionsChecked(MultiplePermissionsReport report) {
                    if (report.areAllPermissionsGranted()) {
                        new MaterialFilePicker()
                                .withActivity(HotpotActivity.this)
                                .withRequestCode(FILE_REQUEST_ID)
//                                .withFilter(Pattern.compile(".*")) // Filtering files and directories by file name using regexp
                                .withFilterDirectories(true) // Set directories filterable (false by default)
                                .withHiddenFiles(false) // Show hidden files and folders
                                .start();
                    }
                }

                @Override
                public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {

                }
            }, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
    }

    //send file client 2 service
    public void sendFile(View view) {
        WifiManagerUtils.checkAndSendFile(this, WifiManagerUtils.SERVER_IP, mFile2Send, mSendResult);
    }

    //send file service 2 client
    public void sendFile(ClientScanResult item) {
        WifiManagerUtils.checkAndSendFile(this, item.getIpAddr(), mFile2Send, mSendResult);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mHandler.removeCallbacks(mCheckWifiClientRunnable);
        unregisterReceiver(mWifiReceiver);
        stopService(new Intent(this, ReceiveService.class));
    }

    public class WifiReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (WifiManager.SCAN_RESULTS_AVAILABLE_ACTION.equals(action)) {
                //Handle wifi scan result
                Log.i(TAG, "WifiReceiver -> receive list size:" + mWifiManger.getScanResults().size());
                mProgressBar.setVisibility(View.GONE);
                if (mWifiAdapter != null) {
                    mWifiAdapter.replaceAll(WifiManagerUtils.removeDuplicateWifiAp(mWifiManger.getScanResults()));
                }
            } else if (WifiManager.NETWORK_STATE_CHANGED_ACTION.equals(action)) {//wifi connect state
                //Handle wifi connect state
                NetworkInfo info = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
                Log.i(TAG, "WifiReceiver -> receive net info: " + info.getState());
                switch (info.getState()) {
                    case CONNECTED:
                        //get connect wifi info
                        WifiInfo wifiInfo = mWifiManger.getConnectionInfo();
                        Log.i(TAG, "WifiReceiver -> Connect Ip: " + WifiManagerUtils.formatIpAddress(wifiInfo.getIpAddress()));
                        Toast.makeText(context, "Connected " + wifiInfo.getSSID(), Toast.LENGTH_SHORT).show();
                        break;
                    case CONNECTING:
//                        Toast.makeText(context, "WiFi Connecting", Toast.LENGTH_SHORT).show();
                        break;
                    case DISCONNECTING:
                        Toast.makeText(context, "WiFi Disconnecting", Toast.LENGTH_SHORT).show();
                        break;
                    case DISCONNECTED:

                        break;
                    case SUSPENDED:

                        break;
                    case UNKNOWN:

                        break;
                }
            } else if (WifiManager.WIFI_STATE_CHANGED_ACTION.equals(action)) {
                //Handle wifi open or not
                int wifiState = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, 0);
                Log.i(TAG, "WifiReceiver -> wifi State:" + wifiState);
                switch (wifiState) {
                    case WifiManager.WIFI_STATE_DISABLING:  // 0
                        break;
                    case WifiManager.WIFI_STATE_DISABLED:  // 1
                        Toast.makeText(context, "WiFi Closed", Toast.LENGTH_SHORT).show();
                        break;
                    case WifiManager.WIFI_STATE_ENABLING:  // 2
                        break;
                    case WifiManager.WIFI_STATE_ENABLED:  // 3
                        Toast.makeText(context, "WiFi Opened", Toast.LENGTH_SHORT).show();
                        break;
                    case WifiManager.WIFI_STATE_UNKNOWN: // 4
                        break;
                    default:
                        break;
                }

            }
        }
    }

    private ResultReceiver mReceiveResult = new ResultReceiver(null) {
        @Override
        protected void onReceiveResult(final int resultCode, final Bundle resultData) {

            if (resultCode == WifiManagerUtils.SERVER_CONNECT_PORT) {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (resultData.getInt(ReceiveService.BUNDLE_RECEIVE_STATE) == ReceiveService.RECEIVE_SUCCESS) {
                            Toast.makeText(HotpotActivity.this, "Receive file successful!", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(HotpotActivity.this, "Receive file failed!", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        }
    };

    private ResultReceiver mSendResult = new ResultReceiver(null) {
        @Override
        protected void onReceiveResult(final int resultCode, final Bundle resultData) {

            if (resultCode == WifiManagerUtils.SERVER_CONNECT_PORT) {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (resultData.getInt(ReceiveService.BUNDLE_RECEIVE_STATE) == ReceiveService.RECEIVE_SUCCESS) {
                            Toast.makeText(HotpotActivity.this, "Send file successful!", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(HotpotActivity.this, "Send file failed!", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        }
    };
}
