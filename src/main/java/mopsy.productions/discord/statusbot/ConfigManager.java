package mopsy.productions.discord.statusbot;

import org.simpleyaml.configuration.file.YamlFile;
import org.simpleyaml.configuration.implementation.api.QuoteStyle;

import java.io.File;
import java.io.IOException;

public class ConfigManager {
    public static boolean initialized = false;
    static File configFile;
    public static YamlFile configuration;

    public static void init(StatusbotMain statusbotMain){
        configFile = new File(statusbotMain.getConfigPath() + File.separator +"statusbot_config.yml");
        try {
            configuration = new YamlFile(configFile);
            ConfigManager.configuration.options().quoteStyleDefaults().setDefaultQuoteStyle(QuoteStyle.SINGLE);
            configuration.createOrLoadWithComments();

            createDefaults(statusbotMain);
            configuration.save();

            initialized = true;
        }catch (IOException ioException){
            System.out.println("Statusbot: Something went wrong while creating the config file!");
            ioException.printStackTrace();
        }
    }
    private static void createDefaults(StatusbotMain statusbotMain){
        configuration.addDefault("bot_token","enter token here");
        configuration.addDefault("status_mode","playing");
        configuration.addDefault("player_separator_text",", ");
        configuration.addDefault("status_message","$AOP$ player(s) online: $PL$");
        configuration.addDefault("no_player_message","No one is online");
        configuration.addDefault("enable_direct_message_status_messages","true");
        configuration.addDefault("enable_text_channel_status_messages","true");
        statusbotMain.addConfigDefaults(configuration);
    }
    public static String getStr(String key){
        return configuration.getString(key);
    }
    public static boolean getBool(String key){
        return configuration.getString(key).equalsIgnoreCase("true");
    }



}
