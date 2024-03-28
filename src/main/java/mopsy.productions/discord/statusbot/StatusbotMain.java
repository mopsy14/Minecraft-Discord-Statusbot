package mopsy.productions.discord.statusbot;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.simpleyaml.configuration.file.YamlConfiguration;

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

        BotManger.regBot(
                ConfigManager.configuration.getString("bot_token"),
                Parser.createStatusMessage(MakeStringList(getServer().getOnlinePlayers().toArray()))
        );
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e){
        BotManger.regBot(
                ConfigManager.configuration.getString("bot_token"),
                Parser.createStatusMessage(MakeStringList(getServer().getOnlinePlayers().toArray()))
        );
    }
    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent e){
        BotManger.regBot(
                ConfigManager.configuration.getString("bot_token"),
                Parser.createStatusMessage(MakeStringList(getServer().getOnlinePlayers().toArray(),e.getPlayer().getName()))
        );
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
        if(BotManger.jda!=null){
            BotManger.jda.shutdownNow();
        }
    }
    public void onBotReady(){

    }
}
