package com.mojo.consoleplus.socket;

import com.mojo.consoleplus.socket.packet.OtpPacket;
import com.mojo.consoleplus.socket.packet.player.PlayerList;
import org.luaj.vm2.ast.Str;

import java.util.HashMap;
import java.util.concurrent.atomic.AtomicReference;

// Socket 数据保存
public class SocketData {
    public static HashMap<String, PlayerList> playerList = new HashMap<>();

    public static HashMap<String, OtpPacket> tickets = new HashMap<>();

    public static String getPlayer(int uid) {
        for (PlayerList player : playerList.values()) {
            if (player.playerMap.get(uid) != null) {
                return player.playerMap.get(uid);
            }
        }
        return null;
    }

    public static String getPlayerInServer(int uid) {
        AtomicReference<String> ret = new AtomicReference<>();
        playerList.forEach((key, value) -> {
            if (value.playerMap.get(uid) != null) {
                ret.set(key);
            }
        });
        return ret.get();
    }
}
