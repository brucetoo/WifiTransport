package com.brucetoo.wifitransport.HotPot;

import android.content.Context;

import java.util.ArrayList;

/**
 * Created by Bruce Too
 * On 7/1/16.
 * At 11:37
 * Scan the file "/proc/net/arp" in android,
 * and this is callback listener
 *
 * @see WifiManagerUtils#getWifiApClientList(Context, boolean, int, ScanFinishListener)
 */
public interface ScanFinishListener {

    /**
     * Be called when scan file finished,
     *
     * @param clients which connected to Hotspot
     */
    void onFinishScan(ArrayList<ClientScanResult> clients);
}
