package com.mojo.consoleplus;

import com.mojo.consoleplus.socket.SocketClient;
import com.mojo.consoleplus.socket.packet.player.PlayerList;
import emu.grasscutter.Grasscutter;
import emu.grasscutter.game.player.Player;
import emu.grasscutter.server.event.player.PlayerJoinEvent;
import emu.grasscutter.server.event.player.PlayerQuitEvent;
import emu.grasscutter.server.event.game.ReceiveCommandFeedbackEvent;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;


import java.util.ArrayList;

public class EventListeners {
    /* Thanks to opencommand project */
    private static StringBuilder consoleMessageHandler;
    private static final Int2ObjectMap<StringBuilder> playerMessageHandlers = new Int2ObjectOpenHashMap<>();

    public static void setConsoleMessageHandler(StringBuilder handler) {
        consoleMessageHandler = handler;
    }

    /**
     * 获取新的玩家消息处理类
     * 获取时将创建或清空消息处理器并返回实例，**在执行命令前获取！**
     * @param uid 玩家uid
     * @return 新的玩家消息处理类
     */
    public static StringBuilder getPlayerMessageHandler(int uid) {
        var handler = playerMessageHandlers.get(uid);
        if (handler == null) {
            handler = new StringBuilder();
            playerMessageHandlers.put(uid, handler);
        }
        return handler;
    }

    public static void onPlayerJoin(PlayerJoinEvent playerJoinEvent) {
        PlayerList playerList = new PlayerList();
        playerList.player = Grasscutter.getGameServer().getPlayers().size();
        ArrayList<String> playerNames = new ArrayList<>();
        playerNames.add(playerJoinEvent.getPlayer().getNickname());
        playerList.playerMap.put(playerJoinEvent.getPlayer().getUid(), playerJoinEvent.getPlayer().getNickname());
        for (Player player : Grasscutter.getGameServer().getPlayers().values()) {
            playerNames.add(player.getNickname());
            playerList.playerMap.put(player.getUid(), player.getNickname());
        }
        playerList.playerList = playerNames;
        SocketClient.sendPacket(playerList);
    }

    public static void onPlayerQuit(PlayerQuitEvent playerQuitEvent) {
        PlayerList playerList = new PlayerList();
        playerList.player = Grasscutter.getGameServer().getPlayers().size();
        ArrayList<String> playerNames = new ArrayList<>();
        for (Player player : Grasscutter.getGameServer().getPlayers().values()) {
            playerNames.add(player.getNickname());
            playerList.playerMap.put(player.getUid(), player.getNickname());
        }
        playerList.playerMap.remove(playerQuitEvent.getPlayer().getUid());
        playerNames.remove(playerQuitEvent.getPlayer().getNickname());
        playerList.playerList = playerNames;
        SocketClient.sendPacket(playerList);
    }

    public static void onCommandResponse(ReceiveCommandFeedbackEvent event) {
        StringBuilder handler;
        if (event.getPlayer() == null) {
            handler = consoleMessageHandler;
        } else {
            handler = playerMessageHandlers.get(event.getPlayer().getUid());
        }

        if (handler != null) {
            if (!handler.isEmpty()) {
                // New line
                handler.append(System.lineSeparator());
            }
            handler.append(event.getMessage());
        }
    }

}
