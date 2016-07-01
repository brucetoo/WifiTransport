package com.brucetoo.wifitransport;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.Channel;
import android.util.Log;

/**
 * Created by Bruce Too
 * On 6/29/16.
 * At 13:43
 */
public class WiFiP2PBroadcastReceiver extends BroadcastReceiver {

    private static final String TAG = WiFiP2PBroadcastReceiver.class.getSimpleName();
    private WifiP2pManager mManager;
    private Channel mChannel;
    private MainActivity mActivity;

    public WiFiP2PBroadcastReceiver(WifiP2pManager manager, Channel channel, MainActivity activity) {
        super();
        this.mManager = manager;
        this.mChannel = channel;
        this.mActivity = activity;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {
            Log.i(TAG, "p2p state changed!");
            // Check to see if Wi-Fi is enabled and notify appropriate activity
            int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
            if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {
                mActivity.setIsWifiStatusReady(true);
            } else {
                mActivity.setIsWifiStatusReady(false);
                mActivity.resetData();
            }

        } else if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {
            Log.i(TAG, "p2p peers changed!");
            //notify DeviceListFragment to refresh data
            mManager.requestPeers(mChannel, mActivity.findDeviceListFragment());

        } else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {
            //when the state of the device's Wi-Fi connection changes
            Log.i(TAG, "p2p connection changed!");
            NetworkInfo networkState = intent.getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);
            if (networkState.isConnected()) {
                mManager.requestConnectionInfo(mChannel, mActivity.findDeviceDetailFragment());
            }

        } else if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)) {
            Log.i(TAG, "p2p device changed!");
            // when a device's details have changed, such as the device's name.
            mActivity.findDeviceListFragment().updateDeviceInfo((WifiP2pDevice) intent.getParcelableExtra(
                    WifiP2pManager.EXTRA_WIFI_P2P_DEVICE));
        }
    }

}