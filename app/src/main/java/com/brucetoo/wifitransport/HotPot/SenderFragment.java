package com.brucetoo.wifitransport.HotPot;

import android.Manifest;
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
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.brucetoo.wifitransport.R;
import com.brucetoo.wifitransport.WifiApplication;
import com.brucetoo.wifitransport.materialfilepicker.MaterialFilePicker;
import com.brucetoo.wifitransport.materialfilepicker.ui.FilePickerActivity;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;

import java.util.List;

/**
 * Created by Bruce Too
 * On 7/7/16.
 * At 11:17
 */
public class SenderFragment extends Fragment implements View.OnClickListener {

    private final static String TAG = SenderFragment.class.getSimpleName();
    public static final int FILE_REQUEST_ID = 1000;

    private TextView mTextClient;
    private TextView mTextFileName;
    private Button mBtnSendFile;
    private Button mBtnBrowseFile;
    private String mFile2Send;
    private Handler mHandler = new Handler();
    private ScanResult mCurrentResult;
    private String mCurrentIp;
    private WifiReceiver mWifiReceiver;
    private boolean mCanConnectWifi = true;

    public static SenderFragment newInstance() {

        Bundle args = new Bundle();

        SenderFragment fragment = new SenderFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_sender, container, false);
        mTextClient = (TextView) view.findViewById(R.id.text_client);
        mTextFileName = (TextView) view.findViewById(R.id.text_file_send);
        mBtnSendFile = (Button) view.findViewById(R.id.btn_send_file);
        mBtnBrowseFile = (Button) view.findViewById(R.id.btn_browse_file);
        mBtnSendFile.setOnClickListener(this);
        mBtnBrowseFile.setOnClickListener(this);
        mTextClient.setVisibility(View.GONE);
        mTextClient.setOnClickListener(this);
        mCurrentResult = null;
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        startReceiveService();
        registerReceiver();
        WifiManagerUtils.checkAndScanWifiList(this);
    }

    private void startReceiveService() {
        Intent intent = new Intent(getActivity(), ReceiveService.class);
        intent.putExtra(ReceiveService.INTENT_RECEIVER_RESULT, mReceiveResult);
        getActivity().startService(intent);
    }

    private void registerReceiver() {
        mWifiReceiver = new WifiReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        getActivity().registerReceiver(mWifiReceiver, filter);
    }


    private ResultReceiver mReceiveResult = new ResultReceiver(null) {
        @Override
        protected void onReceiveResult(final int resultCode, final Bundle resultData) {

            if (resultCode == WifiManagerUtils.SERVER_CONNECT_PORT) {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (resultData.getInt(ReceiveService.BUNDLE_RECEIVE_STATE) == ReceiveService.RECEIVE_SUCCESS) {
                            Toast.makeText(WifiApplication.getApplication(), "Receive file successful!", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(WifiApplication.getApplication(), "Receive file failed!", Toast.LENGTH_SHORT).show();
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
                            Toast.makeText(WifiApplication.getApplication(), "Send file successful!", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(WifiApplication.getApplication(), "Send file failed!", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        }
    };

    @Override
    public void onClick(View v) {
        if (v == mBtnBrowseFile) {
            //TODO handle under Jelly_bean
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                Dexter.checkPermissions(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport report) {
                        if (report.areAllPermissionsGranted()) {
                            Intent start = new MaterialFilePicker()
                                    .withFragment(getActivity())
                                    .withRequestCode(FILE_REQUEST_ID)
//                                .withFilter(Pattern.compile(".*")) // Filtering files and directories by file name using regexp
                                    .withFilterDirectories(true) // Set directories filterable (false by default)
                                    .withHiddenFiles(false) // Show hidden files and folders
                                    .start();
                            startActivityForResult(start, FILE_REQUEST_ID);
                        }
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {

                    }
                }, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE);
            }
        } else if (v == mBtnSendFile) {
            WifiManagerUtils.checkAndSendFile(getActivity(), mCurrentIp, WifiManagerUtils.SERVER_IP, mFile2Send, mSendResult);
        } else if (v == mTextClient) {
            if(mCanConnectWifi) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        WifiManagerUtils.connectToWiFiAp(getActivity(), mCurrentResult, "");
                    }
                }).start();
                mCanConnectWifi = false;
            }else {
                Toast.makeText(getActivity(), "Connecting,wait a second!", Toast.LENGTH_SHORT).show();
            }

        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        if (requestCode == WifiManagerUtils.PERMISSIONS_REQUEST_CODE_ACCESS_COARSE_LOCATION
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Log.i(TAG, "SDK_INT >= 22 start scan wifi list");
            WifiManagerUtils.startScanWifiList(getActivity());
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (resultCode == Activity.RESULT_OK && requestCode == FILE_REQUEST_ID) {
            mFile2Send = data.getStringExtra(FilePickerActivity.RESULT_FILE_PATH);
            mTextFileName.setText("File is already : " + mFile2Send);
            WifiManagerUtils.openSystemDefaultViewApp(getActivity(),mFile2Send);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getActivity().unregisterReceiver(mWifiReceiver);
        getActivity().stopService(new Intent(getActivity(), ReceiveService.class));
    }

    public class WifiReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
            if (WifiManager.SCAN_RESULTS_AVAILABLE_ACTION.equals(action)) {
                //Handle wifi scan result
                Log.i(TAG, "WifiReceiver -> receive list size:" + wifiManager.getScanResults().size());
                List<ScanResult> scanResults = WifiManagerUtils.removeDuplicateWifiAp(wifiManager.getScanResults());
                for (ScanResult result : scanResults) {
                    //TODO add wifi filter
                    if (result.SSID.equals("brucetoo")) {
                        mTextClient.setVisibility(View.VISIBLE);
                        mTextClient.setText(result.SSID);
                        mCurrentResult = result;
                        return;
                    } else {
                        mTextClient.setVisibility(View.GONE);
                    }
                }
            } else if (WifiManager.NETWORK_STATE_CHANGED_ACTION.equals(action)) {//wifi connect state
                //Handle wifi connect state
                NetworkInfo info = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
                Log.i(TAG, "WifiReceiver -> receive net info: " + info.getState());
                switch (info.getState()) {
                    case CONNECTED:
                        mCanConnectWifi = true;
                        //get connect wifi info
                        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
                        mCurrentIp = WifiManagerUtils.formatIpAddress(wifiInfo.getIpAddress());
                        Log.i(TAG, "WifiReceiver -> Connect Ip: " + mCurrentIp);
                        Log.i(TAG, "WifiReceiver -> Ping Ip: " + WifiManagerUtils.checkIpReachable(mCurrentIp));
                        Toast.makeText(context, "Connected " + wifiInfo.getSSID(), Toast.LENGTH_SHORT).show();
                        break;
                    case CONNECTING:
                    case DISCONNECTING:
                    case DISCONNECTED:
                    case SUSPENDED:
                    case UNKNOWN:
                        mCanConnectWifi = false;
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
}
