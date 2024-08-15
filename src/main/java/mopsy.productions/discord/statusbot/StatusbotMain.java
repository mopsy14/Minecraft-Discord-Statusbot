package mopsy.productions.discord.statusbot;

import net.dv8tion.jda.api.entities.channel.concrete.PrivateChannel;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
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
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Mod("discord_statusbot")
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
        if(BotManager.jda!=null) {
            if (ConfigManager.getBool("enable_server_start_messages")) {
                String startMessage = Parser.createStartMessage();
                if (ConfigManager.getBool("enable_text_channel_status_messages")) {
                    for (long id : BotManager.messageTextChannels) {
                        TextChannel channel = BotManager.jda.getTextChannelById(id);
                        if (channel != null)
                            channel.sendMessage(startMessage).queue();
                    }
                }
                if (ConfigManager.getBool("enable_direct_message_status_messages")) {
                    for (UserChannelPair id : BotManager.messagePrivateChannels) {
                        PrivateChannel channel = BotManager.jda.getPrivateChannelById(id.channel);
                        if (channel != null)
                            channel.sendMessage(startMessage).queue();
                    }
                }
            }
        }
    }

    public StatusbotMain() {
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void start(final FMLServerStartedEvent event) {
        server = event.getServer();
        initAll();



        BotManager.regBot(
                ConfigManager.configuration.getString("bot_token"),
                Parser.createStatusMessage(()->Arrays.asList(server.getPlayerNames()),server.getPlayerNames().length),
                this
        );
    }

    @SubscribeEvent
    public void stop(final FMLServerStoppingEvent event){
        if(BotManager.jda!=null){
            if (ConfigManager.getBool("enable_server_stop_messages")) {
                if (ConfigManager.getBool("enable_text_channel_status_messages")) {
                    String stopMessage = Parser.createStopMessage();
                    for (long id : BotManager.messageTextChannels) {
                        TextChannel channel = BotManager.jda.getTextChannelById(id);
                        if (channel != null)
                            channel.sendMessage(stopMessage).queue();
                    }
                }
                if (ConfigManager.getBool("enable_direct_message_status_messages")) {
                    String stopMessage = Parser.createStopMessage();
                    for (UserChannelPair id : BotManager.messagePrivateChannels) {
                        PrivateChannel channel = BotManager.jda.getPrivateChannelById(id.channel);
                        if (channel != null)
                            channel.sendMessage(stopMessage).queue();
                    }
                }
            }
        }
        DataManager.saveAllData(this);
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        if(BotManager.jda!=null){
            BotManager.jda.shutdown();

            try {
                if(!BotManager.jda.awaitShutdown(Duration.ofSeconds(10))){
                    BotManager.jda.shutdownNow();
                }
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

        }
    }

    @SubscribeEvent
    public void joined(final PlayerLoggedInEvent event) {
        BotManager.regBot(
                ConfigManager.configuration.getString("bot_token"),
                Parser.createStatusMessage(()->Arrays.asList(server.getPlayerNames()),server.getPlayerNames().length),
                this
        );
        if(BotManager.jda!=null) {
            if (ConfigManager.getBool("enable_server_join_messages")) {
                String joinMessage = Parser.createJoinMessage(
                        ()->Arrays.asList(server.getPlayerNames()),
                        event.getEntity().getName().getString(),
                        server.getPlayerNames().length
                );
                if (ConfigManager.getBool("enable_text_channel_status_messages")) {
                    for (long id : BotManager.messageTextChannels) {
                        TextChannel channel = BotManager.jda.getTextChannelById(id);
                        if (channel != null)
                            channel.sendMessage(joinMessage).queue();

                    }
                }
                if (ConfigManager.getBool("enable_direct_message_status_messages")) {
                    for (UserChannelPair id : BotManager.messagePrivateChannels) {
                        PrivateChannel channel = BotManager.jda.getPrivateChannelById(id.channel);
                        if (channel != null)
                            channel.sendMessage(joinMessage).queue();
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public void left(final PlayerLoggedOutEvent event) {
        BotManager.regBot(
                ConfigManager.configuration.getString("bot_token"),
                Parser.createStatusMessage(()->makeStringList(server.getPlayerNames(), event.getEntity().getName().getString()),server.getPlayerNames().length-1),
                this
        );
        if(BotManager.jda!=null) {
            if (ConfigManager.getBool("enable_server_leave_messages")) {
                String leaveMessage = Parser.createLeaveMessage(
                        ()->makeStringList(server.getPlayerNames(),event.getEntity().getName().getString()),
                        event.getEntity().getName().getString(),
                        server.getPlayerNames().length-1
                );
                if (ConfigManager.getBool("enable_text_channel_status_messages")) {
                    for (long id : BotManager.messageTextChannels) {
                        TextChannel channel = BotManager.jda.getTextChannelById(id);
                        if (channel != null)
                            channel.sendMessage(leaveMessage).queue();

                    }
                }
                if (ConfigManager.getBool("enable_direct_message_status_messages")) {
                    for (UserChannelPair id : BotManager.messagePrivateChannels) {
                        PrivateChannel channel = BotManager.jda.getPrivateChannelById(id.channel);
                        if (channel != null)
                            channel.sendMessage(leaveMessage).queue();
                    }
                }
            }
        }
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
