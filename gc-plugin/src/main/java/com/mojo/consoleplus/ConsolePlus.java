package com.mojo.consoleplus;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mojo.consoleplus.command.PluginCommand;
import com.mojo.consoleplus.config.MojoConfig;
import com.mojo.consoleplus.socket.SocketClient;
import com.mojo.consoleplus.socket.SocketServer;
import emu.grasscutter.Grasscutter;
import emu.grasscutter.command.CommandMap;
import emu.grasscutter.plugin.Plugin;
import emu.grasscutter.plugin.PluginConfig;
import emu.grasscutter.server.event.EventHandler;
import emu.grasscutter.server.event.HandlerPriority;
import emu.grasscutter.server.event.player.PlayerJoinEvent;
import emu.grasscutter.server.event.player.PlayerQuitEvent;
import io.javalin.http.staticfiles.Location;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;

import static emu.grasscutter.config.Configuration.HTTP_POLICIES;
import static emu.grasscutter.config.Configuration.PLUGIN;

public class ConsolePlus extends Plugin {
    public static MojoConfig config = MojoConfig.loadConfig();
    public static String versionTag;
    public static AuthHandler authHandler;
    public static Gson gson = new GsonBuilder().setPrettyPrinting().create();

    @Override
    public void onLoad() {
        try (InputStream in = getClass().getResourceAsStream("/plugin.json");
             BufferedReader reader = new BufferedReader(new InputStreamReader(in))) {
            Gson gson = new Gson();
            PluginConfig pluginConfig = gson.fromJson(reader, PluginConfig.class);
            this.getLogger().info("[MojoConsole] loaded!");
            versionTag = pluginConfig.version;
        } catch (Exception e) {
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
            Grasscutter.getHttpServer().getHandle()._conf.addStaticFiles(staticFileConfig -> {
                staticFileConfig.hostedPath = "/mojoplus";
                staticFileConfig.directory = folder_name;
                staticFileConfig.location = Location.EXTERNAL;
            });
        } else {
            if (!HTTP_POLICIES.cors.enabled) {
                Grasscutter.getLogger().error("[MojoConsole] You enabled the useCDN option, in this option, you have to configure Grasscutter accept CORS request. See `config.json`->`server`->`policies`->`cors`.");
                return;
            }
        }

        authHandler = new AuthHandler();

        if (Grasscutter.config.server.runMode == Grasscutter.ServerRunMode.DISPATCH_ONLY) {
            SocketServer.startServer(getLogger());
            Grasscutter.getHttpServer().addRouter(RequestOnlyHttpHandler.class);
        } else if (Grasscutter.config.server.runMode == Grasscutter.ServerRunMode.GAME_ONLY) {
            SocketClient.connectServer(getLogger());
            new EventHandler<>(PlayerJoinEvent.class)
                    .priority(HandlerPriority.HIGH)
                    .listener(EventListeners::onPlayerJoin)
                    .register(this);
            new EventHandler<>(PlayerQuitEvent.class)
                    .priority(HandlerPriority.HIGH)
                    .listener(EventListeners::onPlayerQuit)
                    .register(this);
        } else {
            Grasscutter.getHttpServer().addRouter(RequestHandler.class);
        }
        CommandMap.getInstance().registerCommand("mojoconsole", new PluginCommand());
        this.getLogger().info("[MojoConsole] enabled. Version: " + versionTag);
    }

    @Override
    public void onDisable() {
        CommandMap.getInstance().unregisterCommand("mojoconsole");
        this.getLogger().info("[MojoConsole] Mojoconsole Disabled");
    }

}

