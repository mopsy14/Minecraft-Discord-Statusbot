package mopsy.productions.discord.statusbot;

import org.simpleyaml.configuration.file.YamlFile;

public class StatusbotMain {
    private boolean online = true;
    public void init(){initAll();}
    private void initAll(){
        ConfigManager.init(this);
        DataManager.getAllData(this);
    }
    void addConfigDefaults(YamlFile configuration){
        ConfigManager.addConfigKey(configuration,"embed_title","Minecraft Server Status",String.join(
                "\n",
                "",
                "Options are true/false",
                "This will enable or disable the sending of the player leave message in both server and private channels."));
        ConfigManager.addConfigKey(configuration,"embed_content",String.join(
                "\n",
                "status: $server-status$",
        ));
    }
    String getConfigPath(){
        return "";
    }
    public void onBotReady(){

    }
    private void regEmbedVarProviders(){
        EmbedManager.regVarSupplier("server-status",(statusbotMain) -> statusbotMain.online?":green_circle:":":red_circle:");
    }
}
