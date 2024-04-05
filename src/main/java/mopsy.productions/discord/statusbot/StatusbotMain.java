package mopsy.productions.discord.statusbot;

import net.dv8tion.jda.api.entities.channel.concrete.PrivateChannel;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import org.simpleyaml.configuration.file.YamlConfiguration;

import java.io.File;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class StatusbotMain implements ModInitializer {

    private void initAll(){
        ConfigManager.init(this);
        DataManager.getAllData(this);
    }
    void addConfigDefaults(YamlConfiguration configuration){

    }
    String getConfigPath(){
        return System.getProperty("user.dir") + File.separator + "config";
    }
    public void onBotReady() {
        if(BotManager.jda!=null) {
            if (ConfigManager.getBool("enable_server_start_messages")) {
                String startMessage = Parser.createStartMessage();
                if (ConfigManager.getBool("enable_direct_message_status_messages")) {
                    for (long id : BotManager.messageTextChannels) {
                        TextChannel channel = BotManager.jda.getTextChannelById(id);
                        if (channel != null)
                            channel.sendMessage(startMessage).queue();
                    }
                }
                if (ConfigManager.getBool("enable_text_channel_status_messages")) {
                    for (UserChannelPair id : BotManager.messagePrivateChannels) {
                        PrivateChannel channel = BotManager.jda.getPrivateChannelById(id.channel);
                        if (channel != null)
                            channel.sendMessage(startMessage).queue();
                    }
                }
            }
        }
    }
    @Override
    public void onInitialize() {
        initAll();

        ServerLifecycleEvents.SERVER_STARTED.register(server->{
            BotManager.regBot(
                    ConfigManager.configuration.getString("bot_token"),
                    Parser.createStatusMessage(()->MakeStringList(server.getPlayerNames()),server.getPlayerNames().length),
                    this
            );
        });

        ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
            if(BotManager.jda!=null){
                if (ConfigManager.getBool("enable_server_stop_messages")) {
                    if (ConfigManager.getBool("enable_direct_message_status_messages")) {
                        String stopMessage = Parser.createStopMessage();
                        for (long id : BotManager.messageTextChannels) {
                            TextChannel channel = BotManager.jda.getTextChannelById(id);
                            if (channel != null)
                                channel.sendMessage(stopMessage).queue();
                        }
                    }
                    if (ConfigManager.getBool("enable_text_channel_status_messages")) {
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
        });
        ServerPlayConnectionEvents.JOIN.register((serverPlayNetworkHandler,packetSender,server)->{
            BotManager.regBot(
                    ConfigManager.configuration.getString("bot_token"),
                    Parser.createStatusMessage(()->MakeStringListWith(server.getPlayerNames(),serverPlayNetworkHandler.getPlayer().getEntityName()),server.getPlayerNames().length+1),
                    this
            );
            if(BotManager.jda!=null) {
                if (ConfigManager.getBool("enable_server_join_messages")) {
                    String joinMessage = Parser.createJoinMessage(
                            ()->MakeStringListWith(server.getPlayerNames(),serverPlayNetworkHandler.getPlayer().getEntityName()),
                            serverPlayNetworkHandler.getPlayer().getEntityName(),
                            server.getPlayerNames().length+1
                    );
                    if (ConfigManager.getBool("enable_direct_message_status_messages")) {
                        for (long id : BotManager.messageTextChannels) {
                            TextChannel channel = BotManager.jda.getTextChannelById(id);
                            if (channel != null)
                                channel.sendMessage(joinMessage).queue();

                        }
                    }
                    if (ConfigManager.getBool("enable_text_channel_status_messages")) {
                        for (UserChannelPair id : BotManager.messagePrivateChannels) {
                            PrivateChannel channel = BotManager.jda.getPrivateChannelById(id.channel);
                            if (channel != null)
                                channel.sendMessage(joinMessage).queue();
                        }
                    }
                }
            }
        });
        ServerPlayConnectionEvents.DISCONNECT.register((serverPlayNetworkHandler,server)->{
            BotManager.regBot(
                    ConfigManager.configuration.getString("bot_token"),
                    Parser.createStatusMessage(()->MakeStringList(server.getPlayerNames(),serverPlayNetworkHandler.getPlayer().getEntityName()),server.getPlayerNames().length-1),
                    this
            );
            if(BotManager.jda!=null) {
                if (ConfigManager.getBool("enable_server_leave_messages")) {
                    String startMessage = Parser.createLeaveMessage(
                            ()->MakeStringList(server.getPlayerNames(),serverPlayNetworkHandler.getPlayer().getEntityName()),
                            serverPlayNetworkHandler.getPlayer().getEntityName(),
                            server.getPlayerNames().length-1
                    );
                    if (ConfigManager.getBool("enable_direct_message_status_messages")) {
                        for (long id : BotManager.messageTextChannels) {
                            TextChannel channel = BotManager.jda.getTextChannelById(id);
                            if (channel != null)
                                channel.sendMessage(startMessage).queue();
                        }
                    }
                    if (ConfigManager.getBool("enable_text_channel_status_messages")) {
                        for (UserChannelPair id : BotManager.messagePrivateChannels) {
                            PrivateChannel channel = BotManager.jda.getPrivateChannelById(id.channel);
                            if (channel != null)
                                channel.sendMessage(startMessage).queue();
                        }
                    }
                }
            }
        });
    }

    private List<String> MakeStringList(String[] players){
        return Arrays.asList(players);
    }

    private List<String> MakeStringList(String[] players, String excluded){
        List<String> res = new ArrayList<>(players.length-1);
        for(String player : players){
            String name = player;
            if(!name.equals(excluded)){
                res.add(name);
            }
        }
        return res;
    }
    private List<String> MakeStringListWith(String[] players, String extra){
        List<String> res = new ArrayList<>(players.length+1);
        res.addAll(Arrays.asList(players));
        res.add(extra);
        return res;
    }
}
