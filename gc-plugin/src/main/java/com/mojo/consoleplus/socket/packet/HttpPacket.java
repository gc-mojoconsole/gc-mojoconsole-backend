package com.mojo.consoleplus.socket.packet;

import emu.grasscutter.Grasscutter;
import emu.grasscutter.utils.JsonUtils;

// http返回数据
public class HttpPacket extends BasePacket {
    public int code;
    public String message;
    public String data;

    public HttpPacket(int code, String message, String data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    public HttpPacket(int code, String message) {
        this.code = code;
        this.message = message;
    }

    @Override
    public String getPacket() {
        return JsonUtils.encode(this);
    }

    @Override
    public PacketEnum getType() {
        return PacketEnum.HttpPacket;
    }

    @Override
    public String toString() {
        return "HttpPacket [code=" + code + ", message=" + message + ", data=" + data + "]";
    }
}
