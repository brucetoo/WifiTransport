package com.brucetoo.wifitransport.HotPot;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.provider.Settings;
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
public class ReceiverFragment extends Fragment implements View.OnClickListener {

    private final static String TAG = ReceiverFragment.class.getSimpleName();
    private static final String WIFI_AP_STATE_CHANGED_ACTION = "android.net.wifi.WIFI_AP_STATE_CHANGED";
    public static final int FILE_REQUEST_ID = 1000;
    private static final String WIFI_NAME = "brucetoo";

    private TextView mTextName;
    private TextView mTextClient;
    private TextView mTextFileName;
    private Button mBtnSendFile;
    private Button mBtnBrowseFile;
    private String mFile2Send;
    private Handler mHandler = new Handler();
    private String mClientIp;

    public static ReceiverFragment newInstance() {

        Bundle args = new Bundle();

        ReceiverFragment fragment = new ReceiverFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_receiver, container, false);
        mTextName = (TextView) view.findViewById(R.id.text_name);
        mTextClient = (TextView) view.findViewById(R.id.text_client);
        mTextFileName = (TextView) view.findViewById(R.id.text_file_send);
        mBtnSendFile = (Button) view.findViewById(R.id.btn_send_file);
        mBtnBrowseFile = (Button) view.findViewById(R.id.btn_browse_file);
        mBtnSendFile.setOnClickListener(this);
        mBtnBrowseFile.setOnClickListener(this);
        mTextClient.setOnClickListener(this);
        mTextName.setText("Current Wifi: " + WIFI_NAME);
        mClientIp = "";
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        startReceiveService();
        registerReceiver();
        WifiManagerUtils.checkAndCreateWifiAp(this, WIFI_NAME, "");
        WifiManagerUtils.startScanWifiList(getActivity());
    }

    private WifiReceiver mWifiReceiver;

    private void registerReceiver() {
        mWifiReceiver = new WifiReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(WIFI_AP_STATE_CHANGED_ACTION);
        getActivity().registerReceiver(mWifiReceiver, filter);
    }


    private void startReceiveService() {
        Intent intent = new Intent(getActivity(), ReceiveService.class);
        intent.putExtra(ReceiveService.INTENT_RECEIVER_RESULT, mReceiveResult);
        getActivity().startService(intent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getActivity().unregisterReceiver(mWifiReceiver);
        WifiManagerUtils.closeWifiAp(getActivity());
        getActivity().stopService(new Intent(getActivity(), ReceiveService.class));
    }

    private ResultReceiver mReceiveResult = new ResultReceiver(null) {
        @Override
        protected void onReceiveResult(final int resultCode, final Bundle resultData) {

            if (resultCode == WifiManagerUtils.SERVER_CONNECT_PORT) {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {

                        String ip = resultData.getString(ReceiveService.BUNDLE_RECEIVE_IP);
                        Log.i(TAG, "mReceiveResult ip: " + ip);
                        if (ip != null) {
                            mClientIp = ip;
                            mTextClient.setVisibility(View.VISIBLE);
                            mTextClient.setText(ip);
                        }

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
            WifiManagerUtils.checkAndSendFile(getActivity(), WifiManagerUtils.SERVER_IP, mFile2Send, mSendResult);
        } else if (v == mTextClient) {
            WifiManagerUtils.checkAndSendFile(getActivity(), mClientIp, mFile2Send, mSendResult);
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
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && requestCode == WifiManagerUtils.PERMISSIONS_REQUEST_CODE_WRITE_SETTING) {
            if (Settings.System.canWrite(getActivity())) {
                Log.i(TAG, "SDK_INT >= 22 WRITE_SETTING grant");
                if (WifiManagerUtils.checkAndCreateWifiAp(this, WIFI_NAME, "")) {
                    Log.i(TAG, "create wifi app successful");
                }
            }
        }
    }

    public class WifiReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(WIFI_AP_STATE_CHANGED_ACTION)) {
                int wifiState = intent.getIntExtra("wifi_state", 0); //name = EXTRA_WIFI_STATE
                if (wifiState == 13) { //WIFI_AP_STATE_ENABLED
                    Toast.makeText(getActivity(), "Create ap successfully", Toast.LENGTH_SHORT).show();
                } else if (wifiState == 14) {//WIFI_AP_STATE_FAILED
                    Toast.makeText(getActivity(), "Create ap failed", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
}
