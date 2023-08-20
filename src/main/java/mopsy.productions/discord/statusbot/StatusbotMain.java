package mopsy.productions.discord.statusbot;

import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.ServerConnectedEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.event.EventHandler;
import org.simpleyaml.configuration.file.YamlConfiguration;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class StatusbotMain extends Plugin implements Listener {
    private void initAll(){
        ConfigManager.init(this);
    }
    void addConfigDefaults(YamlConfiguration configuration){

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

        BotManger.regBot(
                ConfigManager.configuration.getString("bot_token"),
                Parser.createStatusMessage(MakeStringList(getProxy().getPlayers()))
        );
    }

    @EventHandler
    public void onJoin(ServerConnectedEvent e){
        BotManger.regBot(
                ConfigManager.configuration.getString("bot_token"),
                Parser.createStatusMessage(MakeStringList(getProxy().getPlayers()))
        );
    }
    @EventHandler
    public void onLeave(PlayerDisconnectEvent e){
        BotManger.regBot(
                ConfigManager.configuration.getString("bot_token"),
                Parser.createStatusMessage(MakeStringList(getProxy().getPlayers(),e.getPlayer().getName()))
        );
    }

    @Override
    public void onDisable() {
        if(BotManger.jda!=null){
            BotManger.jda.shutdownNow();
        }
    }
}
