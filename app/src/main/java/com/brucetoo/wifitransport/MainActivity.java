package com.brucetoo.wifitransport;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.Channel;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;


/**
 * Created by Bruce Too
 * On 6/29/16.
 * At 13:33
 */
public class MainActivity extends FragmentActivity implements DeviceActionListener {

    public static final String TAG = "wifi_transfer";
    public static final String IP_SERVER = "192.168.49.1";
    public static final int PORT = 1234;
    public static final String LOCATION = Environment.getExternalStorageDirectory().getAbsolutePath();
    public static final int FILE_REQUEST_ID = 1000;

    private WifiP2pManager mWifiP2PManager;
    private Channel mChannel;
    private BroadcastReceiver mWifiP2PClientReceiver;

    private boolean mIsTransferActive;
    private boolean mIsWifiStateReady;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mWifiP2PManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        mChannel = mWifiP2PManager.initialize(this, getMainLooper(), null);
        mWifiP2PClientReceiver = new WiFiP2PBroadcastReceiver(mWifiP2PManager, mChannel, this);


        IntentFilter clientIntentFilter = new IntentFilter();
        clientIntentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        clientIntentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        clientIntentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        clientIntentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);

        registerReceiver(mWifiP2PClientReceiver, clientIntentFilter);
    }

    /**
     * Search for peers
     * We can receive {@link WifiP2pManager#WIFI_P2P_PEERS_CHANGED_ACTION} action
     * and use {@link WifiP2pManager#requestPeers} to get peers list
     *
     * @param view view be clicked
     * @see {@link WiFiP2PBroadcastReceiver#onReceive(Context, Intent)}
     */
    public void searchForPeers(View view) {
        if (mIsWifiStateReady) {
            findDeviceListFragment().startDiscovery();
            mWifiP2PManager.discoverPeers(mChannel, new WifiP2pManager.ActionListener() {

                @Override
                public void onSuccess() {
                    Toast.makeText(MainActivity.this, "Discovery Initiated",
                            Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onFailure(int reasonCode) {
                    Toast.makeText(MainActivity.this, "Discovery Failed : " + reasonCode,
                            Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Toast.makeText(MainActivity.this, "This device is not support", Toast.LENGTH_SHORT).show();
        }
    }

//    public void browseForFile() {
//
//        new MaterialFilePicker()
//                .withActivity(this)
//                .withRequestCode(FILE_REQUEST_ID)
//                .withFilter(Pattern.compile(".*")) // Filtering files and directories by file name using regexp
//                .withFilterDirectories(true) // Set directories filterable (false by default)
//                .withHiddenFiles(false) // Show hidden files and folders
//                .start();
//        //or
////        Intent intent = new Intent(this, FilePickerActivity.class);
////        intent.putExtra(FilePickerActivity.ARG_FILE_FILTER, Pattern.compile(".*\\.txt$"));
////        intent.putExtra(FilePickerActivity.ARG_DIRECTORIES_FILTER, true);
////        intent.putExtra(FilePickerActivity.ARG_SHOW_HIDDEN, true);
////        startActivityForResult(intent, 1);
//    }

    public void setIsWifiStatusReady(boolean ready) {
        mIsWifiStateReady = ready;
    }

    public DeviceListFragment findDeviceListFragment() {
        return ((DeviceListFragment) getSupportFragmentManager().findFragmentById(R.id.frag_list));
    }

    public DeviceDetailFragment findDeviceDetailFragment() {
        return ((DeviceDetailFragment) getSupportFragmentManager().findFragmentById(R.id.frag_detail));
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(mWifiP2PClientReceiver);
        super.onDestroy();
    }


    public void resetData() {
        findDeviceListFragment().clearPeers();
        findDeviceDetailFragment().resetViews();
    }


    @Override
    public void showDetails(WifiP2pDevice device) {
        findDeviceDetailFragment().showDetail(device);
    }

    @Override
    public void cancelDisconnect() {

    }

    @Override
    public void connect(WifiP2pConfig config) {
        mWifiP2PManager.connect(mChannel, config, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                // WiFiDirectBroadcastReceiver will notify us. Ignore for now.
            }

            @Override
            public void onFailure(int reason) {
                Toast.makeText(MainActivity.this, "Connect failed. Retry.",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void disconnect() {
        findDeviceDetailFragment().resetViews();
        mWifiP2PManager.removeGroup(mChannel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                findDeviceDetailFragment().getView().setVisibility(View.GONE);
            }

            @Override
            public void onFailure(int reason) {
                Log.d(TAG, "Disconnect failed. Reason :" + reason);
            }
        });
    }
}
