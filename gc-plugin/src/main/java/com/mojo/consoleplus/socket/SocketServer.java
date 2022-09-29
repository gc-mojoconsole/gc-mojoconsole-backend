package com.mojo.consoleplus.socket;

import com.mojo.consoleplus.ConsolePlus;
import com.mojo.consoleplus.socket.packet.*;
import com.mojo.consoleplus.socket.packet.player.PlayerList;
import emu.grasscutter.Grasscutter;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

import static com.mojo.consoleplus.ConsolePlus.gson;

// Socket 服务器
public class SocketServer {
    // 客户端超时时间
    private static final int TIMEOUT = 5000;
    private static final HashMap<String, ClientThread> clientList = new HashMap<>();

    private static final HashMap<String, Integer> clientTimeout = new HashMap<>();
    private static Logger mLogger;

    public static void startServer(Logger logger) {
        mLogger = logger;
        try {
            int port = ConsolePlus.config.socketPort;
            new Timer().schedule(new SocketClientCheck(), 500);
            new WaitClientConnect(port);
        } catch (Throwable e) {
            mLogger.error("[Mojo Console] Socket server start failed", e);
        }
    }

    // 向全部客户端发送数据
    public static boolean sendAllPacket(BasePacket packet) {
        var p = SocketUtils.getPacket(packet);
        HashMap<String, ClientThread> old = (HashMap<String, ClientThread>) clientList.clone();
        for (var client : old.entrySet()) {
            if (!client.getValue().sendPacket(p)) {
                mLogger.warn("[Mojo Console] Send packet to client {} failed", client.getKey());
                clientList.remove(client.getKey());
            }
        }
        return false;
    }

    // 根据地址发送到相应的客户端
    public static boolean sendPacket(String address, BasePacket packet) {
        var p = SocketUtils.getPacket(packet);
        var client = clientList.get(address);
        if (client != null) {
            if (client.sendPacket(p)) {
                return true;
            }
            mLogger.warn("[Mojo Console] Send packet to client {} failed", address);
            clientList.remove(address);
        }
        return false;
    }

    // 根据Uid发送到相应的客户端异步返回数据
    public static boolean sendUidPacket(Integer playerId, BasePacket player, SocketDataWait<?> socketDataWait) {
        var p = SocketUtils.getPacketAndPackID(player);
        var clientID = SocketData.getPlayerInServer(playerId);
        if (clientID == null) return false;
        var client = clientList.get(clientID);
        if (client != null) {
            socketDataWait.uid = p.get(0);
            if (!client.sendPacket(p.get(1), socketDataWait)) {
                mLogger.warn("[Mojo Console] Send packet to client {} failed", clientID);
                clientList.remove(clientID);
                return false;
            }
            return true;
        }
        return false;
    }

    // 客户端超时检测
    private static class SocketClientCheck extends TimerTask {
        @Override
        public void run() {
            HashMap<String, Integer> old = (HashMap<String, Integer>) clientTimeout.clone();
            for (var client : old.entrySet()) {
                var clientID = client.getKey();
                var clientTime = client.getValue();
                if (clientTime > TIMEOUT) {
                    mLogger.info("[Mojo Console] Client {} timeout, disconnect.", clientID);
                    clientList.remove(clientID);
                    clientTimeout.remove(clientID);
                    SocketData.playerList.remove(clientID);
                } else {
                    clientTimeout.put(clientID, clientTime + 500);
                }
            }
        }
    }

    // 客户端数据包处理
    private static class ClientThread extends Thread {
        private final Socket socket;
        private InputStream is;
        private OutputStream os;
        private final String address;
        private final String token;
        private boolean auth = false;

        private final HashMap<String, SocketDataWait<?>> socketDataWaitList = new HashMap<>();

        public ClientThread(Socket accept) {
            socket = accept;
            address = socket.getInetAddress() + ":" + socket.getPort();
            token = ConsolePlus.config.socketToken;
            try {
                is = accept.getInputStream();
                os = accept.getOutputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }
            start();
        }

        public Socket getSocket() {
            return socket;
        }

        // 发送数据包
        public boolean sendPacket(String packet) {
            return SocketUtils.writeString(os, packet);
        }

        // 发送异步数据包
        public boolean sendPacket(String packet, SocketDataWait<?> socketDataWait) {
            if (SocketUtils.writeString(os, packet)) {
                socketDataWaitList.put(socketDataWait.uid, socketDataWait);
                return true;
            } else {
                return false;
            }
        }

        @Override
        public void run() {
            // noinspection InfiniteLoopStatement
            while (true) {
                try {
                    String data = SocketUtils.readString(is);
                    Packet packet = gson.fromJson(data, Packet.class);
                    if (packet.type == PacketEnum.AuthPacket) {
                        AuthPacket authPacket = gson.fromJson(packet.data, AuthPacket.class);
                        if (authPacket.token.equals(token)) {
                            mLogger.info("[Mojo Console] Client {} auth success.", address);
                            auth = true;
                            clientList.put(address, this);
                            clientTimeout.put(address, 0);
                            sendPacket(SocketUtils.getPacket(new SignaturePacket(ConsolePlus.authHandler.getSignature())));
                        } else {
                            mLogger.error("[Mojo Console] AuthPacket: {} auth filed.", address);
                            socket.close();
                            return;
                        }
                    }
                    if (!auth) {
                        mLogger.error("[Mojo Console] AuthPacket: {} not auth", address);
                        socket.close();
                        return;
                    }
                    switch (packet.type) {
                        // 缓存玩家列表
                        case PlayerList -> {
                            PlayerList playerList = gson.fromJson(packet.data, PlayerList.class);
                            SocketData.playerList.put(address, playerList);
                        }
                        // Http信息返回
                        case HttpPacket -> {
                            HttpPacket httpPacket = gson.fromJson(packet.data, HttpPacket.class);
                            var socketWait = socketDataWaitList.get(packet.packetID);
                            if (socketWait == null) {
                                mLogger.error("[Mojo Console] HttpPacket: {} not found", packet.packetID);
                                return;
                            }
                            socketWait.setData(httpPacket);
                            socketDataWaitList.remove(packet.packetID);
                        }
                        case OtpPacket -> {
                            OtpPacket otpPacket = gson.fromJson(packet.data, OtpPacket.class);
                            if (otpPacket.remove) {
                                SocketData.tickets.remove(otpPacket.otp);
                            } else {
                                SocketData.tickets.put(otpPacket.otp, otpPacket);
                            }
                        }
                        // 心跳包
                        case HeartBeat -> {
                            clientTimeout.put(address, 0);
                        }
                    }
                } catch (Throwable e) {
                    e.printStackTrace();
                    mLogger.error("[Mojo Console] Client {} disconnect.", address);
                    clientList.remove(address);
                    clientTimeout.remove(address);
                    SocketData.playerList.remove(address);
                    return;
                }
            }
        }
    }

    // 等待客户端连接
    private static class WaitClientConnect extends Thread {
        ServerSocket socketServer;

        public WaitClientConnect(int port) throws IOException {
            socketServer = new ServerSocket(port);
            start();
        }

        @Override
        public void run() {
            mLogger.info("[Mojo Console] Start socket server on port " + socketServer.getLocalPort());
            // noinspection InfiniteLoopStatement
            while (true) {
                try {
                    Socket accept = socketServer.accept();
                    String address = accept.getInetAddress() + ":" + accept.getPort();
                    mLogger.info("[Mojo Console] Client connect: " + address);
                    new ClientThread(accept);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
