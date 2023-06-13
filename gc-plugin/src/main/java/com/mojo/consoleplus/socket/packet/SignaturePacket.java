package com.mojo.consoleplus.socket.packet;

import emu.grasscutter.Grasscutter;
import emu.grasscutter.utils.JsonUtils;

public class SignaturePacket extends BasePacket {
    public String signature;

    public SignaturePacket(String signature) {
        this.signature = signature;
    }

    @Override
    public String getPacket() {
        return JsonUtils.encode(this);
    }

    @Override
    public PacketEnum getType() {
        return PacketEnum.Signature;
    }
}
