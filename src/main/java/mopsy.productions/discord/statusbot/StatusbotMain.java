package mopsy.productions.discord.statusbot;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.channel.concrete.PrivateChannel;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import org.simpleyaml.configuration.file.YamlConfiguration;

public class StatusbotMain {
    public void init(){initAll();}
    private void initAll(){
        ConfigManager.init(this);
    }
    void addConfigDefaults(YamlConfiguration configuration){

    }
    String getConfigPath(){
        return System.getProperty("user.dir")+"/configs";
    }

    public static void main(String[] args){
        StatusbotMain main = new StatusbotMain();
        main.init();
        BotManager.regBot(ConfigManager.configuration.getString("bot_token"),"test1234");

        BotEvents.regEvents();

        while(BotManager.jda.getStatus() == JDA.Status.CONNECTED||BotManager.jda.getStatus() == JDA.Status.CONNECTING_TO_WEBSOCKET) {
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            for (long id : BotManager.messageTextChannels) {
                BotManager.jda.getChannelById(TextChannel.class, id).sendMessage("test").queue();
            }
            for (long id : BotManager.messagePrivateChannels) {
                BotManager.jda.getChannelById(PrivateChannel.class, id).sendMessage("test").queue();
            }
        }


        BotManager.jda.shutdownNow();
        try {
            BotManager.jda.awaitShutdown();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

}
