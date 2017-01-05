package com.zc.blescandemo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();
    private BleScanUtils bleScanUtils;

    private ArrayList<BeaconData> beaconDatas = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final BeaconData beaconData = new BeaconData();
        beaconData.setUuid("B5B182C7-EAB1-4988-AA99-B5C1517008D9");
        beaconData.setMajor("779");
        beaconData.setMinor("6669");
        beaconData.setTriggerDistance("1.8");
        beaconDatas.add(beaconData);
        TriggerManager.getInstance().setBeaconDatas(beaconDatas);

        bleScanUtils = new BleScanUtils(this);
        bleScanUtils.setUuid("B5B182C7-EAB1-4988-AA99-B5C1517008D9");
        bleScanUtils.setMajor(779);
        bleScanUtils.setMinor(6669);
        bleScanUtils.startBleScan();
        bleScanUtils.setScanListener(new BleScanUtils.ScanListener() {
            @Override
            public void onScanListenre(ArrayList<Beacon> beacons) {
                if (beacons.size() > 0){
                    Log.i("TriggerManager", "nScanListenre: beacons.distance = " + beacons.get(0).getDistance());
                    Log.i("TriggerManager", "nScanListenre: beacons.rssi = " + beacons.get(0).getRssi());
                }

                TriggerManager.getInstance().getTriggerBeacon(beacons);
            }

            @Override
            public void onExit(Beacon beacon) {
                Log.i("TriggerManager", "onExit: beacon = " + beacon.getProximityUUID() + "-" + beacon.getMajor() + "-" + beacon.getMinor());
            }

            @Override
            public void onEnter(Beacon beacon) {
                Log.i("TriggerManager", "onEnter: beacon = " + beacon.getProximityUUID() + "-" + beacon.getMajor() + "-" + beacon.getMinor());
            }
        });
    }
}
