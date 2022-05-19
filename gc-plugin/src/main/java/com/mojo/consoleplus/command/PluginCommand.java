package com.mojo.consoleplus.command;

import java.net.URLEncoder;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import emu.grasscutter.command.Command;
import emu.grasscutter.command.CommandHandler;
import emu.grasscutter.game.mail.Mail;
import emu.grasscutter.game.player.Player;
import static emu.grasscutter.Configuration.*;

import com.mojo.consoleplus.ConsolePlus;
import com.google.gson.Gson;

@Command(label = "mojoconsole", usage = "mojoconsole", description = "Send Mojoconsole link via mail (by default it's in-game webview, but you may use argument `o` for popping out external browser)", aliases = {
        "mojo" }, permission = "mojo.console")
public class PluginCommand implements CommandHandler {
    static class HashParams{
        public String k2; // session key
        public String d; // mojo backend url
    }
    public static class Ticket {
        public Player sender;
        public Player targetPlayer;
        public long expire;
        public Boolean api = false; // from api
        public String key;

        public Ticket(Player sender2, Player targetPlayer2, long l) {
            sender = sender2;
            targetPlayer = targetPlayer2;
            expire = l;
        }
        public Ticket(Player targetPlayer, long expire, Boolean api) {
            this.sender = null;
            this.targetPlayer = targetPlayer;
            this.expire = expire;
            this.api = api;
        }
    }
    public HashMap<String, Ticket> tickets = new HashMap<String, Ticket>();
    public static PluginCommand instance;

    public PluginCommand(){
        instance = this;
    }

    @Override
    public void execute(Player sender, Player targetPlayer, List<String> args) {
        if (sender != targetPlayer){
            String otp = new DecimalFormat("000000").format(new Random().nextInt(999999));
            while (tickets.containsKey(otp)){
                otp = new DecimalFormat("000000").format(new Random().nextInt(999999));
            }
            CommandHandler.sendMessage(sender, ConsolePlus.config.responseMessageThird.replace("{{OTP}}", otp));
            flushTicket();
            tickets.put(otp, new Ticket(sender, targetPlayer, System.currentTimeMillis()/ 1000 + 300));
            return;
        }
        String link_type = "webview";
        if (args.size() > 0) {
            if (args.get(0).equals("o")){
                link_type = "browser";
            } else {
                String otp = args.get(0);
                Ticket resolved;
                Boolean valid = false;
                if (tickets.containsKey(otp)) {
                    resolved = tickets.get(otp);
                    if (sender == resolved.targetPlayer && resolved.expire > System.currentTimeMillis() / 1000){
                        sender = resolved.sender;
                        targetPlayer = resolved.targetPlayer;
                        valid = true;
                        CommandHandler.sendMessage(targetPlayer, ConsolePlus.config.responseMessageSuccess);
                        if (resolved.api == false) {
                            tickets.remove(otp);
                        }
                    }
                }
                if (!valid){
                    CommandHandler.sendMessage(sender, ConsolePlus.config.responseMessageError);
                    return;
                }
            }
        }
        String authKey = ConsolePlus.authHandler.genKey(targetPlayer.getUid(), System.currentTimeMillis() / 1000 + ConsolePlus.config.mail.expireHour * 3600);
        String link = getServerURL(authKey);
        // Grasscutter.getLogger().info(link);

        if (sender != null) {
            Mail mail = new Mail();
            mail.mailContent.title = ConsolePlus.config.mail.title;
            mail.mailContent.sender = ConsolePlus.config.mail.author;
            mail.mailContent.content = ConsolePlus.config.mail.content.replace("{{ LINK }}", "<type=\""+ link_type + "\" text=\"Mojo Console\" href=\"" + link + "\"/>");
            mail.expireTime = System.currentTimeMillis() / 1000 + 3600 * ConsolePlus.config.mail.expireHour;
            sender.sendMail(mail);
            CommandHandler.sendMessage(sender, ConsolePlus.config.responseMessage);
        } else {
            tickets.get(args.get(0)).key = authKey;
        }

    }

    private static String getServerURL(String sessionKey) {
        if (ConsolePlus.config.UseCDN){
            Gson gson = new Gson();
            HashParams hp = new HashParams();
            hp.k2 = sessionKey;
            hp.d = getMojoBackendURL();
            try {
                sessionKey = URLEncoder.encode(sessionKey, "utf-8");
            } catch (Exception e) {
                e.printStackTrace();
            }
            try{
                return ConsolePlus.config.CDNLink + "#" + URLEncoder.encode(gson.toJson(hp), "utf-8");
            } catch (Exception e){
                e.printStackTrace();
                return ConsolePlus.config.CDNLink +  "?k2=" + sessionKey;
            }
        } else {
            return getMojoBackendURL() + ConsolePlus.config.interfacePath + "?k2=" + sessionKey;
        }
    }

    private static String getMojoBackendURL() {
        return "http" + (HTTP_ENCRYPTION.useEncryption ? "s" : "") + "://"
        + lr(HTTP_INFO.accessAddress, HTTP_INFO.bindAddress) + ":"
        + lr(HTTP_INFO.accessPort, HTTP_INFO.bindPort);
    }

    private void flushTicket()  {
        Long curtime = System.currentTimeMillis() / 1000;
        for (String otp : tickets.keySet()) {
            if (curtime > tickets.get(otp).expire) {
                tickets.remove(otp);
            }
        }
    }

    public static PluginCommand getInstance() {
        return instance;
    }
}
