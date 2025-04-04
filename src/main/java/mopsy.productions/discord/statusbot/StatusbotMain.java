package mopsy.productions.discord.statusbot;

import org.simpleyaml.configuration.file.YamlFile;

public class StatusbotMain {
    private boolean online = true;
    public void init(){initAll();}
    private void initAll(){
        ConfigManager.init(this);
        DataManager.getAllData(this);
        regDefaultEmbedVarProviders();
    }
    void addConfigDefaults(YamlFile configuration){
        ConfigManager.addConfigKey(configuration,"embed_title","Minecraft Server Status",String.join(
                "\n",
                "",
                "Options are true/false",
                "This will enable or disable the sending of the player leave message in both server and private channels."));
        ConfigManager.addConfigKey(configuration,"embed_content",String.join(
                "\n",
                "status: $server-status$"),
                String.join(
                    "\n",
                    "",
                    "This is the text displayed below the title of embeds",
                    "Possible placeholders are:",
                    "$CPL$ the name of the player that joined",
                    "$AOP$ being the number of players currently online on the server",
                    "$PL$ being a list of player names separated by 'embed_player_separator_text'"));
        ConfigManager.addConfigKey(configuration,"embed_player_separator_text",", ",
                String.join(
                        "\n",
                        "",
                        "Enter the character(s) displayed between every player name in embeds.",
                        "Changing this to '---' would for example result in:",
                        "playername1---playername2---playername3---playername4"));
    }
    String getConfigPath(){
        return "";
    }
    public void onBotReady(){

    }
    public void regDefaultEmbedVarProviders(){
        EmbedManager.regVarSupplier("server-status",(statusbotMain) -> statusbotMain.online?":green_circle:":":red_circle:");
    }
}
