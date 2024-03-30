package mopsy.productions.discord.statusbot;

import net.dv8tion.jda.api.entities.channel.concrete.PrivateChannel;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.simpleyaml.configuration.file.YamlConfiguration;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class StatusbotMain extends JavaPlugin implements Listener {
    public void init(){initAll();}
    private void initAll(){
        ConfigManager.init(this);
        DataManager.getAllData(this);
    }
    void addConfigDefaults(YamlConfiguration configuration){

    }
    String getConfigPath(){
        return getDataFolder().getAbsolutePath();
    }

    @Override
    public void onEnable() {
        initAll();
        getServer().getPluginManager().registerEvents(this,this);

        BotManager.regBot(
                ConfigManager.configuration.getString("bot_token"),
                Parser.createStatusMessage(()->MakeStringList(getServer().getOnlinePlayers().toArray()),getServer().getOnlinePlayers().size())
        );
        if(BotManager.jda!=null){
            BotEvents.regEvents(this);
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e){
        BotManager.regBot(
                ConfigManager.configuration.getString("bot_token"),
                Parser.createStatusMessage(()->MakeStringList(getServer().getOnlinePlayers().toArray()),getServer().getOnlinePlayers().size())
        );
        if(BotManager.jda!=null) {
            if (ConfigManager.getBool("enable_server_join_messages")) {
                String startMessage = Parser.createJoinMessage(
                        ()->MakeStringList(getServer().getOnlinePlayers().toArray()),
                        e.getPlayer().getName(),
                        getServer().getOnlinePlayers().size()
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
    public void onPlayerLeave(PlayerQuitEvent e){
        BotManager.regBot(
                ConfigManager.configuration.getString("bot_token"),
                Parser.createStatusMessage(()->MakeStringList(getServer().getOnlinePlayers().toArray(),e.getPlayer().getName()),getServer().getOnlinePlayers().size()-1)
        );
        if(BotManager.jda!=null) {
            if (ConfigManager.getBool("enable_server_leave_messages")) {
                String startMessage = Parser.createLeaveMessage(
                        ()->MakeStringList(getServer().getOnlinePlayers().toArray(),e.getPlayer().getName()),
                        e.getPlayer().getName(),
                        getServer().getOnlinePlayers().size()-1
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

    private List<String> MakeStringList(Object[] players){
        List<String> res = new ArrayList<>(players.length);
        for(Object player : players){
            res.add(((Player) player).getName());
        }
        return res;
    }

    private List<String> MakeStringList(Object[] players, String excluded){
        List<String> res = new ArrayList<>(players.length-1);
        for(Object player : players){
            String name = ((Player) player).getName();
            if(!name.equals(excluded)){
                res.add(name);
            }
        }
        return res;
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
        if(BotManager.jda!=null){
            BotManager.jda.shutdownNow();
            /*
            try {
                if(!BotManager.jda.awaitShutdown(Duration.ofSeconds(10))){
                    BotManager.jda.shutdownNow();
                }
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            */
        }
    }
    public void onBotReady(){
        System.out.println("onBotReady");
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
}
