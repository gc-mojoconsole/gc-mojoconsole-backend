package com.mojo.consoleplus;

import emu.grasscutter.Grasscutter;
import emu.grasscutter.command.CommandMap;
import emu.grasscutter.plugin.Plugin;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;

import com.google.gson.Gson;
import com.mojo.consoleplus.command.PluginCommand;

import io.javalin.http.staticfiles.Location;
import emu.grasscutter.plugin.PluginConfig;
import static emu.grasscutter.Configuration.PLUGIN;
import static emu.grasscutter.Configuration.HTTP_POLICIES;

import com.mojo.consoleplus.config.MojoConfig;

public class ConsolePlus extends Plugin{
    public static MojoConfig config = MojoConfig.loadConfig();
    public static String versionTag;
    public static AuthHandler authHandler;

    @Override
    public void onLoad() {
        try (InputStream in = getClass().getResourceAsStream("/plugin.json");
           BufferedReader reader = new BufferedReader(new InputStreamReader(in))) {
            Gson gson = new Gson();
            PluginConfig pluginConfig = gson.fromJson(reader, PluginConfig.class);
            this.getLogger().info("[MojoConsole] loaded!");
            versionTag = pluginConfig.version;
        }
           catch (Exception e) {
               e.printStackTrace();
           }
    // Use resource
    }


    @Override
    public void onEnable() {
        String folder_name = PLUGIN("mojoconsole/");
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
        this.getLogger().info("[MojoConsole] enabled. Version: " + versionTag);
        authHandler = new AuthHandler();
    }

    @Override
    public void onDisable() {
        CommandMap.getInstance().unregisterCommand("mojoconsole");
        this.getLogger().info("[MojoConsole] Mojoconsole Disabled");
    }

}

