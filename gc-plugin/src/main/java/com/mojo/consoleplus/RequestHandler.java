package com.mojo.consoleplus;

import com.mojo.consoleplus.command.PluginCommand;
import com.mojo.consoleplus.forms.RequestAuth;
import com.mojo.consoleplus.forms.RequestJson;
import com.mojo.consoleplus.forms.ResponseAuth;
import com.mojo.consoleplus.forms.ResponseJson;
import emu.grasscutter.Grasscutter;
import emu.grasscutter.command.CommandMap;
import emu.grasscutter.game.player.Player;
import emu.grasscutter.server.http.Router;
import emu.grasscutter.utils.MessageHandler;
import io.javalin.Javalin;
import io.javalin.http.Context;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Map;
import java.util.Random;

import static java.lang.Integer.parseInt;
import static java.lang.Long.parseLong;


public final class RequestHandler implements Router {
	// private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    @Override public void applyRoutes(Javalin javalin) {
        javalin.post("/mojoplus/api", RequestHandler::processRequest);
        javalin.post("/mojoplus/auth", RequestHandler::requestKey);
    }


	public static void processRequest(Context context) {
        RequestJson request = context.bodyAsClass(RequestJson.class);
        Player player = null;

        if (request.k2 != null) { // version 2 token
            int uid; 
            long expire;
            String hashDigest;
            uid = parseInt(request.k2.split(":")[0]);
            expire = parseLong(request.k2.split(":")[1]);
            hashDigest = request.k2.split(":")[2];
            if (ConsolePlus.authHandler.auth(uid, expire, hashDigest)){
                Map<Integer, Player> playersMap = Grasscutter.getGameServer().getPlayers();
                for (int playerid: playersMap.keySet()) {
                    if (playersMap.get(playerid).getUid() == uid) {
                        player = playersMap.get(playerid);
                    }
                }
            }
        }

        if (player != null) {
            MessageHandler resultCollector = new MessageHandler();
            player.setMessageHandler(resultCollector); // hook the message
            switch (request.request){
                case "invoke":
                    try{
                        // TODO: Enable execut commands to third party
                        CommandMap.getInstance().invoke(player, player, request.payload);
                    } catch (Exception e) {
                        context.json(new ResponseJson("error", 500, e.getStackTrace().toString()));
                        break;
                    }
                case "ping":
                    // res.json(new ResponseJson("success", 200));
                    context.json(new ResponseJson("success", 200, resultCollector.getMessage()));
                    break;
                default:
                    context.json(new ResponseJson("400 Bad Request", 400));
                    break;
            }
            player.setMessageHandler(null);
            return;
        }

        context.json(new ResponseJson("403 Forbidden", 403));
    }

    public static void requestKey(Context context) throws IOException {
        RequestAuth request = context.bodyAsClass(RequestAuth.class);
        if (request.otp != null && !request.otp.equals("")) {
            if (PluginCommand.getInstance().tickets.get(request.otp) == null) {
                context.json(new ResponseAuth(404, "Not found", null));
                return;
            }
            String key = PluginCommand.getInstance().tickets.get(request.otp).key;
            if (key == null){
                context.json(new ResponseAuth(403, "Not ready yet", null));
            } else {
                PluginCommand.getInstance().tickets.remove(request.otp);
                context.json(new ResponseAuth(200, "", key));
            }
        } else if (request.uid != 0) {
            String otp = new DecimalFormat("000000").format(new Random().nextInt(999999));
            while (PluginCommand.getInstance().tickets.containsKey(otp)){
                otp = new DecimalFormat("000000").format(new Random().nextInt(999999));
            }
            Map<Integer, Player> playersMap = Grasscutter.getGameServer().getPlayers();
            Player targetPlayer = null;
            for (int playerid: playersMap.keySet()) {
                if (playersMap.get(playerid).getUid() == request.uid) {
                    targetPlayer = playersMap.get(playerid);
                }
            }
            if (targetPlayer == null){
                context.json(new ResponseAuth(404, "Not found", null));
                return;
            }
            PluginCommand.getInstance().tickets.put(otp, new PluginCommand.Ticket(targetPlayer, System.currentTimeMillis()/ 1000 + 300, true));
            context.json(new ResponseAuth(201, "Code generated", otp));
        }
    }
}
