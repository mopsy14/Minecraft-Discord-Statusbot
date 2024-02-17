package mopsy.productions.discord.statusbot;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.channel.concrete.PrivateChannel;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import org.simpleyaml.configuration.file.YamlConfiguration;

public class StatusbotMain {
    public static boolean shouldStopBot = false;
    public void init(){initAll();}
    private void initAll(){
        ConfigManager.init(this);
        DataManager.getAllData(this);
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

        while((BotManager.jda.getStatus() == JDA.Status.CONNECTED||BotManager.jda.getStatus() == JDA.Status.CONNECTING_TO_WEBSOCKET) && !shouldStopBot) {
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            if(ConfigManager.getBool("enable_text_channel_status_messages")) {
                for (long id : BotManager.messageTextChannels) {
                    TextChannel channel = BotManager.jda.getTextChannelById(id);
                    if (channel != null)
                        channel.sendMessage("test").queue();

                }
            }
            if(ConfigManager.getBool("enable_direct_message_status_messages")) {
                for (UserChannelPair id : BotManager.messagePrivateChannels) {
                    PrivateChannel channel = BotManager.jda.getPrivateChannelById(id.channel);
                    if (channel != null)
                        channel.sendMessage("test").queue();
                }
            }
        }

        DataManager.saveAllData(main);

        BotManager.jda.shutdown();

        try {
            BotManager.jda.awaitShutdown();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

}
