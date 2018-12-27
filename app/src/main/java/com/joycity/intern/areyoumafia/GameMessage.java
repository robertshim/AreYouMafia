package com.joycity.intern.areyoumafia;

public class GameMessage {
    int type;
    int roomId;
    String writer;
    String text;

    public GameMessage() {
    }

    public GameMessage(byte[] data) {
        this.getData(data);
    }

    public GameMessage(int type, int roomId, String writer, String text) {
        this.type = type;
        this.roomId = roomId;
        this.writer = writer;
        this.text = text;
    }

    public byte[] toBytes() {
        byte[] bytes = new byte[152];

        bytes[3] = (byte) ((type & 0xFF000000) >> 24);
        bytes[2] = (byte) ((type & 0x00FF0000) >> 16);
        bytes[1] = (byte) ((type & 0x0000FF00) >> 8);
        bytes[0] = (byte) (type & 0x000000FF);

        bytes[7] = (byte) ((roomId & 0xFF000000) >> 24);
        bytes[6] = (byte) ((roomId & 0x00FF0000) >> 16);
        bytes[5] = (byte) ((roomId & 0x0000FF00) >> 8);
        bytes[4] = (byte) (roomId & 0x000000FF);

        byte[] temp = String.format("%-16s", writer).replace(" ", "\0").getBytes();

        System.arraycopy(temp, 0, bytes, 8, temp.length);

        temp = text.getBytes();

        System.arraycopy(temp, 0, bytes, 24, temp.length);

        return bytes;
    }

    public void getData(byte[] bytes) {
        type |= (((int) bytes[3]) << 24) & 0xFF000000;
        type |= (((int) bytes[2]) << 16) & 0xFF0000;
        type |= (((int) bytes[1]) << 8) & 0xFF00;
        type |= (((int) bytes[0])) & 0xFF;

        roomId |= (((int) bytes[7]) << 24) & 0xFF000000;
        roomId |= (((int) bytes[6]) << 16) & 0xFF0000;
        roomId |= (((int) bytes[5]) << 8) & 0xFF00;
        roomId |= (((int) bytes[4])) & 0xFF;

        byte[] temp = new byte[16];
        System.arraycopy(bytes, 8, temp, 0, 16);
        this.writer = new String(temp).trim();

        temp = new byte[128];
        System.arraycopy(bytes, 24, temp, 0, 128);
        this.text = new String(temp).trim();

    }
}
