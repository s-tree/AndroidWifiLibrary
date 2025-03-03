package com.jingxi.smartlife.wifi.library;

import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.text.TextUtils;
import android.util.Log;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

public class WifiStateUtil {

    protected static boolean isWrongPass(WifiManager wifiManager,String ssid,boolean defaultValue){
        WifiConfiguration wifiConfiguration = isExsits(wifiManager,ssid);
        if(wifiConfiguration == null){
            return defaultValue;
        }
        try{
            Method getNetworkSelectionStatus = WifiConfiguration.class.getDeclaredMethod("getNetworkSelectionStatus");
            getNetworkSelectionStatus.setAccessible(true);

            Object object = getNetworkSelectionStatus.invoke(wifiConfiguration);
            if(object == null){
                return defaultValue;
            }

            Method isNetworkEnabled = object.getClass().getDeclaredMethod("isNetworkEnabled");
            isNetworkEnabled.setAccessible(true);
            boolean result = (boolean) isNetworkEnabled.invoke(object);
            if(result){
                return defaultValue;
            }

            Method getNetworkSelectionDisableReason = object.getClass().getDeclaredMethod("getNetworkSelectionDisableReason");
            getNetworkSelectionDisableReason.setAccessible(true);
            int reason = (int) getNetworkSelectionDisableReason.invoke(object);

            int wrongTag = 12;
            Class networkSelectionStatus = Class.forName("android.net.wifi.WifiConfiguration$NetworkSelectionStatus");
            if(networkSelectionStatus != null){
                Field field = networkSelectionStatus.getDeclaredField("DISABLED_BY_WRONG_PASSWORD");
                if(field != null){
                    field.setAccessible(true);
                    wrongTag = (int) field.get(null);
                }
            }

            return reason == wrongTag;
        }catch (Exception e){
            e.printStackTrace();
        }
        return defaultValue;
    }

    /**
     * 查看以前是否也配置过这个网络
     */
    protected static WifiConfiguration isExsits(WifiManager wifiManager,String SSID) {
        List<WifiConfiguration> existingConfigs = wifiManager.getConfiguredNetworks();
        if (existingConfigs != null) {
            for (WifiConfiguration existingConfig : existingConfigs) {
                if (TextUtils.equals(SSID.replaceAll("\"", ""),
                        existingConfig.SSID.replaceAll("\"", ""))) {
                    return existingConfig;
                }
            }
        }
        return null;
    }


    /**
     * remove配置过的网络，无法移除非本app配置的网络
     */
    protected static void removeConfiguration(WifiManager wifiManager,String ssid) {
        List<WifiConfiguration> configurations = wifiManager.getConfiguredNetworks();
        for(WifiConfiguration configuration : configurations){
            if(TextUtils.equals(configuration.SSID,"\"" + ssid + "\"")){
                WifiStateUtil.forgetNetwork(wifiManager,configuration.networkId);
                wifiManager.saveConfiguration();
            }
        }
    }
    /**
     * remove配置过的网络
     */
    protected static boolean removeNet(WifiManager wifiManager,String ssid) {
        if (ssid != null && !TextUtils.isEmpty(ssid)) {
            WifiConfiguration wifiConfiguration = isExsits(wifiManager,ssid);
            if (wifiConfiguration != null) {
                Log.e("wifiState", wifiConfiguration.networkId + "");
                return forgetNetwork(wifiManager, wifiConfiguration.networkId);
            }
        }
        return false;
    }


    /**
     * 忘记网络
     */

    protected static boolean forgetNetwork(WifiManager manager, int networkId) {
        if (manager == null) {
            return false;
        }
        try {
            Method forget = manager.getClass().getDeclaredMethod("forget",
                    int.class, Class.forName("android.net.wifi.WifiManager$ActionListener"));
            if (forget != null) {
                forget.setAccessible(true);
                forget.invoke(manager, networkId, null);
                manager.saveConfiguration();
            }
            Log.e("wifiState", "success");
            return true;
        } catch (Exception e) {
            Log.e("wifiState", e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}
