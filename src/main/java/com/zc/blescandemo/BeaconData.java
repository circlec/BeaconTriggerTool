package com.zc.blescandemo;

import android.os.Parcel;
import android.os.Parcelable;

public class BeaconData implements Parcelable{
    private String uuid;
    private String major;
    private String minor;
    private String triggerDistance;
    private String msg;

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getMajor() {
        return major;
    }

    public void setMajor(String major) {
        this.major = major;
    }

    public String getMinor() {
        return minor;
    }

    public void setMinor(String minor) {
        this.minor = minor;
    }

    public String getTriggerDistance() {
        return triggerDistance;
    }

    public void setTriggerDistance(String triggerDistance) {
        this.triggerDistance = triggerDistance;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.uuid);
        dest.writeString(this.major);
        dest.writeString(this.minor);
        dest.writeString(this.triggerDistance);
        dest.writeString(this.msg);
    }

    public BeaconData() {
    }

    protected BeaconData(Parcel in) {
        this.uuid = in.readString();
        this.major = in.readString();
        this.minor = in.readString();
        this.triggerDistance = in.readString();
        this.msg = in.readString();
    }

    public static final Creator<BeaconData> CREATOR = new Creator<BeaconData>() {
        @Override
        public BeaconData createFromParcel(Parcel source) {
            return new BeaconData(source);
        }

        @Override
        public BeaconData[] newArray(int size) {
            return new BeaconData[size];
        }
    };
}
