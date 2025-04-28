package mopsy.productions.discord.statusbot;

import net.dv8tion.jda.api.entities.channel.concrete.PrivateChannel;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.MinecraftServer;
import okhttp3.OkHttpClient;
import org.simpleyaml.configuration.file.YamlFile;

import java.io.File;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class StatusbotMain implements ModInitializer {
    private boolean online = true;
    private MinecraftServer server = null;
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
                        "This is the text displayed below the title of embeds",
                        "Possible placeholders are:",
                        "$server-status$ A red (offline) or green (online) circle telling whether the server is online",
                        "$amount-of-players$ The number of players currently online on the server",
                        "$max-players$ The maximum number of players that can play on the server",
                        "$motd$ The message of the day of the server",
                        "$player-list$ A list of player names separated by 'embed_player_separator_text'"));
        ConfigManager.addConfigKey(configuration,"embed_player_separator_text",", ",
                String.join(
                        "\n",
                        "",
                        "Enter the character(s) displayed between every player name in embeds.",
                        "Changing this to '---' would for example result in:",
                        "playername1---playername2---playername3---playername4"));
    }
    String getConfigPath(){
        return System.getProperty("user.dir") + File.separator + "config";
    }
    public void onBotReady() {
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
    @Override
    public void onInitialize() {
        initAll();

        ServerLifecycleEvents.SERVER_STARTED.register(server->{
            this.server=server;
            BotManager.regBot(
                    ConfigManager.configuration.getString("bot_token"),
                    Parser.createStatusMessage(()->MakeStringList(server.getPlayerNames()),server.getPlayerNames().length),
                    this
            );
        });

        ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
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
        });
        ServerTickEvents.END_SERVER_TICK.register((server)->{
            if (server.getTicks() % 200 == 0)
                EmbedManager.tryUpdateAllEmbeds(StatusbotMain.this);
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
    public void regDefaultEmbedVarProviders(){
        EmbedManager.regVarSupplier("server-status",(statusbotMain) -> statusbotMain.online?":green_circle:":":red_circle:");
        EmbedManager.regVarSupplier("amount-of-players",(statusbotMain -> String.valueOf(statusbotMain.server.getCurrentPlayerCount())));
        EmbedManager.regVarSupplier("player-list",(statusbotMain -> String.join(ConfigManager.getStr("embed_player_separator_text"),MakeStringList(statusbotMain.server.getPlayerNames()))));
        EmbedManager.regVarSupplier("max-players",(statusbotMain -> String.valueOf(statusbotMain.server.getMaxPlayerCount())));
        EmbedManager.regVarSupplier("motd",(statusbotMain -> String.valueOf(statusbotMain.server.getServerMotd())));
    }
}
