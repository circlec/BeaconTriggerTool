package com.zc.blescandemo;

import android.bluetooth.BluetoothDevice;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import java.math.BigDecimal;


public class Beacon implements Parcelable {
    private final String proximityUUID;
    private final String name;
    private final String macAddress;
    private final int major;
    private final int minor;
    private final int measuredPower;
    private final int rssi;
    protected Double accuracy;

    final private static char[] hexArray = {'0', '1', '2', '3', '4', '5', '6',
            '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};

    protected static double calculateAccuracy(int measuredPower, double rssi) {
        if (rssi == 0) {
            return -1.0;
        }
        if (measuredPower == 0) {
            return -1.0;
        }
        if (rssi > 0) {
            rssi = rssi - 128;
        }
        double ratio = rssi * 1.0 / measuredPower;
        if (ratio < 1.0) {
            return Math.pow(ratio, 10);
        } else {
            double accuracy = (0.89976) * Math.pow(ratio, 7.7095) + 0.111;
//            double accuracy = (0.42093) * Math.pow(ratio, 6.9476) + 0.54992;
            return accuracy;
        }
    }

    public static final Creator<Beacon> CREATOR = new Creator<Beacon>() {
        public Beacon createFromParcel(Parcel source) {
            return new Beacon(source);
        }

        public Beacon[] newArray(int size) {
            return new Beacon[size];
        }
    };

    private Beacon(Parcel parcel) {
        this.proximityUUID = parcel.readString();
        this.name = parcel.readString();
        this.macAddress = parcel.readString();
        this.major = parcel.readInt();
        this.minor = parcel.readInt();
        this.measuredPower = parcel.readInt();
        this.rssi = parcel.readInt();
        this.power = parcel.readInt();
    }

    public Beacon(String proximityUUID, String name, String macAddress,
                  int major, int minor, int measuredPower, int rssi) {
        this.proximityUUID = proximityUUID;
        if (TextUtils.isEmpty(name)) {
            this.name = "Unknown";
        } else {
            this.name = name;
        }
        this.macAddress = macAddress;
        this.major = major;
        this.minor = minor;
        this.measuredPower = measuredPower;
        this.rssi = rssi;
    }

    public Beacon(String proximityUUID, String name, String macAddress,
                  int major, int minor, int measuredPower, int rssi, int power) {
        this.proximityUUID = proximityUUID;
        if (TextUtils.isEmpty(name)) {
            this.name = "Unknown";
        } else {
            this.name = name;
        }
        this.macAddress = macAddress;
        this.major = major;
        this.minor = minor;
        this.measuredPower = measuredPower;
        this.rssi = rssi;
        this.power = power;
    }

    public int describeContents() {
        return 0;
    }

    public boolean equals(Object o) {
        if (this == o)
            return true;
        if ((o == null) || (getClass() != o.getClass())) {
            return false;
        }
        Beacon beacon = (Beacon) o;
        return this.getProximityUUID().equals(beacon.getProximityUUID()) && this.getMajor() == beacon.getMajor() && this.getMinor() == beacon.getMinor();
    }

    /**
     * 获取beacon的距离
     *
     * @return beacon的距离
     */
    public double getDistance() {
        if (accuracy == null) {
            // accuracy = calculateAccuracy(measuredPower,
            // runningAverageRssi != null ? runningAverageRssi : rssi);
            if (rssi > 0) {
                accuracy = calculateAccuracy(measuredPower, rssi - 128);
            } else {
                accuracy = calculateAccuracy(measuredPower, rssi);
            }
        }
        // 此格式转换可以保持俄语环境下.不被转成，
        BigDecimal bg = new BigDecimal(accuracy);
        double distanceFormatted = bg.setScale(2, BigDecimal.ROUND_HALF_UP)
                .doubleValue();
        return distanceFormatted;
    }

    /**
     * 获取beacon的mac地址
     *
     * @return beacon的mac地址
     */
    public String getMacAddress() {
        return this.macAddress;
    }

    /**
     * 获取beacon的Major值
     *
     * @return beacon的Major值
     */
    public int getMajor() {
        return this.major;
    }

    /**
     * 获取beacon的measurePower
     *
     * @return beacon的measurePower
     */
    public int getMeasuredPower() {
        return this.measuredPower;
    }

    /**
     * 获取beacon的minor值
     *
     * @return beacon的minor值
     */
    public int getMinor() {
        return this.minor;
    }

    /**
     * 获取beacon名称
     *
     * @return beacon名称
     */
    public String getName() {
        return this.name;
    }


    /**
     * 获取beacon的ProximityUUID
     *
     * @return beacon的ProximityUUID
     */
    public String getProximityUUID() {
        return this.proximityUUID;
    }

    /**
     * 获取beacon的RSSI
     *
     * @return beacon的RSSI
     */
    public int getRssi() {
        return this.rssi;
    }

    int power;

    /**
     * 获取beacon的电量（固件版本2.1可以使用）
     *
     * @return beacon的电量
     */
    public int getPower() {
        return this.power;
    }

    public int hashCode() {
        int result = this.proximityUUID.hashCode();
        result = 31 * result + this.major;
        result = 31 * result + this.minor;
        return result;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.proximityUUID);
        dest.writeString(this.name);
        dest.writeString(this.macAddress);
        dest.writeInt(this.major);
        dest.writeInt(this.minor);
        dest.writeInt(this.measuredPower);
        dest.writeInt(this.rssi);
    }

    public static Beacon beaconFromLeScan(BluetoothDevice device, int rssi,
                                          byte[] scanRecord) {
        if (device == null) {
            return null;
        }
        String name = device.getName();
        if (TextUtils.isEmpty(name)) {
            return null;
        }
        if (!name.contains("AprilBeacon") && !name.contains("abeacon") && !name.equals("be")) {
            return null;
        }

        return fromScanData(device, rssi, scanRecord);
    }

    private static Beacon fromScanData(BluetoothDevice device, int rssi,
                                       byte[] scanData) {

        if (((int) scanData[5] & 0xff) == 0x4c
                && ((int) scanData[6] & 0xff) == 0x00
                && ((int) scanData[7] & 0xff) == 0x02
                && ((int) scanData[8] & 0xff) == 0x15) {
        } else {
            return null;
        }

        int major = (scanData[25] & 0xff) * 0x100 + (scanData[26] & 0xff);
        int minor = (scanData[27] & 0xff) * 0x100 + (scanData[28] & 0xff);
        int measuredPower = (int) scanData[29];
        int power = (int) scanData[31];

        byte[] proximityUuidBytes = new byte[16];
        System.arraycopy(scanData, 9, proximityUuidBytes, 0, 16);
        String hexString = bytesToHex(proximityUuidBytes);
        StringBuilder sb = new StringBuilder();
        sb.append(hexString.substring(0, 8));
        sb.append("-");
        sb.append(hexString.substring(8, 12));
        sb.append("-");
        sb.append(hexString.substring(12, 16));
        sb.append("-");
        sb.append(hexString.substring(16, 20));
        sb.append("-");
        sb.append(hexString.substring(20, 32));
        String proximityUuid = sb.toString().toUpperCase();

        return new Beacon(proximityUuid, device.getName(), device.getAddress(),
                major, minor, measuredPower, rssi, power);

    }

    private static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        int v;
        for (int j = 0; j < bytes.length; j++) {
            v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }
}
