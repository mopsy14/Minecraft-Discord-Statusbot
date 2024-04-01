package mopsy.productions.discord.statusbot;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import org.simpleyaml.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.Arrays;
import java.util.List;

public class StatusbotMain implements ModInitializer {

    private String[] prevPlayers = {};

    private void initAll(){
        ConfigManager.init(this);
        DataManager.getAllData(this);
    }
    void addConfigDefaults(YamlConfiguration configuration){

    }
    String getConfigPath(){
        return System.getProperty("user.dir") + File.separator + "config";
    }
    public void onBotReady(){


    @Override
    public void onInitialize() {
        initAll();

        ServerLifecycleEvents.SERVER_STARTED.register(server->{
            prevPlayers = server.getPlayerNames();
            BotManger.regBot(
                    ConfigManager.configuration.getString("bot_token"),
                    Parser.createStatusMessage(List.of(prevPlayers))
            );
        });
        ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
            if (BotManger.jda != null) {
                BotManger.jda.shutdownNow();
            }
        });
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            if(!Arrays.equals(server.getPlayerNames(), prevPlayers)){
                prevPlayers = server.getPlayerNames();
                BotManger.regBot(
                        ConfigManager.configuration.getString("bot_token"),
                        Parser.createStatusMessage(List.of(prevPlayers))
                );
            }
        });
    }
    }
}
