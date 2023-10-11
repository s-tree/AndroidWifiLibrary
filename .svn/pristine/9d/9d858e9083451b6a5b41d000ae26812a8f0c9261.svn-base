package com.jingxi.smartlife.wifi.library;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiManager;
import android.text.TextUtils;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class WifiReceiverUtil {
    public static final String TAG = "WifiReceiverUtil";

    private static Context context;

    protected static void registerReceiver(Context context){
        if(context == null){
            return;
        }
        IntentFilter filter = new IntentFilter(WifiManager.WIFI_STATE_CHANGED_ACTION);
        filter.addAction(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION);
        context.registerReceiver(wifiReceiver,filter);
    }

    protected static void unRegisterReceiver(){
        if(context == null){
            return;
        }
        context.unregisterReceiver(wifiReceiver);
    }


    private static BroadcastReceiver wifiReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(TextUtils.equals(intent.getAction(),WifiManager.WIFI_STATE_CHANGED_ACTION)){
                onWifiStateChanged(intent);
            }
            else if(TextUtils.equals(intent.getAction(),WifiManager.SUPPLICANT_STATE_CHANGED_ACTION)){
                onSupplicantStateChanged(intent);
            }
        }
    };

    private static void onWifiStateChanged(Intent intent){
        if(WifiUtils.onWifiChangedList == null
                || WifiUtils.onWifiChangedList.isEmpty()){
            return;
        }
        boolean isEnabled = false;
        int wifiState = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, 0);
        if(wifiState == WifiManager.WIFI_STATE_ENABLED){
            isEnabled = true;
        }
        else if(wifiState == WifiManager.WIFI_STATE_DISABLED){
            isEnabled = false;
        }
        else if(wifiState == WifiManager.WIFI_STATE_ENABLING){
            return;
        }
        else if(wifiState == WifiManager.WIFI_STATE_DISABLING){
            return;
        }
        List<WifiUtils.OnWifiChanged> temp = new ArrayList<>(WifiUtils.onWifiChangedList);
        for(WifiUtils.OnWifiChanged onWifiChanged : temp){
            onWifiChanged.onEnabled(isEnabled);
        }
    }

    private static void onSupplicantStateChanged(Intent intent){
        if(WifiUtils.onWifiChangedList == null
                || WifiUtils.onWifiChangedList.isEmpty()){
            return;
        }
        SupplicantState state = intent.getParcelableExtra(WifiManager.EXTRA_NEW_STATE);
        int supplicationErr = intent.getIntExtra(WifiManager.EXTRA_SUPPLICANT_ERROR,-1);
        /**
         * 经过测试，断开连接时，在密码错误的情况的 reason = 0，其他时候reason = null
         */
        int reason = intent.getIntExtra("supplicantErrorReason",-1);
        Log.w(TAG,"onSupplicantStateChanged state = " + state + " errorCode = " + supplicationErr + " reason = " + reason);
        if(state == SupplicantState.DISCONNECTED
                && reason == 0){
            Log.w(TAG,"密码错误");
        }
        List<WifiUtils.OnWifiChanged> temp = new ArrayList<>(WifiUtils.onWifiChangedList);
        for(WifiUtils.OnWifiChanged onWifiChanged : temp){
            onWifiChanged.onConnectStateChanged(WifiUtils.getConnectState(state),reason);
        }
    }
}
