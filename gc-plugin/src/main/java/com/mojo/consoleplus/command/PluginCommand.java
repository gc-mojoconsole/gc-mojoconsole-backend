package com.mojo.consoleplus.command;

import java.util.List;

import emu.grasscutter.Grasscutter;
import emu.grasscutter.command.Command;
import emu.grasscutter.command.CommandHandler;
import emu.grasscutter.game.mail.Mail;
import emu.grasscutter.game.player.Player;
import static emu.grasscutter.Configuration.*;

@Command(label = "mojoconsole", usage = "mojoconsole", description = "Send Mojoconsole link via mail (by default it's in-game webview, but you may use argument `o` for popping out external browser)", aliases = {
        "mojo" }, permission = "mojo.console")
public class PluginCommand implements CommandHandler {
    @Override
    public void execute(Player sender, Player targetPlayer, List<String> args) {
        Mail mail = new Mail();
        String link = getServerURL(targetPlayer.getAccount().getSessionKey());
        String link_type = "webview";
        Grasscutter.getLogger().info(link);
        if (args.size() > 0 && args.get(0).equals("o")) {
            link_type = "browser";
        }

        mail.mailContent.title = "MojoConsole";
        mail.mailContent.sender = "MojoConsolePlus";
        mail.mailContent.content = "Here is your mojo console link: " +
                "<type=\""+ link_type + "\" text=\"Mojo Console\" href=\"" + link + "\"/>" +
                "Note that the link will <b>expire</b> in some time, you may retrieve a new one after that.";
        targetPlayer.sendMail(mail);
        CommandHandler.sendMessage(sender, "[MojoConsole] Link sent, check your mailbox");
    }

    private static String getServerURL(String sessionKey) {
        return "http" + (DISPATCH_ENCRYPTION.useEncryption ? "s" : "") + "://"
        + lr(DISPATCH_INFO.accessAddress, DISPATCH_INFO.bindAddress) + ":"
        + lr(DISPATCH_INFO.accessPort, DISPATCH_INFO.bindPort) + "/mojoplus/console.html?k=" + sessionKey;
    }
}
