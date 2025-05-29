package mopsy.productions.discord.statusbot;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.channel.concrete.PrivateChannel;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedOutEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.server.FMLServerStartedEvent;
import net.minecraftforge.fml.event.server.FMLServerStoppingEvent;
import okhttp3.OkHttpClient;
import org.simpleyaml.configuration.file.YamlFile;

import java.io.File;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Mod("statusbot")
public class StatusbotMain {
    private MinecraftServer server = null;
    private boolean online = true;
    private boolean isStopping = false;
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
                "running on Minecraft $mc-version$",
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
                        "$mc-version$ The Minecraft version the server is running on"));
        ConfigManager.addConfigKey(configuration,"embed_player_separator_text",", ",
                String.join(
                        "\n",
                        "",
                        "Enter the character(s) displayed between every player name in embeds.",
                        "Changing this to '---' would for example result in:",
                        "playername1---playername2---playername3---playername4"));
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
        isStopping = true;
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

    @SubscribeEvent
    public void tick(TickEvent.ServerTickEvent event){
        if (BotManager.jda!=null&&BotManager.jda.getStatus()== JDA.Status.CONNECTED) {
            if(server.getTickCount()%200==0){
                EmbedManager.tryUpdateAllEmbeds(this);
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

    public void regDefaultEmbedVarProviders(){
        EmbedManager.regVarSupplier("server-status",(statusbotMain) -> statusbotMain.online?":green_circle:":":red_circle:");
        EmbedManager.regVarSupplier("amount-of-players",(statusbotMain) -> String.valueOf(isStopping?0:server.getPlayerCount()));
        EmbedManager.regVarSupplier("player-list",(statusbotMain) -> isStopping?"":String.join(ConfigManager.getStr("embed_player_separator_text"),Arrays.asList(server.getPlayerNames())));
        EmbedManager.regVarSupplier("max-players",(statusbotMain) -> String.valueOf(server.getMaxPlayers()));
        EmbedManager.regVarSupplier("motd",(statusbotMain) -> server.getMotd());
        EmbedManager.regVarSupplier("mc-version",(statusbotMain) -> server.getServerVersion());
    }
}
