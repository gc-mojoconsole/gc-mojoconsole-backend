package com.mojo.consoleplus.socket.packet.player;

import com.mojo.consoleplus.socket.SocketServer;
import com.mojo.consoleplus.socket.packet.BasePacket;
import com.mojo.consoleplus.socket.packet.PacketEnum;
import emu.grasscutter.Grasscutter;

import static com.mojo.consoleplus.ConsolePlus.gson;

// 玩家操作类
public class Player extends BasePacket {
    public PlayerEnum type;
    public int uid;
    public String data;

    @Override
    public String getPacket() {
        return gson.toJson(this);
    }

    @Override
    public PacketEnum getType() {
        return PacketEnum.Player;
    }

    public static void dropMessage(int uid, String str) {
        Player p = new Player();
        p.type = PlayerEnum.DropMessage;
        p.uid = uid;
        p.data = str;
        SocketServer.sendAllPacket(p);
    }
}
