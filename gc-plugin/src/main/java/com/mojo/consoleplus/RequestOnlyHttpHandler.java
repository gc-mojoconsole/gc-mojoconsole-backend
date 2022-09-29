package com.mojo.consoleplus;

import com.mojo.consoleplus.command.PluginCommand;
import com.mojo.consoleplus.forms.RequestAuth;
import com.mojo.consoleplus.forms.RequestJson;
import com.mojo.consoleplus.forms.ResponseAuth;
import com.mojo.consoleplus.forms.ResponseJson;
import com.mojo.consoleplus.socket.SocketData;
import com.mojo.consoleplus.socket.SocketDataWait;
import com.mojo.consoleplus.socket.SocketServer;
import com.mojo.consoleplus.socket.packet.HttpPacket;
import com.mojo.consoleplus.socket.packet.OtpPacket;
import com.mojo.consoleplus.socket.packet.player.Player;
import com.mojo.consoleplus.socket.packet.player.PlayerEnum;
import emu.grasscutter.server.http.Router;
import io.javalin.Javalin;
import io.javalin.http.Context;

import java.text.DecimalFormat;
import java.util.Random;

import static java.lang.Integer.parseInt;
import static java.lang.Long.parseLong;


public final class RequestOnlyHttpHandler implements Router {
	// private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    @Override public void applyRoutes(Javalin javalin) {
        javalin.post("/mojoplus/api", RequestOnlyHttpHandler::processRequest);
        javalin.post("/mojoplus/auth", RequestOnlyHttpHandler::requestKey);
    }


	public static void processRequest(Context context) {
        RequestJson request = context.bodyAsClass(RequestJson.class);
        String player = null;
        int uid = -1;

        if (request.k2 != null) { // version 2 token
            long expire;
            String hashDigest;
            uid = parseInt(request.k2.split(":")[0]);
            expire = parseLong(request.k2.split(":")[1]);
            hashDigest = request.k2.split(":")[2];
            if (ConsolePlus.authHandler.auth(uid, expire, hashDigest)){
                player = SocketData.getPlayer(uid);
            }
        }

        if (player != null) {
            SocketDataWait<HttpPacket> wait = null;
            switch (request.request){
                case "invoke":
                    wait = new SocketDataWait<>(2000) {
                        @Override
                        public void run() {}
                        @Override
                        public HttpPacket initData(HttpPacket data) {
                            return data;
                        }

                        @Override
                        public void timeout() {
                            context.json(new ResponseJson("timeout", 500));
                        }
                    };
                    try{
                        // TODO: Enable execut commands to third party
                        Player p = new Player();
                        p.type = PlayerEnum.RunCommand;
                        p.uid = uid;
                        p.data = request.payload;
                        SocketServer.sendUidPacket(uid, p, wait);
                    } catch (Exception e) {
                        context.json(new ResponseJson("error", 500, e.getStackTrace().toString()));
                        break;
                    }
                case "ping":
                    // res.json(new ResponseJson("success", 200));
                    if (wait == null) {
                        context.json(new ResponseJson("success", 200, null));
                    } else {
                        var data = wait.getData();
                        if (data == null) {
                            context.json(new ResponseJson("timeout", 500));
                        } else {
                            context.json(new ResponseJson(data.message, data.code, data.data));
                        }
                    }
                    break;
                default:
                    context.json(new ResponseJson("400 Bad Request", 400));
                    break;
            }
            return;
        }

        context.json(new ResponseJson("403 Forbidden", 403));
    }

    public static void requestKey(Context context) {
        RequestAuth request = context.bodyAsClass(RequestAuth.class);
        if (request.otp != null && !request.otp.equals("")) {
            if (PluginCommand.getInstance().tickets.get(request.otp) == null) {
                context.json(new ResponseAuth(404, "Not found", null));
                return;
            }
            String key = SocketData.tickets.get(request.otp).key;
            if (key == null){
                context.json(new ResponseAuth(403, "Not ready yet", null));
            } else {
                SocketData.tickets.remove(request.otp);
                context.json(new ResponseAuth(200, "", key));
            }
        } else if (request.uid != 0) {
            String otp = new DecimalFormat("000000").format(new Random().nextInt(999999));
            while (PluginCommand.getInstance().tickets.containsKey(otp)){
                otp = new DecimalFormat("000000").format(new Random().nextInt(999999));
            }
            String targetPlayer = SocketData.getPlayer(request.uid);
            if (targetPlayer == null){
                context.json(new ResponseAuth(404, "Not found", null));
                return;
            }
            var otpPacket = new OtpPacket(request.uid, otp, System.currentTimeMillis() / 1000 + 300, true);
            if (!SocketServer.sendPacket(SocketData.getPlayerInServer(request.uid), otpPacket)) {
                context.json(new ResponseAuth(500, "Send otp to server failed.", null));
                return;
            }
            SocketData.tickets.put(otp, otpPacket);
            context.json(new ResponseAuth(201, "Code generated", otp));
        }
    }
}
