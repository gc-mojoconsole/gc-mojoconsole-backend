package com.mojo.consoleplus;

import static java.lang.Integer.parseInt;
import static java.lang.Long.parseLong;

import java.io.IOException;
import java.util.Map;

import emu.grasscutter.Grasscutter;
import emu.grasscutter.command.CommandMap;
import emu.grasscutter.database.DatabaseHelper;
import emu.grasscutter.game.Account;
import emu.grasscutter.game.player.Player;
import emu.grasscutter.utils.MessageHandler;
import express.http.Request;
import express.http.Response;
import express.Express;

import emu.grasscutter.server.http.Router;
import io.javalin.Javalin;


// import com.google.gson.Gson;
// import com.google.gson.GsonBuilder;
import com.mojo.consoleplus.forms.RequestJson;
import com.mojo.consoleplus.forms.ResponseJson;


public final class RequestHandler implements Router {
	// private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    @Override public void applyRoutes(Express app, Javalin handle) {
        app.post("/mojoplus/api", RequestHandler::processRequest);
    }


	public static void processRequest(Request req, Response res) throws IOException {
        RequestJson request = req.body(RequestJson.class);
        res.type("application/json");
        Player player = null;
        if (request.k != null) { // version 1 token
            Account account = DatabaseHelper.getAccountBySessionKey(request.k);
            Map<Integer, Player> playersMap = Grasscutter.getGameServer().getPlayers();
            // String invokeResult = "";
            if (account != null) {
                for (int playerid: playersMap.keySet()) {
                    if (playersMap.get(playerid).getUid() == account.getPlayerUid()) {
                        player = playersMap.get(playerid);
                    }
                }
            }
        }

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
                        res.json(new ResponseJson("error", 500, e.getStackTrace().toString()));
                        break;
                    }
                case "ping":
                    // res.json(new ResponseJson("success", 200));
                    res.json(new ResponseJson("success", 200, resultCollector.getMessage()));
                    break;
                default:
                    res.json(new ResponseJson("400 Bad Request", 400));
                    break;
            }
            player.setMessageHandler(null);
            return;
        }

        res.json(new ResponseJson("403 Forbidden", 403));
    }
}
