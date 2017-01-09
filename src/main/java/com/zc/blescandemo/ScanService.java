package com.zc.blescandemo;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.ArrayList;

public class ScanService extends Service {
    private static final String TAG = ScanService.class.getSimpleName();
    private BleScanUtils bleScanUtils;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "onCreate: ");
        bleScanUtils = new BleScanUtils(this);
        bleScanUtils.setUuid("B5B182C7-EAB1-4988-AA99-B5C1517008D9");
        bleScanUtils.setMajor(779);
        bleScanUtils.setScanListener(new BleScanUtils.ScanListener() {
            @Override
            public void onScanListenre(ArrayList<Beacon> beacons) {
                Log.i(TAG, "onScanListenre: beacons.size = " + beacons.size());
            }

            @Override
            public void onExit(Beacon beacon) {
                Log.i(TAG, "onExit: ");
            }

            @Override
            public void onEnter(Beacon beacon) {
                Log.i(TAG, "onEnter: ");
            }
        });
        bleScanUtils.startBleScan();
    }

    @Override
    public void onDestroy() {
        Log.i(TAG, "onDestroy: ");
        super.onDestroy();
        bleScanUtils.stopBleScan();
    }
}
