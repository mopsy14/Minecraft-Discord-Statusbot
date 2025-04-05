package mopsy.productions.discord.statusbot;

import me.clip.placeholderapi.PlaceholderAPI;
import net.dv8tion.jda.api.entities.channel.concrete.PrivateChannel;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import okhttp3.OkHttpClient;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.simpleyaml.configuration.file.YamlFile;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class StatusbotMain extends JavaPlugin implements Listener {
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
        ConfigManager.addConfigKey(configuration,"embed_content",String.join("\n",
                        "status: $server-status$",
                        "$amount-of-players$/$max-players$ players online:",
                        "$player-list$"),
                String.join(
                    "\n",
                    "",
                    "This is the text displayed below the title of embeds",
                    "Possible placeholders are:",
                    "$server-status$ A red (offline) or green (online) circle telling whether the server is online",
                    "$amount-of-players$ The number of players currently online on the server",
                    "$max-players$ The maximum number of players that can play on the server",
                    "$motd$ The message of the day of the server",
                    "$player-list$ A list of player names separated by 'embed_player_separator_text'",
                    "$%ph-api%$ Replace ph-api with any placeholder of the PlaceholderAPI plugin (if installed)"));
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

    @Override
    public void onEnable() {
        initAll();
        getServer().getPluginManager().registerEvents(this,this);

        BotManager.regBot(
                ConfigManager.configuration.getString("bot_token"),
                Parser.createStatusMessage(()->MakeStringList(getServer().getOnlinePlayers().toArray()),getServer().getOnlinePlayers().size()),
                this
        );
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e){
        BotManager.regBot(
                ConfigManager.configuration.getString("bot_token"),
                Parser.createStatusMessage(()->MakeStringList(getServer().getOnlinePlayers().toArray()),getServer().getOnlinePlayers().size()),
                this
        );
        if(BotManager.jda!=null) {
            if (ConfigManager.getBool("enable_server_join_messages")) {
                String startMessage = Parser.createJoinMessage(
                        ()->MakeStringList(getServer().getOnlinePlayers().toArray()),
                        e.getPlayer().getName(),
                        getServer().getOnlinePlayers().size()
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
    public void onPlayerLeave(PlayerQuitEvent e){
        BotManager.regBot(
                ConfigManager.configuration.getString("bot_token"),
                Parser.createStatusMessage(()->MakeStringList(getServer().getOnlinePlayers().toArray(),e.getPlayer().getName()),getServer().getOnlinePlayers().size()-1),
                this
        );
        if(BotManager.jda!=null) {
            if (ConfigManager.getBool("enable_server_leave_messages")) {
                String startMessage = Parser.createLeaveMessage(
                        ()->MakeStringList(getServer().getOnlinePlayers().toArray(),e.getPlayer().getName()),
                        e.getPlayer().getName(),
                        getServer().getOnlinePlayers().size()-1
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
    public void onBotReady(){
        if(BotManager.jda!=null) {
            if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI"))
                EmbedManager.regBackupVarSupplier(((statusbotMain, input) -> {
                    if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI"))
                        return PlaceholderAPI.setPlaceholders(null,input);
                    else
                        return "N/A";
                }));
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
            new BukkitRunnable(){
                @Override
                public void run() {
                    EmbedManager.tryUpdateAllEmbeds(StatusbotMain.this);
                }
            }.runTaskTimer(this,0,200);
        }
    }
    public void regDefaultEmbedVarProviders(){
        EmbedManager.regVarSupplier("server-status",(statusbotMain) -> statusbotMain.online?":green_circle:":":red_circle:");
        EmbedManager.regVarSupplier("amount-of-players",(statusbotMain -> String.valueOf(getServer().getOnlinePlayers().size())));
        EmbedManager.regVarSupplier("player-list",(statusbotMain -> String.join(ConfigManager.getStr("embed_player_separator_text"),MakeStringList(getServer().getOnlinePlayers().toArray()))));
        EmbedManager.regVarSupplier("max-players",(statusbotMain -> String.valueOf(getServer().getMaxPlayers())));
        EmbedManager.regVarSupplier("motd",(statusbotMain -> getServer().getMotd()));
    }
}
