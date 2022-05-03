package com.mojo.consoleplus.command;

import java.util.List;

import emu.grasscutter.Grasscutter;
import emu.grasscutter.command.Command;
import emu.grasscutter.command.CommandHandler;
import emu.grasscutter.game.mail.Mail;
import emu.grasscutter.game.player.Player;

@Command(label = "mojoconsole", usage = "mojoconsole", description = "Generate Mojoconsole link (no arguments required)", aliases = {
        "mojo" }, permission = "mojo.console")
public class PluginCommand implements CommandHandler {
    @Override
    public void execute(Player sender, List<String> args) {
        Mail mail = new Mail();
        String link = getServerURL(sender.getAccount().getSessionKey());
        Grasscutter.getLogger().info(link);
        mail.mailContent.content = "Here is your mojo console link: " +
                "<type=\"webview\" text=\"Mojo Console\" href=\"" + link + "\"/>" +
                "Note that the link will <b>expire</b> in some time, you may retrieve a new one after that.";
        sender.sendMail(mail);
        CommandHandler.sendMessage(sender, "[MojoConsole] Link sent, check your mailbox");
    }

    private static String getServerURL(String sessionKey) {
        return "http" + (Grasscutter.getConfig().getDispatchOptions().FrontHTTPS ? "s" : "") + "://" +
                (Grasscutter.getConfig().getDispatchOptions().PublicIp.isEmpty()
                        ? Grasscutter.getConfig().getDispatchOptions().Ip
                        : Grasscutter.getConfig().getDispatchOptions().PublicIp)
                +
                ":"
                + (Grasscutter.getConfig().getDispatchOptions().PublicPort != 0
                        ? Grasscutter.getConfig().getDispatchOptions().PublicPort
                        : Grasscutter.getConfig().getDispatchOptions().Port) + "/gcstatic/mojo/console.html?k=" + sessionKey;
    }
}
