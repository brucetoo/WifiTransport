package com.brucetoo.wifitransport;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.joanzapata.android.BaseAdapterHelper;
import com.joanzapata.android.QuickAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Bruce Too
 * On 6/30/16.
 * At 11:37
 */
public class DeviceListFragment extends Fragment implements WifiP2pManager.PeerListListener {

    private ListView mPeerList;
    private View mEmptyView;
    private TextView mMyName;
    private TextView mMyStatus;
    private QuickAdapter<WifiP2pDevice> mPeersAdapter;
    private ProgressDialog progressDialog;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_device_list, container, false);
        mPeerList = (ListView) view.findViewById(R.id.list_devices);
        mEmptyView = view.findViewById(R.id.list_empty);
        mMyName = (TextView) view.findViewById(R.id.text_name);
        mMyStatus = (TextView) view.findViewById(R.id.text_status);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initPeerLists();
    }


    private void initPeerLists() {

        mPeersAdapter = new QuickAdapter<WifiP2pDevice>(getActivity(), R.layout.list_item) {
            @Override
            protected void convert(BaseAdapterHelper helper, WifiP2pDevice item) {
                helper.setText(R.id.device_name, item.deviceName);
                helper.setText(R.id.device_details, getDeviceStatus(item.status));
            }
        };

        mPeerList.setAdapter(mPeersAdapter);

        mPeerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                WifiP2pDevice device = mPeersAdapter.getItem(position);
                if (device != null) {
                    //show detail
                    ((MainActivity) getActivity()).showDetails(device);
                }
            }
        });
    }

    private static String getDeviceStatus(int deviceStatus) {

        Log.d(MainActivity.TAG, "Peer status :" + deviceStatus);
        switch (deviceStatus) {
            case WifiP2pDevice.AVAILABLE:
                return "Available";
            case WifiP2pDevice.INVITED:
                return "Invited";
            case WifiP2pDevice.CONNECTED:
                return "Connected";
            case WifiP2pDevice.FAILED:
                return "Failed";
            case WifiP2pDevice.UNAVAILABLE:
                return "Unavailable";
            default:
                return "Unknown";

        }
    }

    public void updateDeviceInfo(WifiP2pDevice device) {
//        this.device = device;
        mMyName.setText(device.deviceName);
        mMyStatus.setText(getDeviceStatus(device.status));
    }

    public void startDiscovery() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
        progressDialog = ProgressDialog.show(getActivity(), "Press back to cancel", "finding peers", true,
                true, null);
    }

    public void clearPeers() {
        mPeersAdapter.clear();
    }

    /**
     * This will be called when {@link WifiP2pManager#WIFI_P2P_PEERS_CHANGED_ACTION}
     * happened in {@link WiFiP2PBroadcastReceiver#onReceive(Context, Intent)}
     */
    @Override
    public void onPeersAvailable(WifiP2pDeviceList peers) {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
        List<WifiP2pDevice> list = new ArrayList<>();
        for (WifiP2pDevice device : peers.getDeviceList()) {
            list.add(device);
        }
        mPeersAdapter.replaceAll(list);
        if (peers.getDeviceList().size() == 0) {
            mEmptyView.setVisibility(View.VISIBLE);
        } else {
            mEmptyView.setVisibility(View.GONE);
        }
    }

}
