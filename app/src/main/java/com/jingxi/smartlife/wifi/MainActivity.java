package com.jingxi.smartlife.wifi;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;

import com.jingxi.smartlife.wifi.library.IWifiUtil;
import com.jingxi.smartlife.wifi.library.WifiUtils;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements CompoundButton.OnCheckedChangeListener, WifiUtils.OnWifiChanged, View.OnClickListener {
    Switch wifiSwitch;
    RecyclerView recyclerView;
    WifiListAdapter adapter;
    LinearLayoutManager linearLayoutManager;
    List<ScanResult> scanResults;
    WifiAutoConnectManager wifiAutoConnectManager;
    Toast toast;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        IWifiUtil.init(getApplication());
        IWifiUtil.addOnWifiChanged(this);

        wifiAutoConnectManager = new WifiAutoConnectManager((WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE));
        toast = Toast.makeText(this, "", Toast.LENGTH_SHORT);

        wifiSwitch = findViewById(R.id.wifiSwitch);
        wifiSwitch.setChecked(IWifiUtil.isWifiEnabled());
        wifiSwitch.setOnCheckedChangeListener(this);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(adapter = new WifiListAdapter(this, new ArrayList<ScanResult>()));

        IWifiUtil.startScan();
    }

    private synchronized void setAdapter(List<ScanResult> scanResults) {
        this.scanResults = scanResults;
        adapter.updateData(scanResults);
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        IWifiUtil.setWifiEnable(isChecked);
    }

    @Override
    public void onEnabled(boolean isEnabled) {
        if (isEnabled) {
            IWifiUtil.startScan();
        }
    }

    @Override
    public void onConnectStateChanged(int state, int reason) {
        if (adapter == null) {
            return;
        }
        if (state != WifiUtils.CONNECT_STATE_NONE) {
            adapter.notifyDataSetChanged();
            linearLayoutManager.scrollToPosition(0);
        }
        if (reason == WifiUtils.REASON_WRONG_PASS) {
            toast.setText("密码错误");
            toast.show();
            ScanResult scanResult = (ScanResult) editText.getTag();
            if (scanResult == null) {
                return;
            }
            IWifiUtil.forget(scanResult.SSID);
        }
    }

    @Override
    public void onScanResult(List<ScanResult> scanResults, int step, boolean isLast) {
        if (scanResults == null) {
            return;
        }
        setAdapter(scanResults);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.wifiItem) {
            ScanResult scanResult = (ScanResult) v.getTag();
            if (editText != null) {
                editText.setTag(scanResult);
            }
//            if (IWifiUtil.isWifiExist(scanResult.SSID)) {
//                IWifiUtil.startConnectExist(scanResult.SSID);
////                wifiAutoConnectManager.connect(scanResult.SSID,"", WifiAutoConnectManager.WifiCipherType.WIFICIPHER_WPA);
//            } else
                if (scanResult.capabilities.toUpperCase().contains("WPA")
                    || scanResult.capabilities.toUpperCase().contains("WEP")) {
                showDialog(scanResult);
            } else {
                IWifiUtil.startConnect(scanResult.SSID, scanResult.BSSID,"");
//                wifiAutoConnectManager.connect(scanResult.SSID,"", WifiAutoConnectManager.WifiCipherType.WIFICIPHER_NOPASS);
            }
        }
    }

    private AlertDialog dialog;
    private EditText editText;

    private void showDialog(ScanResult scanResult) {
        if (dialog == null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setView(editText = new EditText(this));
            builder.setNegativeButton("连接", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    ScanResult tag = (ScanResult) editText.getTag();
                    IWifiUtil.startConnect(tag.SSID, editText.getText().toString());
//                    wifiAutoConnectManager.connect(tag.SSID,editText.getText().toString(), WifiAutoConnectManager.WifiCipherType.WIFICIPHER_WPA);
                }
            });
            builder.setPositiveButton("取消", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                }
            });
            dialog = builder.create();
        }
        editText.setTag(scanResult);
        if (dialog.isShowing()) {
            return;
        }
        dialog.show();
    }
}
