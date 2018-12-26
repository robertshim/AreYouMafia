package com.joycity.intern.areyoumafia;

import android.os.Parcel;
import android.os.Parcelable;

public class RoomInfo implements Parcelable {
    public int id;
    public String url;
    public int port;
    public int numOfPlayer;

    public RoomInfo(int id, String url, int port, int numOfPlayer) {
        this.id = id;
        this.url = url;
        this.port = port;
        this.numOfPlayer = numOfPlayer;
    }

    protected RoomInfo(Parcel in) {
        id = in.readInt();
        numOfPlayer = in.readInt();
        url = in.readString();
        port = in.readInt();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeInt(numOfPlayer);
        dest.writeString(url);
        dest.writeInt(port);
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
