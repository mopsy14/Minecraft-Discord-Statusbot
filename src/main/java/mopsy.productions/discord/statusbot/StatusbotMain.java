package mopsy.productions.discord.statusbot;

import net.dv8tion.jda.api.entities.channel.concrete.PrivateChannel;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.ServerConnectedEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.event.EventHandler;
import okhttp3.OkHttpClient;
import org.simpleyaml.configuration.file.YamlFile;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class StatusbotMain extends Plugin implements Listener {
    private boolean online = true;
    public void init(){initAll();}
    private void initAll(){
        ConfigManager.init(this);
        DataManager.getAllData(this);
        regDefaultEmbedVarProviders();
    }
    void addConfigDefaults(YamlFile configuration){
        ConfigManager.addConfigKey(configuration,"embed_title","Minecraft Server Status",String.join(
                "\n",
                "",
                "The title of the embeds sent by the statusbot",
                "For all possible placeholders, see 'embed_content'"));
        ConfigManager.addConfigKey(configuration,"embed_content",String.join(
                "\n",
                "status: $server-status$",
                "$amount-of-players$/$max-players$ players online:",
                "$player-list$"),
                String.join(
                        "\n",
                        "",
                        "$server-status$ A red (offline) or green (online) circle telling whether the server is online",
                        "$amount-of-players$ The number of players currently online on the server",
                        "$max-players$ The maximum number of players that can play on the server",
                        "$motd$ The message of the day of the server",
                        "$player-list$ A list of player names separated by 'embed_player_separator_text'",
                        "$amount-of-servers$ The number of backend servers configured in the config of the proxy"));
        ConfigManager.addConfigKey(configuration,"embed_player_separator_text",", ",
                String.join(
                        "\n",
                        "",
                        "Enter the character(s) displayed between every player name in embeds.",
                        "Changing this to '---' would for example result in:",
                        "playername1---playername2---playername3---playername4"));
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
                Parser.createStatusMessage(()->MakeStringList(getProxy().getPlayers()),getProxy().getOnlineCount()),
                this
        );
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
            getProxy().getScheduler().schedule(this, () -> EmbedManager.tryUpdateAllEmbeds(StatusbotMain.this),0,10, TimeUnit.SECONDS);
        }
    }
    @EventHandler
    public void onJoin(ServerConnectedEvent e){
        BotManager.regBot(
                ConfigManager.configuration.getString("bot_token"),
                Parser.createStatusMessage(()->MakeStringList(getProxy().getPlayers()),getProxy().getOnlineCount()),
                this
        );
        if(BotManager.jda!=null) {
            if (ConfigManager.getBool("enable_server_join_messages")) {
                String startMessage = Parser.createJoinMessage(
                        ()->MakeStringList(getProxy().getPlayers()),
                        e.getPlayer().getName(),
                        getProxy().getOnlineCount()
                );
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
    @EventHandler
    public void onLeave(PlayerDisconnectEvent e){
        BotManager.regBot(
                ConfigManager.configuration.getString("bot_token"),
                Parser.createStatusMessage(()->MakeStringList(getProxy().getPlayers(),e.getPlayer().getName()),getProxy().getOnlineCount()-1),
                this
        );
        if(BotManager.jda!=null) {
            if (ConfigManager.getBool("enable_server_leave_messages")) {
                String startMessage = Parser.createLeaveMessage(
                        ()->MakeStringList(getProxy().getPlayers(),e.getPlayer().getName()),
                        e.getPlayer().getName(),
                        getProxy().getOnlineCount()-1
                );
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

    @Override
    public void onDisable() {
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
            online=false;
            EmbedManager.tryUpdateAllEmbeds(this);
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

            OkHttpClient client = BotManager.jda.getHttpClient();
            client.connectionPool().evictAll();
            client.dispatcher().executorService().shutdownNow();
        }
    }
    public void regDefaultEmbedVarProviders(){
        EmbedManager.regVarSupplier("server-status",(statusbotMain) -> statusbotMain.online?":green_circle:":":red_circle:");
        EmbedManager.regVarSupplier("amount-of-players",(statusbotMain -> String.valueOf(getProxy().getPlayers().size())));
        EmbedManager.regVarSupplier("player-list",(statusbotMain -> String.join(ConfigManager.getStr("embed_player_separator_text"),MakeStringList(getProxy().getPlayers()))));
        EmbedManager.regVarSupplier("max-players",(statusbotMain -> String.valueOf(getProxy().getConfig().getPlayerLimit())));
        EmbedManager.regVarSupplier("amount-of-servers",(statusbotMain -> String.valueOf(getProxy().getServers().size())));
    }
}
