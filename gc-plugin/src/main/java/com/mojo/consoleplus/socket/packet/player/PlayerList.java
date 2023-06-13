package com.mojo.consoleplus.socket.packet.player;

import com.mojo.consoleplus.socket.packet.BasePacket;
import com.mojo.consoleplus.socket.packet.PacketEnum;
import emu.grasscutter.Grasscutter;
import emu.grasscutter.utils.JsonUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


// 玩家列表信息
public class PlayerList extends BasePacket {
    public int player = -1;
    public List<String> playerList = new ArrayList<>();
    public Map<Integer, String> playerMap = new HashMap<>();

    @Override
    public String getPacket() {
        return JsonUtils.encode(this);
    }

    @Override
    public PacketEnum getType() {
        return PacketEnum.PlayerList;
    }

    @Override
    public String toString() {
        return "PlayerList [player=" + player + ", playerList=" + playerList + ", playerMap=" + playerMap + "]";
    }
}
