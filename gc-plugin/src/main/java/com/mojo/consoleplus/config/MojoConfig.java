package com.mojo.consoleplus.config;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import emu.grasscutter.Grasscutter;

public class MojoConfig {

    public boolean UseCDN = false;
    public String CDNLink = "https://gc-mojoconsole.github.io/";
    public String interfacePath = "/mojoplus/console.html";
    public String responseMessage = "[MojoConsole] Link sent to your mailbox, check it!";
    
    static public class MailTemplate {
        public String title = "Mojo Console Link";
        public String author = "Mojo Console";
        public String content = "Here is your mojo console link: {{ LINK }}\n" +
        "Note that the link will <b>expire</b> in some time, you may retrieve a new one after that.";
    };

    public MailTemplate mail = new MailTemplate();

    static String getConfigPath(){
        try{
            String result = new File(MojoConfig.class.getProtectionDomain().getCodeSource().getLocation()
            .toURI()).getParent() + "/mojoconfig.json";
            return result;
        } catch (Exception e){
            e.printStackTrace();
            return "";
        }
    }

    public static MojoConfig loadConfig(){
        String configPath = getConfigPath();
        File configFile = new File(configPath);
        Gson gson = new Gson();

        try{
            String s = Files.readString(configFile.toPath(), StandardCharsets.UTF_8);
            return gson.fromJson(s, MojoConfig.class);     
        } catch (Exception e) {
            MojoConfig config = new MojoConfig();
            config.saveConfig();
            return config;
        }
    }

    public void saveConfig(){
        String configPath = getConfigPath();
        File configFile = new File(configPath);
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        try{
            Files.writeString(configFile.toPath(), gson.toJson(this, MojoConfig.class));            
        } catch (Exception e) {
            e.printStackTrace();
            Grasscutter.getLogger().error("[Mojoconsole] Config save failed!");
        }
    }
}
