package src.main.java.discord.statusbot;

import java.util.HashMap;
import java.util.Map;

public class StatusbotMain {
    private void initAll(){
        ConfigManager.init(this);
    }
    Map<String,Object> getConfigDefaults(){
        return new HashMap<>();
    }
    String getConfigPath(){
        return "";
    }

}
