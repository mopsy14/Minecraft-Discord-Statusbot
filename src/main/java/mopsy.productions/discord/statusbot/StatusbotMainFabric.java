package mopsy.productions.discord.statusbot;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.MinecraftServer;
import org.simpleyaml.configuration.file.YamlFile;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class StatusbotMainFabric implements IStatusbotMain, ModInitializer {
    private boolean online = true;
    private MinecraftServer server = null;

    @Override
    public void addConfigDefaults(YamlFile configuration){
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

    @Override
    public String getConfigPath(){
        return System.getProperty("user.dir") + File.separator + "config";
    }

    @Override
    public void regDefaultEmbedVarProviders(){
        EmbedManager.regVarSupplier("server-status",(statusbotMain) -> ((StatusbotMainFabric)statusbotMain).online?":green_circle:":":red_circle:");
        EmbedManager.regVarSupplier("amount-of-players",(statusbotMain -> String.valueOf(((StatusbotMainFabric)statusbotMain).server.getCurrentPlayerCount())));
        EmbedManager.regVarSupplier("player-list",(statusbotMain -> String.join(ConfigManager.getStr("embed_player_separator_text"),MakeStringList(((StatusbotMainFabric)statusbotMain).server.getPlayerNames()))));
        EmbedManager.regVarSupplier("max-players",(statusbotMain -> String.valueOf(((StatusbotMainFabric)statusbotMain).server.getMaxPlayerCount())));
        EmbedManager.regVarSupplier("motd",(statusbotMain -> String.valueOf(((StatusbotMainFabric)statusbotMain).server.getServerMotd())));
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
            IStatusbotMain.super.onBotShutdown();
        });

        ServerPlayConnectionEvents.JOIN.register((serverPlayNetworkHandler,packetSender,server)->{
            String status = Parser.createStatusMessage(()->MakeStringListWith(server.getPlayerNames(),serverPlayNetworkHandler.getPlayer().getEntityName()),server.getPlayerNames().length+1);
            String joinMessage = Parser.createJoinMessage(
                    ()->MakeStringListWith(server.getPlayerNames(),serverPlayNetworkHandler.getPlayer().getEntityName()),
                    serverPlayNetworkHandler.getPlayer().getEntityName(),
                    server.getPlayerNames().length+1
            );
            IStatusbotMain.super.onPlayerJoined(status, joinMessage);
        });

        ServerPlayConnectionEvents.DISCONNECT.register((serverPlayNetworkHandler,server)->{
            String status = Parser.createStatusMessage(()->MakeStringList(server.getPlayerNames(),serverPlayNetworkHandler.getPlayer().getEntityName()),server.getPlayerNames().length-1);
            String leaveMessage = Parser.createLeaveMessage(
                    ()->MakeStringList(server.getPlayerNames(),serverPlayNetworkHandler.getPlayer().getEntityName()),
                    serverPlayNetworkHandler.getPlayer().getEntityName(),
                    server.getPlayerNames().length-1
            );
            IStatusbotMain.super.onPlayerLeft(status, leaveMessage);
        });

        ServerTickEvents.END_SERVER_TICK.register((server)->{
            if (server.getTicks() % 200 == 0)
                EmbedManager.tryUpdateAllEmbeds(StatusbotMainFabric.this);
        });
    }

    private List<String> MakeStringList(String[] players){
        return Arrays.asList(players);
    }

    private List<String> MakeStringList(String[] players, String excluded){
        List<String> res = new ArrayList<>(players.length-1);
        for(String player : players){
            if(!player.equals(excluded)){
                res.add(player);
            }
        }
        return res;
    }
    private List<String> MakeStringListWith(String[] players, String extra) {
        List<String> res = new ArrayList<>(players.length + 1);
        res.addAll(Arrays.asList(players));
        res.add(extra);
        return res;
    }
}
