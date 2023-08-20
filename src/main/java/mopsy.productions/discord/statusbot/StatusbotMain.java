package mopsy.productions.discord.statusbot;

import org.simpleyaml.configuration.file.YamlConfiguration;

public class StatusbotMain {
    public void init(){initAll();}
    private void initAll(){
        ConfigManager.init(this);
    }
    void addConfigDefaults(YamlConfiguration configuration){

    }
    String getConfigPath(){
        return "";
    }

}
