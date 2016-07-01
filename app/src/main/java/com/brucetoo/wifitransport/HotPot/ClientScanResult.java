package com.brucetoo.wifitransport.HotPot;

/**
 * Created by Bruce Too
 * On 7/1/16.
 * At 11:36
 */
public class ClientScanResult {

    private String IpAddr;
    private String HWAddr;
    private String Device;
    private boolean isReachable;

    public ClientScanResult(String ipAddr, String hWAddr, String device, boolean isReachable) {
        super();
        this.IpAddr = ipAddr;
        this.HWAddr = hWAddr;
        this.Device = device;
        this.isReachable = isReachable;
    }

    public String getIpAddr() {
        return IpAddr;
    }

    public void setIpAddr(String ipAddr) {
        IpAddr = ipAddr;
    }


    public String getHWAddr() {
        return HWAddr;
    }

    public void setHWAddr(String hWAddr) {
        HWAddr = hWAddr;
    }


    public String getDevice() {
        return Device;
    }

    public void setDevice(String device) {
        Device = device;
    }


    public boolean isReachable() {
        return isReachable;
    }

    public void setReachable(boolean isReachable) {
        this.isReachable = isReachable;
    }

    @Override
    public String toString() {
        return "ipAddr:" + IpAddr + ",HWAddr:" + HWAddr + ",Device:" + Device + ",isReachable:" + isReachable;
    }
}