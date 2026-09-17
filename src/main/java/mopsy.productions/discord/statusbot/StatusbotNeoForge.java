package mopsy.productions.discord.statusbot;

import com.mojang.logging.LogUtils;
import net.minecraft.server.MinecraftServer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import org.simpleyaml.configuration.file.YamlFile;
import org.slf4j.Logger;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Mod("statusbot")
public class StatusbotNeoForge implements IStatusbotMain {
    private MinecraftServer server = null;
    private boolean online = true;
    public static final Logger LOGGER = LogUtils.getLogger();

    @Override
    public void addConfigDefaults(YamlFile configuration) {
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

    @Override
    public String getConfigPath(){
        if(server != null && server.isDedicatedServer()){
            return Path.of("").toAbsolutePath() + File.separator + "config";
        }
        return "";
    }

    public StatusbotNeoForge() {
        NeoForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void start(final ServerStartedEvent event) {
        server = event.getServer();
        initAll();

        BotManager.regBot(
                ConfigManager.configuration.getString("bot_token"),
                Parser.createStatusMessage(()-> Arrays.asList(server.getPlayerNames()),server.getPlayerNames().length),
                this
        );
    }

    @SubscribeEvent
    public void stop(final ServerStoppingEvent event){
        online = false;

        IStatusbotMain.super.onBotShutdown();
    }

    @SubscribeEvent
    public void joined(final PlayerEvent.PlayerLoggedInEvent event) {
        String status = Parser.createStatusMessage(()->Arrays.asList(server.getPlayerNames()),server.getPlayerNames().length);
        String message = Parser.createJoinMessage(
                ()->Arrays.asList(server.getPlayerNames()),
                event.getEntity().getName().getString(),
                server.getPlayerNames().length
        );

        IStatusbotMain.super.onPlayerJoined(status, message);
    }

    @SubscribeEvent
    public void left(final PlayerEvent.PlayerLoggedOutEvent event) {
        String status = Parser.createStatusMessage(()->makeStringList(server.getPlayerNames(), event.getEntity().getName().getString()),server.getPlayerNames().length-1);
        String message = Parser.createLeaveMessage(
                ()->makeStringList(server.getPlayerNames(),event.getEntity().getName().getString()),
                event.getEntity().getName().getString(),
                server.getPlayerNames().length-1
        );

        IStatusbotMain.super.onPlayerLeft(status, message);
    }

    @SubscribeEvent
    public void tick(ServerTickEvent.Post event) {
        if (event.getServer().getTickCount() % 200 == 0) {
            IStatusbotMain.super.updateEmbeds();
        }
    }

    private static List<String> makeStringList(String[] players, String excluded){
        List<String> res = new ArrayList<>(players.length-1);
        for(String player : players){
            if(!player.equals(excluded)){
                res.add(player);
            }
        }
        return res;
    }

    public void regDefaultEmbedVarProviders(){
        EmbedManager.regVarSupplier("server-status",(statusbotMain) -> online?":green_circle:":":red_circle:");
        EmbedManager.regVarSupplier("amount-of-players",(statusbotMain -> String.valueOf(online?server.getPlayerCount():0)));
        EmbedManager.regVarSupplier("player-list",(statusbotMain -> online?String.join(ConfigManager.getStr("embed_player_separator_text"),Arrays.asList(server.getPlayerNames())):""));
        EmbedManager.regVarSupplier("max-players",(statusbotMain -> String.valueOf(server.getMaxPlayers())));
        EmbedManager.regVarSupplier("motd",(statusbotMain -> server.getMotd()));
        EmbedManager.regVarSupplier("mc-version",(statusbotMain -> server.getServerVersion()));
    }

    @Override
    public void log(String string, boolean error) {
        if (error) {
            LOGGER.error(string);
        } else {
            LOGGER.info(string);
        }
    }
}
