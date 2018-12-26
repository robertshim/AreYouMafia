package com.joycity.intern.areyoumafia;

public class GameMessage {
    public enum MESSAGE_TYPE{
        CREATE, JOIN, CHAT, VOTE
    }
    MESSAGE_TYPE type;
    int roomId;
    char[] write = new char[16];
    char[] text = new char[128];
}
