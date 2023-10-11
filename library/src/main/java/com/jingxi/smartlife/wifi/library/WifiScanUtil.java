package com.jingxi.smartlife.wifi.library;

import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.SystemClock;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class WifiScanUtil {

    private static WifiManager mWifiManager;
    private static ScheduledExecutorService executor;

    private static int start = 0;
    private static int maxStep = 10;

    private static Handler handler = new Handler();

    private static ScheduledExecutorService getExecutors(){
        if(executor == null){
            try {
                executor = Executors.newSingleThreadScheduledExecutor();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return executor;
    }

    private static Runnable scanRunnable = new Runnable() {
        @Override
        public void run() {
            if(mWifiManager == null){
                return;
            }
            while (start != -1 && start < maxStep){
                List<ScanResult> scanResults = mWifiManager.getScanResults();
                mWifiManager.startScan();
                resultRunnable.scanResults = scanResults;
                handler.post(resultRunnable);
                start++;
                SystemClock.sleep(1000);
            }
        }
    };

    private static abstract class ResultRunnable implements Runnable {
        public List<ScanResult> scanResults;
    };

    private static ResultRunnable resultRunnable = new ResultRunnable() {
        @Override
        public void run() {
            if(scanResults == null){
                return;
            }
            List<WifiUtils.OnWifiChanged> temp = new ArrayList<>(WifiUtils.onWifiChangedList);
            for(WifiUtils.OnWifiChanged onWifiChanged : temp){
                onWifiChanged.onScanResult(scanResults,start,start >= maxStep);
            }
        }
    };

    protected static void startScan(final WifiManager wifiManager) {
        if (WifiUtils.onWifiChangedList == null
                || WifiUtils.onWifiChangedList.isEmpty()) {
            return;
        }
        cancelScan();
        mWifiManager = wifiManager;
        start = 0;
        getExecutors().execute(scanRunnable);
    }

    protected static void startScanOnce(WifiManager wifiManager){
        List<WifiUtils.OnWifiChanged> temp = new ArrayList<>(WifiUtils.onWifiChangedList);
        List<ScanResult> results = wifiManager.getScanResults();
        for(WifiUtils.OnWifiChanged onWifiChanged : temp){
            onWifiChanged.onScanResult(results,0,true);
        }
    }

    protected static void cancelScan() {
        start = -1;
    }
}
