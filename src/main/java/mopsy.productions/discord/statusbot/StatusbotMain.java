package mopsy.productions.discord.statusbot;

import net.minecraft.server.MinecraftServer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedOutEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.simpleyaml.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Mod("statusbot")
public class StatusbotMain {
    private MinecraftServer server = null;
    private void initAll(){
        ConfigManager.init(this);
        DataManager.getAllData(this);
    }
    void addConfigDefaults(YamlConfiguration configuration){

    }
    String getConfigPath(){
        if(server != null && server.isDedicatedServer()){
            return server.getServerDirectory().getAbsolutePath() + File.separator + "config";
        }
        return "";
    }
    public void onBotReady(){


    public StatusbotMain() {
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void start(final ServerStartedEvent event) {
        server = event.getServer();
        initAll();



        BotManger.regBot(
                ConfigManager.configuration.getString("bot_token"),
                Parser.createStatusMessage(Arrays.asList(server.getPlayerNames()))
        );
    }

    @SubscribeEvent
    public void stop(final ServerStoppingEvent event){
        if(BotManger.jda != null)
            BotManger.jda.shutdownNow();
    }

    @SubscribeEvent
    public void joined(final PlayerLoggedInEvent event) {
        BotManger.regBot(
                ConfigManager.configuration.getString("bot_token"),
                Parser.createStatusMessage(Arrays.asList(server.getPlayerNames()))
        );
    }

    @SubscribeEvent
    public void left(final PlayerLoggedOutEvent event) {
        BotManger.regBot(
                ConfigManager.configuration.getString("bot_token"),
                Parser.createStatusMessage(makeStringList(server.getPlayerNames(), event.getEntity().getName().getString()))
        );
    }

    private List<String> makeStringList(String[] players, String excluded){
        List<String> res = new ArrayList<>(players.length-1);
        for(String player : players){
            if(!player.equals(excluded)){
                res.add(player);
            }
        }
        return res;
    }

    }
}
