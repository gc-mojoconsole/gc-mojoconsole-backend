package com.mojo.consoleplus.command;

import java.net.URLEncoder;
import java.util.List;

import emu.grasscutter.Grasscutter;
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
        public String k; // session key
        public String d; // mojo backend url
    }
    @Override
    public void execute(Player sender, Player targetPlayer, List<String> args) {
        Mail mail = new Mail();
        String link = getServerURL(targetPlayer.getAccount().getSessionKey());
        String link_type = "webview";
        Grasscutter.getLogger().info(link);
        if (args.size() > 0 && args.get(0).equals("o")) {
            link_type = "browser";
        }

        mail.mailContent.title = ConsolePlus.config.mail.title;
        mail.mailContent.sender = ConsolePlus.config.mail.author;
        mail.mailContent.content = ConsolePlus.config.mail.content.replace("{{ LINK }}", "<type=\""+ link_type + "\" text=\"Mojo Console\" href=\"" + link + "\"/>");
        mail.expireTime = System.currentTimeMillis() / 1000 + 3600 * ConsolePlus.config.mail.expireHour;
        targetPlayer.sendMail(mail);
        CommandHandler.sendMessage(sender, ConsolePlus.config.responseMessage);
    }

    private static String getServerURL(String sessionKey) {
        if (ConsolePlus.config.UseCDN){
            Gson gson = new Gson();
            HashParams hp = new HashParams();
            hp.k = sessionKey;
            hp.d = getMojoBackendURL();
            try{
                return ConsolePlus.config.CDNLink + "#" + URLEncoder.encode(gson.toJson(hp), "utf-8");
            } catch (Exception e){
                e.printStackTrace();
                return ConsolePlus.config.CDNLink +  "?k=" + sessionKey;
            }
        } else {
            return getMojoBackendURL() + ConsolePlus.config.interfacePath + "?k=" + sessionKey;
        }
    }

    private static String getMojoBackendURL() {
        return "http" + (HTTP_ENCRYPTION.useEncryption ? "s" : "") + "://"
        + lr(HTTP_INFO.accessAddress, HTTP_INFO.bindAddress) + ":"
        + lr(HTTP_INFO.accessPort, HTTP_INFO.bindPort);
    }
}
