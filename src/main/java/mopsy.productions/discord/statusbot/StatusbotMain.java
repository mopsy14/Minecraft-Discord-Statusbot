package mopsy.productions.discord.statusbot;

import net.minecraft.server.MinecraftServer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedOutEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.server.FMLServerStartedEvent;
import net.minecraftforge.fml.event.server.FMLServerStoppingEvent;
import org.simpleyaml.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.Arrays;

@Mod("discord_statusbot")
public class StatusbotMain {
    private MinecraftServer server = null;
    private void initAll(){
        ConfigManager.init(this);
    }
    void addConfigDefaults(YamlConfiguration configuration){

    }
    String getConfigPath(){
        if(server != null && server.isDedicatedServer()){
            return server.getServerDirectory().getAbsolutePath() + File.separator + "config";
        }
        return "";
    }


    public StatusbotMain() {
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void start(final FMLServerStartedEvent event) {
        server = event.getServer();
        initAll();



        BotManger.regBot(
                ConfigManager.configuration.getString("bot_token"),
                Parser.createStatusMessage(Arrays.asList(event.getServer().getPlayerNames()))
        );
    }

    @SubscribeEvent
    public void stop(final FMLServerStoppingEvent event){
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
                Parser.createStatusMessage(Arrays.asList(server.getPlayerNames()))
        );
    }

}
