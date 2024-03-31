package mopsy.productions.discord.statusbot;

import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.player.ServerPostConnectEvent;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import mopsy.productions.velocitytemplate.BuildConstants;
import net.dv8tion.jda.api.entities.channel.concrete.PrivateChannel;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import org.simpleyaml.configuration.file.YamlConfiguration;

import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Logger;

@Plugin(
        id = "discord_statusbot",
        name = "Discord Statusbot",
        version = BuildConstants.VERSION,
        authors = {"Mopsy"},
        description = "This plugin will host a statusbot on discord",
        url = "https://github.com/mopsy14/Minecraft-Discord-Statusbot/wiki/Getting-started"
)
public class StatusbotMain {
    private final Path dataDirectory;
    private final ProxyServer server;
    private final Logger logger;



    @Inject
    public StatusbotMain(ProxyServer server, Logger logger, @DataDirectory Path dataDirectory) {
        this.server = server;
        this.logger = logger;
        this.dataDirectory = dataDirectory;
    }
    private void initAll(){
        ConfigManager.init(this);
        DataManager.getAllData(this);
    }
    void addConfigDefaults(YamlConfiguration configuration){

    }
    String getConfigPath(){
        return dataDirectory.toAbsolutePath().toString();
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        initAll();

        BotManager.regBot(
                ConfigManager.configuration.getString("bot_token"),
                Parser.createStatusMessage(()->MakeStringList(server.getAllPlayers()),server.getAllPlayers().size()),
                this
        );
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

    private List<String> MakeStringList(Collection<Player> players){
        List<String> res = new ArrayList<>(players.size());
        for(Player player : players){
            res.add(player.getUsername());
        }
        return res;
    }
    @Subscribe
    public void onConnected(ServerPostConnectEvent event){
        BotManager.regBot(
                ConfigManager.configuration.getString("bot_token"),
                Parser.createStatusMessage(()->MakeStringList(server.getAllPlayers()),server.getAllPlayers().size()),
                this
        );
        if(BotManager.jda!=null) {
            if (ConfigManager.getBool("enable_server_join_messages")) {
                String joinMessage = Parser.createJoinMessage(
                        ()->MakeStringList(server.getAllPlayers()),
                        event.getPlayer().getUsername(),
                        server.getAllPlayers().size()
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
    }

    @Subscribe
    public void onDisconnected(DisconnectEvent event){
        BotManager.regBot(
                ConfigManager.configuration.getString("bot_token"),
                Parser.createStatusMessage(()->MakeStringList(server.getAllPlayers()),server.getAllPlayers().size()),
                this
        );
        if(BotManager.jda!=null) {
            if (ConfigManager.getBool("enable_server_leave_messages")) {
                String leaveMessage = Parser.createLeaveMessage(
                        ()->MakeStringList(server.getAllPlayers()),
                        event.getPlayer().getUsername(),
                        server.getAllPlayers().size()
                );
                if (ConfigManager.getBool("enable_direct_message_status_messages")) {
                    for (long id : BotManager.messageTextChannels) {
                        TextChannel channel = BotManager.jda.getTextChannelById(id);
                        if (channel != null)
                            channel.sendMessage(leaveMessage).queue();

                    }
                }
                if (ConfigManager.getBool("enable_text_channel_status_messages")) {
                    for (UserChannelPair id : BotManager.messagePrivateChannels) {
                        PrivateChannel channel = BotManager.jda.getPrivateChannelById(id.channel);
                        if (channel != null)
                            channel.sendMessage(leaveMessage).queue();
                    }
                }
            }
        }
    }

    @Subscribe
    public void onShutdown(ProxyShutdownEvent event){
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
