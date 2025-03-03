package com.jingxi.smartlife.wifi.library;

import android.app.Application;
import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.List;

public class WifiUtils {
    private static final String TAG = "WifiUtils";
    private static WifiManager wifiManager;
    private static Context context;

    public static final int CONNECT_STATE_NONE = 0;
    public static final int CONNECT_STATE_CONNECING = 1;
    public static final int CONNECT_STATE_CONNECTED = 2;

    public static final int REASON_WRONG_PASS = 0;

    protected static final void init(Application application){
        if(wifiManager != null){
            return;
        }
        wifiManager = (WifiManager) application.getSystemService(Context.WIFI_SERVICE);
        context = application;
        WifiReceiverUtil.registerReceiver(context);
    }

    protected static List<OnWifiChanged> onWifiChangedList = new ArrayList<>();

    protected static void addOnWifiChanged(OnWifiChanged onWifiChanged) {
        onWifiChangedList.add(onWifiChanged);
    }

    public static void removeOnWifiChanged(WifiUtils.OnWifiChanged onWifiChanged) {
        onWifiChangedList.remove(onWifiChanged);
    }

    protected static void setWifiEnable(boolean isEnable){
        wifiManager.setWifiEnabled(isEnable);
    }

    protected static boolean isWifiEnabled(){
        return wifiManager.isWifiEnabled();
    }

    protected static void startScan(){
        if(onWifiChangedList == null
                || onWifiChangedList.isEmpty()){
            return;
        }
        WifiScanUtil.startScan(wifiManager);
    }

    protected static void requestWifiManagerScan(){
        if(wifiManager != null){
            wifiManager.startScan();
        }
    }

    protected static void startScanOnce(){
        if(onWifiChangedList == null
                || onWifiChangedList.isEmpty()){
            return;
        }
        WifiScanUtil.startScanOnce(wifiManager);
    }

    protected static void cancelScan(){
        WifiScanUtil.cancelScan();
    }

    protected static int getConnectState(String ssid){
        WifiInfo wifiInfo = getCurrentWifi();
        if (TextUtils.equals(wifiInfo.getSSID().replaceAll("\"",""),ssid)) {
            return getConnectState(wifiInfo.getSupplicantState());
        }
        return 0;
    }

    protected static WifiInfo getCurrentWifi() {
        return wifiManager.getConnectionInfo();
    }

    /**
     * 0 未连接  1连接中 2，已连接
     */
    protected static int getConnectState(SupplicantState state) {
        int connectState = CONNECT_STATE_NONE;
        if(state == SupplicantState.DISCONNECTED){
            connectState = CONNECT_STATE_NONE;
        }
        else if(state == SupplicantState.COMPLETED){
            connectState = CONNECT_STATE_CONNECTED;
        }else{
            connectState = CONNECT_STATE_CONNECING;
        }
        return connectState;
    }

    protected static void startConnect(String ssid,String bssid,String pass){
        WifiConnectionUtil.connectNewWifi(wifiManager,ssid,bssid,pass);
    }

    protected static void startConnectExist(String ssid){
        WifiConnectionUtil.connectExistWifi(wifiManager,ssid);
    }

    protected static boolean isWifiExist(String ssid){
        return getConfiguration(ssid) != null;
    }

    protected static boolean isWifiWPAWEP(ScanResult scanResult){
        if(scanResult == null || TextUtils.isEmpty(scanResult.capabilities)){
            return false;
        }
        return  scanResult.capabilities.toUpperCase().contains("WPA")
            || scanResult.capabilities.toUpperCase().contains("WEP");
    }

    protected static WifiConfiguration getConfiguration(String ssid){
        List<WifiConfiguration> existingConfigs = wifiManager.getConfiguredNetworks();
        if (existingConfigs != null) {
            for (WifiConfiguration existingConfig : existingConfigs) {
                if (TextUtils.equals(ssid.replaceAll("\"", ""),
                        existingConfig.SSID.replaceAll("\"", ""))) {
                    return existingConfig;
                }
            }
        }
        return null;
    }
    protected static void forget(String ssid){
        WifiConfiguration configuration = getConfiguration(ssid);
        if(configuration == null){
            return;
        }
        WifiStateUtil.forgetNetwork(wifiManager,configuration.networkId);
    }

    public static boolean isWrongPass(String ssid,boolean defaultValue){
        if(wifiManager == null){
            return false;
        }
        return WifiStateUtil.isWrongPass(wifiManager,ssid,defaultValue);
    }

    public interface OnWifiChanged{

        void onEnabled(boolean isEnabled);

        void onConnectStateChanged(int state,int reason);

        void onScanResult(List<ScanResult> scanResults,int step,boolean isLast);
    }
}
