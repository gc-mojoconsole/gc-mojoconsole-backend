package com.mojo.consoleplus;

import emu.grasscutter.Grasscutter;
import emu.grasscutter.command.CommandMap;
import emu.grasscutter.plugin.Plugin;

import com.mojo.consoleplus.command.PluginCommand;

import express.Express;

public class ConsolePlus extends Plugin{
    @Override
    public void onLoad() {

    }

    @Override
    public void onEnable() {
        Express app = Grasscutter.getDispatchServer().getServer();
        app.post("/mojoplus/api", new RequestHandler());

        CommandMap.getInstance().registerCommand("mojoconsole", new PluginCommand());
        Grasscutter.getLogger().info("mojo console enabled");
    }

    @Override
    public void onDisable() {
        CommandMap.getInstance().unregisterCommand("mojoconsole");
        Grasscutter.getLogger().info("[GCGM] GCGM Disabled");
    }

}

