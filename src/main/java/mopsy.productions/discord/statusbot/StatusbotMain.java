package mopsy.productions.discord.statusbot;

import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.player.ServerPostConnectEvent;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import mopsy.productions.velocitytemplate.BuildConstants;
import net.dv8tion.jda.api.events.session.ShutdownEvent;
import org.simpleyaml.configuration.file.YamlConfiguration;

import java.nio.file.Path;
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
    }
    void addConfigDefaults(YamlConfiguration configuration){

    }
    String getConfigPath(){
        return dataDirectory.toAbsolutePath().toString();
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        initAll();

        BotManger.regBot(
                ConfigManager.configuration.getString("bot_token"),
                Parser.createStatusMessage(MakeStringList(server.getAllPlayers()))
        );
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
        BotManger.regBot(
                ConfigManager.configuration.getString("bot_token"),
                Parser.createStatusMessage(MakeStringList(server.getAllPlayers()))
        );
    }

    @Subscribe
    public void onDisconnected(DisconnectEvent event){
        BotManger.regBot(
                ConfigManager.configuration.getString("bot_token"),
                Parser.createStatusMessage(MakeStringList(server.getAllPlayers()))
        );
    }

    @Subscribe
    public void onShutdown(ShutdownEvent event){
        if(BotManger.jda!=null){
            BotManger.jda.shutdownNow();
        }
    }
}
