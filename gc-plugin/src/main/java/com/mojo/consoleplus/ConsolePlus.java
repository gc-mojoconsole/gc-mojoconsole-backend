package com.mojo.consoleplus;

import emu.grasscutter.Grasscutter;
import emu.grasscutter.command.CommandMap;
import emu.grasscutter.plugin.Plugin;

import java.io.File;

import com.mojo.consoleplus.command.PluginCommand;

import express.Express;
import io.javalin.http.staticfiles.Location;
import static emu.grasscutter.Configuration.PLUGINS_FOLDER;

public class ConsolePlus extends Plugin{
    @Override
    public void onLoad() {

    }

    @Override
    public void onEnable() {
        Express app = Grasscutter.getDispatchServer().getServer();
        String folder_name = PLUGINS_FOLDER + "/mojoconsole/";
        File folder = new File(folder_name);
        if (!folder.exists()) {
            Grasscutter.getLogger().warn("To make mojo console works, you have to put your frontend file(console.html) inside" + folder.getAbsolutePath());
            folder.mkdirs();
        }
        app.post("/mojoplus/api", new RequestHandler());
        app.raw().config.addStaticFiles("/mojoplus", folder_name, Location.EXTERNAL);
        CommandMap.getInstance().registerCommand("mojoconsole", new PluginCommand());
        Grasscutter.getLogger().info("mojo console enabled");
    }

    @Override
    public void onDisable() {
        CommandMap.getInstance().unregisterCommand("mojoconsole");
        Grasscutter.getLogger().info("[MCP] MCP Disabled");
    }

}

