package com.zc.blescandemo;

import android.text.TextUtils;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
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
    // 已经通知过的beacon 离开beacon范围后移除
    private HashMap<String, Long> notifiedBeacons = new HashMap<>();
    // 未扫描到的beacon集合 key--uuid-major-minor值 未扫描到的次数 次数达到n次将存储的通知beacon移除
    private HashMap<String, Integer> noScanBeacons = new HashMap<>();
    private String lasteTriggerKey;

    private ArrayList<Beacon> usefulBeacons = new ArrayList<>();

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
        getUsefulBeacon(beacons);
        detectExit(usefulBeacons);
        setBeaconWithDistance(usefulBeacons);
        setAvgDistance();
        triggerBeacon();
    }

    private void getUsefulBeacon(List<Beacon> beacons) {
        usefulBeacons.clear();
        for (Beacon beacon : beacons) {
            String beaconKey = beacon.getProximityUUID().toUpperCase() + "-" + beacon.getMajor() + "-" + beacon.getMinor();
            for (BeaconData beaconData : beaconDatas) {
                String beaconDataKey = beaconData.getUuid().toUpperCase() + "-" + beaconData.getMajor() + "-" + beaconData.getMinor();
                if (beaconKey.equals(beaconDataKey)) {
                    usefulBeacons.add(beacon);
                }
            }
        }
    }

    private void detectExit(ArrayList<Beacon> mBeaconList) {
        ArrayList<String> keys = new ArrayList<>();
        for (Beacon beacon : mBeaconList) {
            String key = beacon.getProximityUUID() + "-" + beacon.getMajor()
                    + "-" + beacon.getMinor();
            keys.add(key);
        }

        Iterator<Map.Entry<String, Double>> it = beaconsWithAvgDistance.entrySet()
                .iterator();
        while (it.hasNext()) {
            Map.Entry<String, Double> entry = it.next();
            if (keys.size() > 0 && keys.contains(entry.getKey())) {
                noScanBeacons.put(entry.getKey(), 0);
            } else {
                Integer integer = noScanBeacons.get(entry.getKey());
                noScanBeacons.remove(entry.getKey());
                if (integer != null && integer < triggerThreshold) {
                    noScanBeacons.put(entry.getKey(), integer + 1);
                } else if (integer != null && integer == triggerThreshold) {
                    it.remove();
                    if (beaconsWithDistance.containsKey(entry.getKey()))
                        beaconsWithDistance.remove(entry.getKey());
                    // 判定离开
                    if (notifiedBeacons.containsKey(entry.getKey())) {
                        Log.i(TAG, "3次未扫描到  remove === " + entry.getKey());
                        notifiedBeacons.remove(entry.getKey());
                    }
                }
            }
        }
    }

    private void triggerBeacon() {
        if (beaconsWithAvgDistance.size() == 0) return;
        double tempDistance = 100;
        String tempKey = "";
        for (Map.Entry<String, Double> entry : beaconsWithAvgDistance.entrySet()) {
            double triggerDistance = entry.getValue();
            if (triggerDistance < tempDistance) {
                tempDistance = triggerDistance;
                tempKey = entry.getKey();
            }
        }
        //如果需要返回beaconData中的数据 可以再遍历取出
        if (TextUtils.isEmpty(tempKey)) return;
        if (!notifiedBeacons.containsKey(tempKey)) {//没有触发过
            Log.i(TAG, " 没有触发过 triggerBeacon: 满足触发条件且距离最近的key = " + tempKey);
            notifiedBeacons.put(tempKey, System.currentTimeMillis());
            lasteTriggerKey = tempKey;
        } else if (System.currentTimeMillis() - notifiedBeacons.get(tempKey) >= triggerDelayTime) {
            if (TextUtils.isEmpty(lasteTriggerKey) || !lasteTriggerKey.equals(tempKey)) {
                Log.i(TAG, "触发过且触发过其他的一段时间后 triggerBeacon: 满足触发条件且距离最近的key = " + tempKey);
                notifiedBeacons.put(tempKey, System.currentTimeMillis());
            }
        }
        Log.i(TAG, "triggerBeacon: notifyedbeacons.size = " + notifiedBeacons.size());
        Log.i(TAG, "triggerBeacon: -------------------------------------------------");
    }

    private void setAvgDistance() {
        if (beaconsWithDistance.size() > 0) {
            for (Map.Entry<String, ArrayList<Double>> entry : beaconsWithDistance.entrySet()) {
                String distanceKey = entry.getKey();
                ArrayList<Double> distances = entry.getValue();
                for (BeaconData beaconData : beaconDatas) {
                    int count = 0;//标记满足条件次数
                    double totalDistance = 0;
                    for (Double distance : distances) {
                        String tempKey = beaconData.getUuid().toUpperCase() + "-" + beaconData.getMajor() + "-" + beaconData.getMinor();
                        if (distanceKey.equals(tempKey)
                                && distance < Double.valueOf(beaconData.getTriggerDistance())) {
                            count++;
                            totalDistance += distance;
                        }
                    }
                    if (count == triggerThreshold) { //满足进入条件 要判断是否进入 还需要比较最近的
                        double avgDistance = totalDistance / triggerThreshold;
                        beaconsWithAvgDistance.put(distanceKey, avgDistance);
                    } else if (count == 0 && beaconsWithAvgDistance.containsKey(distanceKey)) {//离开
                        Log.i(TAG, "getTriggerBeacon: 三次满足离开条件");
                        beaconsWithAvgDistance.remove(distanceKey);
                        if (beaconsWithDistance.containsKey(distanceKey))
                            beaconsWithDistance.remove(distanceKey);
                        if (notifiedBeacons.containsKey(distanceKey))
                            notifiedBeacons.remove(distanceKey);
                    }
                }
            }
        } else {
            beaconsWithAvgDistance.clear();
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
