package com.mojo.consoleplus.socket;

import com.mojo.consoleplus.ConsolePlus;
import com.mojo.consoleplus.command.PluginCommand;
import com.mojo.consoleplus.config.MojoConfig;
import com.mojo.consoleplus.socket.packet.*;
import com.mojo.consoleplus.socket.packet.player.Player;
import com.mojo.consoleplus.socket.packet.player.PlayerList;
import emu.grasscutter.Grasscutter;
import emu.grasscutter.command.CommandMap;
// import emu.grasscutter.utils.MessageHandler;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import static com.mojo.consoleplus.ConsolePlus.gson;

// Socket 客户端
public class SocketClient {
    public static ClientThread clientThread;

    public static Logger mLogger;

    public static Timer timer;

    public static boolean connect = false;

    public static ReceiveThread receiveThread;

    public static void connectServer(Logger logger) {
        mLogger = logger;
        connectServer();
    }

    // 连接服务器
    public static void connectServer() {
        if (connect) return;
        if (clientThread != null) {
            mLogger.warn("[Mojo Console] Retry connecting to the server after 15 seconds");
            try {
                Thread.sleep(15000);
            } catch (InterruptedException ex) {
                throw new RuntimeException(ex);
            }
        }
        MojoConfig config = ConsolePlus.config;
        clientThread = new ClientThread(config.socketHost, config.socketPort);

        if (timer != null) {
            timer.cancel();
        }
        timer = new Timer();
        timer.schedule(new SendHeartBeatPacket(), 500);
        timer.schedule(new SendPlayerListPacket(), 1000);
    }

    // 发送数据包
    public static boolean sendPacket(BasePacket packet) {
        var p = SocketUtils.getPacket(packet);
        if (!clientThread.sendPacket(p)) {
            mLogger.warn("[Mojo Console] Send packet to server failed");
            connect = false;
            connectServer();
            return false;
        }
        return true;
    }

    // 发送数据包带数据包ID
    public static boolean sendPacket(BasePacket packet, String packetID) {
        if (!clientThread.sendPacket(SocketUtils.getPacketAndPackID(packet, packetID))) {
            mLogger.warn("[Mojo Console] Send packet to server failed");
            connect = false;
            connectServer();
            return false;
        }
        return true;
    }

    // 心跳包发送
    private static class SendHeartBeatPacket extends TimerTask {
        @Override
        public void run() {
            if (connect) {
                sendPacket(new HeartBeat("Pong"));
            }
        }
    }

    private static class SendPlayerListPacket extends TimerTask {
        @Override
        public void run() {
            if (connect) {
                PlayerList playerList = new PlayerList();
                playerList.player = Grasscutter.getGameServer().getPlayers().size();
                ArrayList<String> playerNames = new ArrayList<>();
                for (emu.grasscutter.game.player.Player player : Grasscutter.getGameServer().getPlayers().values()) {
                    playerNames.add(player.getNickname());
                    playerList.playerMap.put(player.getUid(), player.getNickname());
                }
                playerList.playerList = playerNames;
                sendPacket(playerList);
            }
        }
    }

    // 数据包接收
    private static class ReceiveThread extends Thread {
        private InputStream is;
        private boolean exit;

        public ReceiveThread(Socket socket) {
            try {
                is = socket.getInputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }
            start();
        }

        @Override
        public void run() {
            //noinspection InfiniteLoopStatement
            while (true) {
                try {
                    if (exit) return;
                    String data = SocketUtils.readString(is);
                    Packet packet = gson.fromJson(data, Packet.class);
                    switch (packet.type) {
                        // 玩家类
                        case Player:
                            var player = gson.fromJson(packet.data, Player.class);
                            switch (player.type) {
                                // 运行命令
                                case RunCommand -> {
                                    var command = player.data;
                                    var playerData = Grasscutter.getGameServer().getPlayerByUid(player.uid);
                                    if (playerData == null) {
                                        sendPacket(new HttpPacket(404, "[Mojo Console] Player not found."), packet.packetID);
                                        return;
                                    }
                                    // Player MessageHandler do not support concurrency
                                    //noinspection SynchronizationOnLocalVariableOrMethodParameter
                                    synchronized (playerData) {
                                        try {
                                            // var resultCollector = new MessageHandler();
                                            // playerData.setMessageHandler(resultCollector);
                                            CommandMap.getInstance().invoke(playerData, playerData, command);
                                            sendPacket(new HttpPacket(200, "success", "resultCollector.getMessage()"), packet.packetID);
                                        } catch (Exception e) {
                                            mLogger.warn("[Mojo Console] Run command failed.", e);
                                            sendPacket(new HttpPacket(500, "error", e.getLocalizedMessage()), packet.packetID);
                                        } finally {
                                            // playerData.setMessageHandler(null);
                                        }
                                    }
                                }
                                // 发送信息
                                case DropMessage -> {
                                    var playerData = Grasscutter.getGameServer().getPlayerByUid(player.uid);
                                    if (playerData == null) {
                                        return;
                                    }
                                    playerData.dropMessage(player.data);
                                }
                            }
                            break;
                        case OtpPacket:
                            var otpPacket = gson.fromJson(packet.data, OtpPacket.class);
                            PluginCommand.getInstance().tickets.put(otpPacket.otp, new PluginCommand.Ticket(Grasscutter.getGameServer().getPlayerByUid(otpPacket.uid), otpPacket.expire, otpPacket.api));
                        case Signature:
                            var signaturePacket = gson.fromJson(packet.data, SignaturePacket.class);
                            ConsolePlus.authHandler.setSignature(signaturePacket.signature);
                            break;
                    }
                } catch (Throwable e) {
                    e.printStackTrace();
                    if (!sendPacket(new HeartBeat("Pong"))) {
                        return;
                    }
                }
            }
        }

        public void exit() {
            exit = true;
        }
    }

    // 客户端连接线程
    private static class ClientThread extends Thread {
        private final String ip;
        private final int port;
        private Socket socket;
        private OutputStream os;

        public ClientThread(String ip, int port) {
            this.ip = ip;
            this.port = port;
            start();
        }

        public Socket getSocket() {
            return socket;
        }

        public boolean sendPacket(String string) {
            return SocketUtils.writeString(os, string);
        }

        @Override
        public void run() {
            try {
                if (receiveThread != null) {
                    receiveThread.exit();
                }

                socket = new Socket(ip, port);
                connect = true;
                os = socket.getOutputStream();
                mLogger.info("[Mojo Console] Connect to server: " + ip + ":" + port);
                SocketClient.sendPacket(new AuthPacket(ConsolePlus.config.socketToken));
                receiveThread = new ReceiveThread(socket);
            } catch (IOException e) {
                connect = false;
                mLogger.warn("[Mojo Console] Connect to server failed: " + ip + ":" + port);
                connectServer();
            }
        }
    }
}
