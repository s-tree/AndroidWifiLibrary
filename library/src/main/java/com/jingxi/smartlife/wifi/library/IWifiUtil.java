package com.jingxi.smartlife.wifi.library;

import android.app.Application;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;

public class IWifiUtil {

    public static void init(Application application){
        WifiUtils.init(application);
    }

    public static void addOnWifiChanged(WifiUtils.OnWifiChanged onWifiChanged) {
        WifiUtils.addOnWifiChanged(onWifiChanged);
    }

    public static void removeOnWifiChanged(WifiUtils.OnWifiChanged onWifiChanged){
        WifiUtils.removeOnWifiChanged(onWifiChanged);
    }

    public static void setWifiEnable(boolean isEnable){
        WifiUtils.setWifiEnable(isEnable);
    }

    public static boolean isWifiEnabled(){
        return WifiUtils.isWifiEnabled();
    }

    public static void startScan(){
        WifiUtils.startScan();
    }

    public static void startScanOnce(){
        WifiUtils.startScanOnce();
    }

    public static void cancelScan(){
        WifiUtils.cancelScan();
    }

    public static int getConnectState(String ssid){
        return WifiUtils.getConnectState(ssid);
    }

    public static WifiInfo getCurrentWifi() {
        return WifiUtils.getCurrentWifi();
    }

    public static void startConnect(String ssid,String pass){
        WifiUtils.startConnect(ssid,"",pass);
    }

    public static void startConnect(String ssid,String bssid,String pass){
        WifiUtils.startConnect(ssid,bssid,pass);
    }

    public static void startConnectExist(String ssid){
        WifiUtils.startConnectExist(ssid);
    }

    public static boolean isWifiExist(String ssid){
        return WifiUtils.isWifiExist(ssid);
    }

    public static boolean isWifiWPAWEP(ScanResult scanResult){
        return WifiUtils.isWifiWPAWEP(scanResult);
    }

    public static void forget(String ssid){
        WifiUtils.forget(ssid);
    }

    public static void requestWifiManagerScan(){
        WifiUtils.requestWifiManagerScan();
    }

    public static boolean isWrongPass(String ssid,boolean defaultValue){
        return WifiUtils.isWrongPass(ssid,defaultValue);
    }
}
