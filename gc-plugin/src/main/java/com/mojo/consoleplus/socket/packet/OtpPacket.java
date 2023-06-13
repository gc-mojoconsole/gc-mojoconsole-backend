package com.mojo.consoleplus.socket.packet;

import emu.grasscutter.Grasscutter;
import emu.grasscutter.utils.JsonUtils;


public class OtpPacket extends BasePacket {
    public int uid;
    public String otp;
    public long expire;
    public Boolean api;
    public String key;

    public boolean remove = false;

    public OtpPacket(int uid, String opt, long expire, Boolean api) {
        this.uid = uid;
        this.expire = expire;
        this.api = api;
        this.otp = opt;
    }

    public OtpPacket(String opt) {
        this.otp = opt;
        remove = true;
    }

    @Override
    public String getPacket() {
        return JsonUtils.encode(this);
    }

    @Override
    public PacketEnum getType() {
        return PacketEnum.OtpPacket;
    }
}
