package com.brucetoo.wifitransport;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.brucetoo.wifitransport.HotPot.TransferService;
import com.brucetoo.wifitransport.materialfilepicker.MaterialFilePicker;
import com.brucetoo.wifitransport.materialfilepicker.ui.FilePickerActivity;

import java.io.File;
import java.util.regex.Pattern;

/**
 * Created by Bruce Too
 * On 6/30/16.
 * At 12:43
 */
public class DeviceDetailFragment extends Fragment implements WifiP2pManager.ConnectionInfoListener {

    private TextView mDeviceAddress;
    private TextView mDeviceInfo;
    private TextView mGroupOwner;
    private TextView mGroupIp;
    private TextView mStatus;
    private View mBrowseFile;
    private View mBtnConnect;

    private WifiP2pDevice mCurrentDevice;
    private WifiP2pInfo mCurrentInfo;
    private ProgressDialog mDialog;
    private Intent mServerIntent;

    private static boolean mIsServiceRunning = false;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_device_detail, container, false);
        mDeviceAddress = (TextView) view.findViewById(R.id.text_device_address);
        mDeviceInfo = (TextView) view.findViewById(R.id.text_device_info);
        mGroupOwner = (TextView) view.findViewById(R.id.text_group_owner);
        mGroupIp = (TextView) view.findViewById(R.id.text_group_ip);
        mStatus = (TextView) view.findViewById(R.id.text_status);

        mBtnConnect = view.findViewById(R.id.btn_connect);
        mBtnConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mDialog != null && mDialog.isShowing()) {
                    mDialog.dismiss();
                }
                mDialog = ProgressDialog.show(getActivity(), "Press back to cancel",
                        "Connecting to :" + mCurrentDevice.deviceAddress, true, true);
                WifiP2pConfig config = new WifiP2pConfig();
                config.deviceAddress = mCurrentDevice.deviceAddress;
                config.wps.setup = WpsInfo.PBC;
//                config.groupOwnerIntent = 1;
                ((MainActivity) getActivity()).connect(config);
            }
        });
        view.findViewById(R.id.btn_disconnect).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainActivity) getActivity()).disconnect();
            }
        });

        mBrowseFile = view.findViewById(R.id.btn_browse_file);
        mBrowseFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                browseForFile();
            }
        });
        return view;
    }

    private void browseForFile() {
        Intent start = new MaterialFilePicker()
                .withFragment(getActivity())
                .withRequestCode(MainActivity.FILE_REQUEST_ID)
                .withFilter(Pattern.compile(".*txt")) // Filtering files and directories by file name using regexp
                .withFilterDirectories(true) // Set directories filterable (false by default)
                .withHiddenFiles(false) // Show hidden files and folders
                .start();
        startActivityForResult(start, MainActivity.FILE_REQUEST_ID);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.e(MainActivity.TAG, "onActivityResult");
        if (resultCode == Activity.RESULT_OK && requestCode == MainActivity.FILE_REQUEST_ID) {

            String localIP = Utils.getLocalIPAddress();
            if (mCurrentDevice == null) {
                Log.e(MainActivity.TAG, "current device is null,please check one!");
                return;
            }
            Log.i(MainActivity.TAG, "device address:" + mCurrentDevice.deviceAddress);
            String clientIP = Utils.getIPFromMac(mCurrentDevice.deviceAddress.replace("99", "19"));
            Log.i(MainActivity.TAG, "localIp:" + localIP + "   clientIp:" + clientIP);
            String filePath = data.getStringExtra(FilePickerActivity.RESULT_FILE_PATH);
            Log.i(MainActivity.TAG, "file location:" + filePath);
            File targetDir = new File(filePath);
            if (targetDir.isFile()) {
                if (targetDir.canRead()) {
                    Intent serviceIntent = new Intent(getActivity(), TransferService.class);
                    //local device is group owner
                    if (localIP.equals(MainActivity.IP_SERVER)) {
                        serviceIntent.putExtra(TransferService.INTENT_SERVER_IP, localIP);
                    } else {
                        serviceIntent.putExtra(TransferService.INTENT_SERVER_IP, MainActivity.IP_SERVER);
                    }
                    serviceIntent.putExtra(TransferService.INTENT_SERVER_PORT, MainActivity.PORT);
                    serviceIntent.putExtra(TransferService.INTENT_FILE_TO_SEND, filePath);
                    getActivity().startService(serviceIntent);
                }
            }

        }
    }

    @Override
    public void onConnectionInfoAvailable(WifiP2pInfo info) {
        if (mDialog != null && mDialog.isShowing()) {
            mDialog.dismiss();
        }
        this.mCurrentInfo = info;
        this.getView().setVisibility(View.VISIBLE);
        mGroupOwner.setText("Am i group owner? " + (info.isGroupOwner ? "yes" : "no"));
        mGroupIp.setText("Group owner ip : " + info.groupOwnerAddress.getHostAddress());
        mBrowseFile.setVisibility(View.VISIBLE);
        if (!mIsServiceRunning) {
//            mServerIntent = new Intent(getActivity(),ReceiveService.class);
//            mServerIntent.putExtra(ReceiveService.INTENT_RECEIVER_RESULT, mServerResult);
//            startActivity(mServerIntent);
            new ServerAsyncTask(getActivity(), mServerResult).execute();
            mIsServiceRunning = true;
        }
        mBtnConnect.setVisibility(View.GONE);

    }

    ResultReceiver mServerResult = new ResultReceiver(null) {
        @Override
        protected void onReceiveResult(final int resultCode, final Bundle resultData) {

            if (resultCode == MainActivity.PORT) {
                if (resultData == null) {
                    mIsServiceRunning = false;
                }
            }
        }
    };

    public void showDetail(WifiP2pDevice device) {
        mCurrentDevice = device;
        this.getView().setVisibility(View.VISIBLE);
        mDeviceAddress.setText(device.deviceAddress);
        mDeviceInfo.setText(device.toString());
    }

    public void resetViews() {
        mBtnConnect.setVisibility(View.VISIBLE);
        mBrowseFile.setVisibility(View.GONE);
        mGroupOwner.setText("");
        mGroupIp.setText("");
        mDeviceInfo.setText("");
        mDeviceAddress.setText("");
        this.getView().setVisibility(View.GONE);
    }
}
