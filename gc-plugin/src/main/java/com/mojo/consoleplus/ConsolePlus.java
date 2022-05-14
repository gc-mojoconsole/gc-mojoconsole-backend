package com.mojo.consoleplus;

import emu.grasscutter.Grasscutter;
import emu.grasscutter.command.CommandMap;
import emu.grasscutter.plugin.Plugin;

import java.io.File;

import com.mojo.consoleplus.command.PluginCommand;

import io.javalin.http.staticfiles.Location;
import static emu.grasscutter.Configuration.PLUGINS_FOLDER;
import static emu.grasscutter.Configuration.HTTP_POLICIES;

import com.mojo.consoleplus.config.MojoConfig;

public class ConsolePlus extends Plugin{
    public static MojoConfig config = MojoConfig.loadConfig();

    @Override
    public void onLoad() {
        Grasscutter.getLogger().info("[MojoConsole] loaded!");
    }

    @Override
    public void onEnable() {
        String folder_name = PLUGINS_FOLDER + "/mojoconsole/";
        File folder = new File(folder_name);
        if (!folder.exists()) {
            Grasscutter.getLogger().warn("To make mojo console works, you have to put your frontend file(console.html) inside" + folder.getAbsolutePath());
            folder.mkdirs();
        }
        if (!config.UseCDN) {
            Grasscutter.getHttpServer().getHandle().config.addStaticFiles("/mojoplus", folder_name, Location.EXTERNAL);
        } else {
            if (!HTTP_POLICIES.cors.enabled) {
                Grasscutter.getLogger().error("[MojoConsole] You enabled the useCDN option, in this option, you have to configure Grasscutter accept CORS request. See `config.json`->`server`->`policies`->`cors`.");
                return;
            }
        }
        Grasscutter.getHttpServer().addRouter(RequestHandler.class);
        CommandMap.getInstance().registerCommand("mojoconsole", new PluginCommand());
        Grasscutter.getLogger().info("[MojoConsole] enabled");

    }

    @Override
    public void onDisable() {
        CommandMap.getInstance().unregisterCommand("mojoconsole");
        Grasscutter.getLogger().info("[MojoConsole] Mojoconsole Disabled");
    }

}

