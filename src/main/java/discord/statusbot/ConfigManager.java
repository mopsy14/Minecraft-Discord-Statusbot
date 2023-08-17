package src.main.java.discord.statusbot;

import org.simpleyaml.configuration.file.YamlFile;

import java.io.File;
import java.io.IOException;
import java.util.Map;

public class ConfigManager {
    public static boolean initialized = false;
    static File configFile;
    public static YamlFile configuration;

    public static void init(StatusbotMain statusbotMain){
        configFile = new File(statusbotMain.getConfigPath() + File.separator +"statusbot_config.yml");
        try {
            configuration = new YamlFile(configFile);
            configuration.createOrLoadWithComments();
            createDefaults(statusbotMain);

            initialized = true;
        }catch (IOException ioException){
            System.out.println("§4 Statusbot: Something went wrong while creating the config file!");
            ioException.printStackTrace();
        }
    }
    private static void createDefaults(StatusbotMain statusbotMain){
        configuration.addDefaults(Map.of(
                "bot_token","enter token here",
                "status_mode","playing",
                "player_separator_text",", ",
                "status_message","$AOP$ Players online: $PL$",
                "no_player_message","No one is online"
        ));
        configuration.addDefaults(statusbotMain.getConfigDefaults());
    }



}
