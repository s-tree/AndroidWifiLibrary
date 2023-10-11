package com.jingxi.smartlife.wifi;

import android.net.wifi.ScanResult;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.jingxi.smartlife.wifi.library.IWifiUtil;
import com.jingxi.smartlife.wifi.library.WifiUtils;

import java.util.ArrayList;
import java.util.List;

public class WifiListAdapter extends RecyclerView.Adapter<WifiListAdapter.WifiListHolder> {
    public View.OnClickListener onClickListener;
    public List<ScanResult> scanResultList = new ArrayList<>();

    public WifiListAdapter(View.OnClickListener onClickListener, List<ScanResult> scanResultList) {
        this.onClickListener = onClickListener;
        this.scanResultList = scanResultList;
    }

    public void updateData(List<ScanResult> scanResults){
        this.scanResultList = scanResults;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public WifiListHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        return new WifiListHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.layout_wifi_item,viewGroup,false),onClickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull WifiListHolder wifiListHolder, int i) {
        ScanResult scanResult = scanResultList.get(i);
        wifiListHolder.textView.setTag(scanResult);
        wifiListHolder.textView.setText(scanResult.SSID);

        int connectStatus = IWifiUtil.getConnectState(scanResult.SSID);
        if(connectStatus == WifiUtils.CONNECT_STATE_NONE){
            wifiListHolder.wifiState.setText("");
        }else if(connectStatus == WifiUtils.CONNECT_STATE_CONNECING){
            wifiListHolder.wifiState.setText("连接中");
        }else {
            wifiListHolder.wifiState.setText("已连接");
        }
    }

    @Override
    public int getItemCount() {
        return scanResultList.size();
    }

    public static class WifiListHolder extends RecyclerView.ViewHolder{
        public TextView textView;
        public TextView wifiState;

        public WifiListHolder(@NonNull View itemView, View.OnClickListener onClickListener) {
            super(itemView);
            textView = itemView.findViewById(R.id.wifiItem);
            textView.setOnClickListener(onClickListener);
            wifiState = itemView.findViewById(R.id.wifiState);
        }
    }
}
