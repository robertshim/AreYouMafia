package com.joycity.intern.areyoumafia;

import android.os.Parcel;
import android.os.Parcelable;

public class RoomInfo implements Parcelable {
    public int id;
    public String url;

    protected RoomInfo(Parcel in) {
        id = in.readInt();
        url = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(url);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<RoomInfo> CREATOR = new Creator<RoomInfo>() {
        @Override
        public RoomInfo createFromParcel(Parcel in) {
            return new RoomInfo(in);
        }

        @Override
        public RoomInfo[] newArray(int size) {
            return new RoomInfo[size];
        }
    };
}
