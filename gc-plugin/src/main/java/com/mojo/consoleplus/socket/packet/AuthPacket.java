package com.mojo.consoleplus.socket.packet;

import com.google.gson.GsonBuilder;

import static com.mojo.consoleplus.ConsolePlus.gson;

public class AuthPacket extends BasePacket {
    public String token;

    public AuthPacket(String token) {
        this.token = token;
    }

    @Override
    public String getPacket() {
        return gson.toJson(this);
    }

    @Override
    public PacketEnum getType() {
        return PacketEnum.AuthPacket;
    }
}
