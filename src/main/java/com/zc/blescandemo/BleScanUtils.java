package com.zc.blescandemo;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;


@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
public class BleScanUtils {
    private static final String TAG = BleScanUtils.class.getSimpleName();
    private static final long DELEYTIME = 10 * 1000;
    private long scanPeriod = 3000;
    private long beforeTime;
    private BluetoothAdapter mBluetoothAdapter;
    private ArrayList<Beacon> beacons = new ArrayList<>();
    private HashMap<Beacon, Long> beaconsWithTime = new HashMap<>();
    private String uuid;
    private Integer major;
    private Integer minor;
    private ScanListener scanListener;
    private boolean isStartScan;
    private Context context;

    private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
            if (!isStartScan) {
                return;
            }
            Beacon beacon = Beacon.beaconFromLeScan(device, rssi, scanRecord);
            long nowTime = System.currentTimeMillis();
            if (nowTime - beforeTime > scanPeriod) {
                beforeTime = nowTime;
                if (scanListener != null) {
                    scanListener.onScanListenre(beacons);
                }
                beacons.clear();
                Iterator<Map.Entry<Beacon, Long>> it = beaconsWithTime.entrySet().iterator();
                while (it.hasNext()) {
                    Map.Entry<Beacon, Long> entry = it.next();
                    if (nowTime - entry.getValue() < DELEYTIME) {
                        beacons.add(entry.getKey());
                    } else {
                        it.remove();
                        //离开的beacon范围 可以在这里发送离开通知
                        if (scanListener != null) {
                            scanListener.onExit(entry.getKey());
                        }
                    }
                }
                return;
            }
            if (beacon == null) {
                return;
            }
            if (!TextUtils.isEmpty(uuid) && !uuid.toUpperCase().equals(beacon.getProximityUUID().toUpperCase())) {
                return;
            }
            if (major != null && major != -1 && major != beacon.getMajor()) {
                return;
            }
            if (minor != null && minor != -1 && minor != beacon.getMinor()) {
                return;
            }
            if (!beacons.contains(beacon)) {//之前不在范围内 现在扫描到进入范围内
                beacons.add(beacon);
                //进入beacon范围 可以在这里发送进入通知
                if (scanListener != null) {
                    scanListener.onEnter(beacon);
                }
            } else {
                beacons.remove(beacon);
                beacons.add(beacon);
            }
            beaconsWithTime.put(beacon, System.currentTimeMillis());
        }
    };

    public BleScanUtils(Context context) {
        final BluetoothManager bluetoothManager = (BluetoothManager) context.getSystemService(
                Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();
        this.context = context;
    }

    public boolean startBleScan() {
        if (mBluetoothAdapter == null) {
            return false;
        }
        if (!mBluetoothAdapter.isEnabled()) {
            return false;
        }
        isStartScan = true;
        return mBluetoothAdapter.startLeScan(mLeScanCallback);
    }

    /**
     * 停止蓝牙Ble扫描
     */
    public void stopBleScan() {
        isStartScan = false;
        if (mBluetoothAdapter == null) {
            return;
        }
        if (mLeScanCallback == null) {
            return;
        }
        mBluetoothAdapter.stopLeScan(mLeScanCallback);
    }

    public void setScanPeriod(long scanPeriod) {
        this.scanPeriod = scanPeriod;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public void setMajor(Integer major) {
        this.major = major;
    }

    public void setMinor(Integer minor) {
        this.minor = minor;
    }

    public void setScanListener(ScanListener scanListener) {
        this.scanListener = scanListener;
    }

    public interface ScanListener {
        void onScanListenre(ArrayList<Beacon> beacons);

        void onExit(Beacon beacon);

        void onEnter(Beacon beacon);
    }

    public boolean hasBluetooth() {
        return this.context.getPackageManager().hasSystemFeature(
                PackageManager.FEATURE_BLUETOOTH_LE);
    }

    public boolean checkBlueTooth() {
        if (context.getPackageManager().hasSystemFeature(
                PackageManager.FEATURE_BLUETOOTH_LE) && BluetoothAdapter.getDefaultAdapter().isEnabled()) {
            return true;
        }
        return false;
    }
}
