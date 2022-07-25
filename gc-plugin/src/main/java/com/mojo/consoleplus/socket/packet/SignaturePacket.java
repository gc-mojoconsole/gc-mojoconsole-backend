package com.mojo.consoleplus.socket.packet;

import emu.grasscutter.Grasscutter;

public class SignaturePacket extends BasePacket {
    public String signature;

    public SignaturePacket(String signature) {
        this.signature = signature;
    }

    @Override
    public String getPacket() {
        return Grasscutter.getGsonFactory().toJson(this);
    }

    @Override
    public PacketEnum getType() {
        return PacketEnum.Signature;
    }
}
