package mopsy.productions.discord.statusbot;

import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.simpleyaml.configuration.file.YamlFile;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class StatusbotMainSpigot extends JavaPlugin implements Listener, IStatusbotMain {
    private boolean online = true;
    private Logger logger = null;

    @Override
    public void addConfigDefaults(YamlFile configuration){
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

    @Override
    public String getConfigPath(){
        return getDataFolder().getAbsolutePath();
    }

    @Override
    public void onEnable() {
        logger = getLogger();
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
        String status = Parser.createStatusMessage(()->MakeStringList(getServer().getOnlinePlayers().toArray()),getServer().getOnlinePlayers().size());
        String message = Parser.createJoinMessage(
                ()->MakeStringList(getServer().getOnlinePlayers().toArray()),
                e.getPlayer().getName(),
                getServer().getOnlinePlayers().size()
        );
        IStatusbotMain.super.onPlayerJoined(status, message);
    }
    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent e){
        String status = Parser.createStatusMessage(()->MakeStringList(getServer().getOnlinePlayers().toArray(),e.getPlayer().getName()),getServer().getOnlinePlayers().size()-1);
        String message = Parser.createLeaveMessage(
                ()->MakeStringList(getServer().getOnlinePlayers().toArray(),e.getPlayer().getName()),
                e.getPlayer().getName(),
                getServer().getOnlinePlayers().size()-1
        );
        IStatusbotMain.super.onPlayerLeft(status, message);
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
        online=false;
        IStatusbotMain.super.onBotShutdown();
    }

    public void onBotReady(){
        if (BotManager.jda != null) {
            IStatusbotMain.super.onBotReady();
            if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
                EmbedManager.regBackupVarSupplier(((statusbotMain, input) -> {
                    if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI"))
                        return PlaceholderAPI.setPlaceholders(null, input);
                    else
                        return "N/A";
                }));
            }

            new BukkitRunnable() {
                @Override
                public void run() {
                    StatusbotMainSpigot.this.updateEmbeds();
                }
            }.runTaskTimer(this, 0, 200);
        }
    }

    public void regDefaultEmbedVarProviders(){
        EmbedManager.regVarSupplier("server-status",(statusbotMain) -> ((StatusbotMainSpigot)statusbotMain).online?":green_circle:":":red_circle:");
        EmbedManager.regVarSupplier("amount-of-players",(statusbotMain -> ((StatusbotMainSpigot)statusbotMain).online?String.valueOf(getServer().getOnlinePlayers().size()):"0"));
        EmbedManager.regVarSupplier("player-list",(statusbotMain -> ((StatusbotMainSpigot)statusbotMain).online?String.join(ConfigManager.getStr("embed_player_separator_text"),MakeStringList(getServer().getOnlinePlayers().toArray())):""));
        EmbedManager.regVarSupplier("max-players",(statusbotMain -> String.valueOf(getServer().getMaxPlayers())));
        EmbedManager.regVarSupplier("motd",(statusbotMain -> getServer().getMotd()));
    }

    @Override
    public void log(String string, boolean error) {
        logger.log(error ? Level.SEVERE : Level.INFO, string);
    }
}