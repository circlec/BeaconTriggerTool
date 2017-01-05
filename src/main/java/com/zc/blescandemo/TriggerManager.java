package com.zc.blescandemo;

import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TriggerManager {
    private static final String TAG = TriggerManager.class.getSimpleName();
    private static TriggerManager triggerManager = new TriggerManager();
    //网络配置的触发beacon的数据
    private ArrayList<BeaconData> beaconDatas = new ArrayList<>();
    //触发阈值
    private int triggerThreshold = 3;
    private long triggerDelayTime = 60 * 1000;
    // 用于记录beacon及距离
    private static HashMap<String, ArrayList<Double>> beaconsWithDistance = new HashMap<>();
    // 用于存储beacon及三次有效距离的平均值
    private static HashMap<String, Double> beaconsWithAvgDistance = new HashMap<>();
    // 未扫描到的beacon集合 key--uuid-major-minor值 未扫描到的次数 次数达到n次将存储的通知beacon移除
    private HashMap<String, Integer> noScanBeacons = new HashMap<>();
    // 已经通知过的beacon 离开beacon范围后移除
    private HashMap<String, Long> notifiedBeacons = new HashMap<>();
    // 记录已经通知过的beacon时间 离开后不移除
    private HashMap<String, Long> notifiedBeaconsTimes = new HashMap<>();
    //triggerThreshold次扫描到的所有beacon
    private ArrayList<List<Beacon>> threeScanBeacons = new ArrayList<>();

    private TriggerManager() {
    }

    public static TriggerManager getInstance() {
        return triggerManager;
    }

    public void setBeaconDatas(List<BeaconData> beaconDataList) {
        beaconDatas.clear();
        beaconDatas.addAll(beaconDataList);
    }

    public void setTriggerThreshold(int triggerThreshold) {
        if (triggerThreshold < 1) return;
        this.triggerThreshold = triggerThreshold;
    }

    public void getTriggerBeacon(List<Beacon> beacons) {
        if (beaconDatas.size() == 0) return;
        if (beacons == null || beacons.size() == 0) return;
        setBeaconWithDistance(beacons);
        if (beaconsWithDistance.size() > 0) {
            for (Map.Entry<String, ArrayList<Double>> entry : beaconsWithDistance
                    .entrySet()) {
                Log.i(TAG, "getTriggerBeacon: key = "+entry.getKey());
                Log.i(TAG, "getTriggerBeacon: value = "+entry.getValue());
            }
            Log.i(TAG, "----------------------------------------------");
        }
    }

    private void setBeaconWithDistance(List<Beacon> beacons) {
        for (Beacon mBeacon : beacons) {
            String key = mBeacon.getProximityUUID() + "-" + mBeacon.getMajor() + "-" + mBeacon.getMinor();
            if (beaconsWithDistance.containsKey(key)) {
                ArrayList<Double> distances = beaconsWithDistance.get(key);
                if (distances.size() == triggerThreshold) {
                    distances.remove(0);
                }
                distances.add(mBeacon.getDistance());
                beaconsWithDistance.put(key, distances);
            } else {
                ArrayList<Double> distances = new ArrayList<>();
                distances.add(mBeacon.getDistance());
                beaconsWithDistance.put(key, distances);
            }
        }
    }
}
