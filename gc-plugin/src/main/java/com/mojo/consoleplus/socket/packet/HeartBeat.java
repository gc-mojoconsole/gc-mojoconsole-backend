package com.mojo.consoleplus.socket.packet;

import emu.grasscutter.Grasscutter;
import emu.grasscutter.utils.JsonUtils;

// 心跳包
public class HeartBeat extends BasePacket {
    public String ping;

    public HeartBeat(String ping) {
        this.ping = ping;
    }

    @Override
    public String getPacket() {
        return JsonUtils.encode(this);
    }

    @Override
    public PacketEnum getType() {
        return PacketEnum.HeartBeat;
    }
}
