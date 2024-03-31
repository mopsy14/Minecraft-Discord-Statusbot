package mopsy.productions.discord.statusbot;

import net.dv8tion.jda.api.entities.channel.concrete.PrivateChannel;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.ServerConnectedEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.event.EventHandler;
import org.simpleyaml.configuration.file.YamlConfiguration;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class StatusbotMain extends Plugin implements Listener {
    private void initAll(){
        ConfigManager.init(this);
        DataManager.getAllData(this);
    }
    void addConfigDefaults(YamlConfiguration configuration){

    }
    String getConfigPath(){
        return getDataFolder().getAbsolutePath();
    }

    private List<String> MakeStringList(Collection<ProxiedPlayer> players){
        List<String> res = new ArrayList<>(players.size());
        for(ProxiedPlayer player : players){
            res.add(player.getName());
        }
        return res;
    }
    private List<String> MakeStringList(Collection<ProxiedPlayer> players, String excluded){
        List<String> res = new ArrayList<>(players.size()-1);
        for(ProxiedPlayer player : players){
            String name = player.getName();
            if(!name.equals(excluded)){
                res.add(name);
            }
        }
        return res;
    }

    @Override
    public void onEnable() {
        initAll();
        getProxy().getPluginManager().registerListener(this,this);

        BotManager.regBot(
                ConfigManager.configuration.getString("bot_token"),
                Parser.createStatusMessage(()->MakeStringList(getProxy().getPlayers()),getProxy().getPlayers().size()),
                this
        );
    }
    public void onBotReady(){
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
    @EventHandler
    public void onJoin(ServerConnectedEvent e){
        BotManager.regBot(
                ConfigManager.configuration.getString("bot_token"),
                Parser.createStatusMessage(()->MakeStringList(getProxy().getPlayers()),getProxy().getPlayers().size()),
                this
        );
        if(BotManager.jda!=null) {
            if (ConfigManager.getBool("enable_server_join_messages")) {
                String startMessage = Parser.createJoinMessage(
                        ()->MakeStringList(getProxy().getPlayers()),
                        e.getPlayer().getName(),
                        getProxy().getPlayers().size()
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
    }
    @EventHandler
    public void onLeave(PlayerDisconnectEvent e){
        BotManager.regBot(
                ConfigManager.configuration.getString("bot_token"),
                Parser.createStatusMessage(()->MakeStringList(getProxy().getPlayers(),e.getPlayer().getName()),getProxy().getPlayers().size()-1),
                this
        );
        if(BotManager.jda!=null) {
            if (ConfigManager.getBool("enable_server_leave_messages")) {
                String startMessage = Parser.createLeaveMessage(
                        ()->MakeStringList(getProxy().getPlayers(),e.getPlayer().getName()),
                        e.getPlayer().getName(),
                        getProxy().getPlayers().size()-1
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
    }

    @Override
    public void onDisable() {
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
    }
}
