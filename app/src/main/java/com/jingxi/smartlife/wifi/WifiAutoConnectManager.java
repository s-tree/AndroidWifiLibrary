package com.jingxi.smartlife.wifi;

import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiNetworkSuggestion;
import android.os.Build;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import static android.net.wifi.WifiManager.WIFI_STATE_ENABLED;

/**
 * Created by Administrator on 2016/11/30.
 */

public class WifiAutoConnectManager {

    private static final String TAG = "CHANGE_WIFI";

    WifiManager wifiManager;

    // 定义几种加密方式，一种是WEP，一种是WPA，还有没有密码的情况
    public enum WifiCipherType {
        WIFICIPHER_WEP, WIFICIPHER_WPA, WIFICIPHER_NOPASS, WIFICIPHER_INVALID
    }

    // 构造函数
    public WifiAutoConnectManager(WifiManager wifiManager) {
        this.wifiManager = wifiManager;
    }

    // 提供一个外部接口，传入要连接的无线网
    public void connect(String ssid, String password, WifiCipherType type) {
        new Thread(new ConnectRunnable(ssid, password, type)).start();
    }

    // 查看以前是否也配置过这个网络
    private WifiConfiguration isExsits(String SSID) {
        List<WifiConfiguration> existingConfigs = wifiManager.getConfiguredNetworks();
        if(existingConfigs == null || existingConfigs.isEmpty()){
            return null;
        }
        for (WifiConfiguration existingConfig : existingConfigs) {
            if (existingConfig.SSID.equals("\"" + SSID + "\"")) {
                return existingConfig;
            }
        }
        return null;
    }

    public String getSSID(){
        WifiInfo info = wifiManager.getConnectionInfo();
        return info.getSSID();
    }

    public boolean isWifiCanUsed(){
        WifiInfo info = wifiManager.getConnectionInfo();
        return info.getRssi() > -60;
    }

    public String wifiLevel(){
        WifiInfo info = wifiManager.getConnectionInfo();
        int rssi = info.getRssi();
        if(rssi >= -60){
            return "较好";
        }
        else if(rssi >= -80){
            return "良好";
        }
        else if(rssi >= 100){
            return "一般";
        }
        else{
            return "较差";
        }
    }

    private WifiConfiguration createWifiInfo(String SSID, String Password, WifiCipherType Type) {
        WifiConfiguration config = new WifiConfiguration();
        config.allowedAuthAlgorithms.clear();
        config.allowedGroupCiphers.clear();
        config.allowedKeyManagement.clear();
        config.allowedPairwiseCiphers.clear();
        config.allowedProtocols.clear();
        config.SSID = "\"" + SSID + "\"";

        if (Type == WifiCipherType.WIFICIPHER_NOPASS) // WIFICIPHER_NOPASS
        {
            config.wepKeys[0] = "";
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
            config.wepTxKeyIndex = 0;
        }
        if (Type == WifiCipherType.WIFICIPHER_WEP) // WIFICIPHER_WEP
        {
            config.hiddenSSID = true;
            config.wepKeys[0] = "\"" + Password + "\"";
            config.allowedAuthAlgorithms
                    .set(WifiConfiguration.AuthAlgorithm.SHARED);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
            config.allowedGroupCiphers
                    .set(WifiConfiguration.GroupCipher.WEP104);
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
            config.wepTxKeyIndex = 0;
        }
        if (Type == WifiCipherType.WIFICIPHER_WPA) // WIFICIPHER_WPA
        {
            config.preSharedKey = "\"" + Password + "\"";
            config.hiddenSSID = true;
            config.allowedAuthAlgorithms
                    .set(WifiConfiguration.AuthAlgorithm.OPEN);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
            config.allowedPairwiseCiphers
                    .set(WifiConfiguration.PairwiseCipher.TKIP);
            // config.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
            config.allowedPairwiseCiphers
                    .set(WifiConfiguration.PairwiseCipher.CCMP);
            config.status = WifiConfiguration.Status.ENABLED;
        }

        return config;
    }

    // 打开wifi功能
    private boolean openWifi() {
        boolean bRet = true;
        if (!wifiManager.isWifiEnabled()) {
            bRet = wifiManager.setWifiEnabled(true);
        }
        return bRet;
    }

    private class ConnectRunnable implements Runnable {
        private String ssid;

        private String password;

        private WifiCipherType type;

        public ConnectRunnable(String ssid, String password, WifiCipherType type) {
            this.ssid = ssid;
            this.password = password;
            this.type = type;
        }

        @Override
        public void run() {
            // 打开wifi
            openWifi();
            // 开启wifi功能需要一段时间(我在手机上测试一般需要1-3秒左右)，所以要等到wifi
            // 状态变成WIFI_STATE_ENABLED的时候才能执行下面的语句
            while (wifiManager.getWifiState() == WifiManager.WIFI_STATE_ENABLING) {
                try {
                    // 为了避免程序一直while循环，让它睡个100毫秒检测……
                    Thread.sleep(100);
                } catch (InterruptedException ie) {
                }
            }

            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q){
                wifiNetworkSuggestions(ssid,password);
                return;
            }

            WifiConfiguration wifiConfig = createWifiInfo(ssid, password, type);
            if (wifiConfig == null) {
                Log.d(TAG, "wifiConfig is null!");
                return;
            }
            WifiConfiguration tempConfig = isExsits(ssid);

            if (tempConfig != null) {
//                action1.call(wifiManager.enableNetwork(tempConfig.networkId, true));
                boolean enabled =  wifiManager.enableNetwork(tempConfig.networkId, true);
                return;
            }
            int netID = wifiManager.addNetwork(wifiConfig);
            boolean enabled = wifiManager.enableNetwork(netID, true);
            Log.d(TAG, "enableNetwork status enable=" + enabled);
//            boolean connected = wifiManager.reconnect();
//            Log.d(TAG, "enableNetwork connected=" + connected);
//            action1.call(enabled);
        }
    }

    void wifiNetworkSuggestions(String ssid,String wpaPass){
        List<WifiNetworkSuggestion> suggestions = new ArrayList<>();
        WifiNetworkSuggestion suggestion = new WifiNetworkSuggestion.Builder()
                .setSsid(ssid)
                .setIsEnhancedOpen(false)
                .setWpa2Passphrase(wpaPass)
                .build();
        suggestions.add(suggestion);
        wifiManager.addNetworkSuggestions(suggestions);
    }

    static boolean isHexWepKey(String wepKey) {
        final int len = wepKey.length();

        // WEP-40, WEP-104, and some vendors using 256-bit WEP (WEP-232?)
        if (len != 10 && len != 26 && len != 58) {
            return false;
        }

        return isHex(wepKey);
    }

    private static boolean isHex(String key) {
        for (int i = key.length() - 1; i >= 0; i--) {
            final char c = key.charAt(i);
            if (!(c >= '0' && c <= '9' || c >= 'A' && c <= 'F' || c >= 'a' && c <= 'f')) {
                return false;
            }
        }

        return true;
    }

    public boolean isEnable(){
        return wifiManager.getWifiState() == WIFI_STATE_ENABLED;
    }
}