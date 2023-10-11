package com.jingxi.smartlife.wifi.library;

import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.text.TextUtils;

import java.util.List;

public class WifiConnectionUtil {
    public enum WifiCipherType {
        WIFICIPHER_WEP, WIFICIPHER_WPA, WIFICIPHER_NOPASS, WIFICIPHER_INVALID
    }

    protected static void connectNewWifi(WifiManager wifiManager,String ssid,String bssId, String pass){
        if(!wifiManager.isWifiEnabled()){
            return;
        }
        WifiConfiguration configuration = null;
        if(TextUtils.isEmpty(pass)){
            configuration = createWifiInfo(ssid,"",WifiCipherType.WIFICIPHER_NOPASS);
        }else{
            configuration = createWifiInfo(ssid,pass,WifiCipherType.WIFICIPHER_WPA);
        }
        if(!TextUtils.isEmpty(bssId)){
            configuration.BSSID = bssId;
        }
        int networkId = wifiManager.addNetwork(configuration);
        wifiManager.enableNetwork(networkId,true);
    }

    protected static void connectExistWifi(WifiManager wifiManager,String ssid){
        if(!wifiManager.isWifiEnabled()){
            return;
        }
        List<WifiConfiguration> configurations = wifiManager.getConfiguredNetworks();
        WifiConfiguration configuration = null;
        for(WifiConfiguration temp : configurations){
            if(TextUtils.equals(temp.SSID.replaceAll("\\\"",""),ssid)){
                configuration = temp;
            }
        }
        if(configuration == null){
            return;
        }
        wifiManager.enableNetwork(configuration.networkId,true);
    }

    private static WifiConfiguration createWifiInfo(String SSID, String Password, WifiCipherType Type) {
        WifiConfiguration config = new WifiConfiguration();
        config.allowedAuthAlgorithms.clear();
        config.allowedGroupCiphers.clear();
        config.allowedKeyManagement.clear();
        config.allowedPairwiseCiphers.clear();
        config.allowedProtocols.clear();
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q){
            config.allowedGroupManagementCiphers.clear();
            config.allowedSuiteBCiphers.clear();
        }
        config.SSID = "\"" + SSID + "\"";

        if (Type == WifiCipherType.WIFICIPHER_NOPASS){
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.R){
                config.setSecurityParams(WifiConfiguration.SECURITY_TYPE_OPEN);
            }
            else{
                config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
                config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
                config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.SHARED);
            }
        }
        if (Type == WifiCipherType.WIFICIPHER_WEP){
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
        if (Type == WifiCipherType.WIFICIPHER_WPA) {
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
}
