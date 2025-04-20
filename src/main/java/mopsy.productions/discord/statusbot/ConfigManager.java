package mopsy.productions.discord.statusbot;

import org.simpleyaml.configuration.file.YamlFile;
import org.simpleyaml.configuration.implementation.api.QuoteStyle;

import java.io.File;
import java.io.IOException;

public class ConfigManager {
    public static boolean initialized = false;
    static File configFile;
    public static YamlFile configuration;

    public static void init(StatusbotMain statusbotMain){
        configFile = new File(statusbotMain.getConfigPath() + File.separator +"statusbot_config.yml");
        try {
            configuration = new YamlFile(configFile);
            ConfigManager.configuration.options().quoteStyleDefaults().setDefaultQuoteStyle(QuoteStyle.DOUBLE);
            configuration.createOrLoadWithComments();

            createDefaults(statusbotMain);
            configuration.save();

            initialized = true;
        }catch (IOException ioException){
            System.out.println("Statusbot: Something went wrong while creating the config file!");
            ioException.printStackTrace();
        }
    }
    public static void addConfigKey(YamlFile configuration, String key, String defaultValue, String documentation){
        configuration.addDefault(key, defaultValue);
        configuration.setComment(key, documentation);
    }
    private static void createDefaults(StatusbotMain statusbotMain){
        addConfigKey(configuration,"bot_token","enter token here",
                String.join(
                        "\n",
                        "",
                        "Enter the discord bot token here.",
                        "If you don't know what this means, see the wiki:",
                        "https://github.com/mopsy14/Minecraft-Discord-Statusbot/wiki/Getting-started"));
        addConfigKey(configuration,"status_mode","playing",
                String.join(
                        "\n",
                        "",
                        "Change the status mode the bot will use for displaying its status message.",
                        "Possible options are:",
                        "playing, competing, listening, watching, (streaming is WIP and won't work)"));
        addConfigKey(configuration,"player_separator_text",", ",
                String.join(
                        "\n",
                        "",
                        "Enter the character(s) displayed between every player name in the status message.",
                        "Changing this to '---' would for example result in:",
                        "playername1---playername2---playername3---playername4"));
        addConfigKey(configuration,"status_message","$AOP$ player(s) online: $PL$",
                String.join(
                        "\n",
                        "",
                        "Enter the status message here (using the placeholders).",
                        "The placeholders are:",
                        "$AOP$ being the number of players currently online on the server",
                        "$PL$ being a list of player names separated by 'player_separator_text'"));
        addConfigKey(configuration,"no_player_message","No one is online",
                String.join(
                        "\n",
                        "",
                        "Enter the status message the bot will display when there are 0 players online."));
        addConfigKey(configuration,"enable_direct_message_status_messages","true",
                String.join(
                        "\n",
                        "",
                        "Options are true/false",
                        "This will enable or disable the ability to use the status message sending/configuring in direct messages.",
                        "It is recommended to disable this on larger server since the bot (and the server) may become very slow",
                        "when using this with lots of players and registered channels."));
        addConfigKey(configuration,"enable_text_channel_status_messages","true",
                String.join(
                        "\n",
                        "",
                        "Options are true/false",
                        "This will enable or disable the ability to use the status message sending/configuring in server channels.",
                        "Users need at least the manage channel permission in the channel they're trying to register for the bot."));
        addConfigKey(configuration,"enable_server_start_messages","true",
                String.join(
                        "\n",
                        "",
                        "Options are true/false",
                        "This will enable or disable the sending of the server start message in both server and private channels."));
        addConfigKey(configuration,"start_message","The server has started",
                String.join(
                        "\n",
                        "",
                        "This is the message that will be sent in private and server channels when the server starts."));
        addConfigKey(configuration,"enable_server_stop_messages","true",
                String.join(
                        "\n",
                        "",
                        "Options are true/false",
                        "This will enable or disable the sending of the server stop message in both server and private channels."));
        addConfigKey(configuration,"stop_message","The server has stopped",
                String.join(
                        "\n",
                        "",
                        "This is the message that will be sent in private and server channels when the server stops."));
        addConfigKey(configuration,"enable_server_join_messages","true",
                String.join(
                        "\n",
                        "",
                        "Options are true/false",
                        "This will enable or disable the sending of the player join message in both server and private channels."));
        addConfigKey(configuration,"join_message","Player $CPL$ joined the server",
                String.join(
                        "\n",
                        "",
                        "This is the message that will be sent in private and server channels when a player joins the Minecraft server.",
                        "Possible placeholders are:",
                        "$CPL$ the name of the player that joined",
                        "$AOP$ being the number of players currently online on the server",
                        "$PL$ being a list of player names separated by 'player_separator_text'"));
        addConfigKey(configuration,"enable_server_leave_messages","true",
                String.join(
                        "\n",
                        "",
                        "Options are true/false",
                        "This will enable or disable the sending of the player leave message in both server and private channels."));
        addConfigKey(configuration,"leave_message","Player $CPL$ left the server",
                String.join(
                        "\n",
                        "",
                        "This is the message that will be sent in private and server channels when a player leaves the Minecraft server.",
                        "Possible placeholders are:",
                        "$CPL$ the name of the player that left",
                        "$AOP$ being the number of players currently online on the server",
                        "$PL$ being a list of player names separated by 'player_separator_text'"));
        statusbotMain.addConfigDefaults(configuration);
    }
    public static String getStr(String key){
        return configuration.getString(key);
    }
    public static boolean getBool(String key){
        return configuration.getString(key).equalsIgnoreCase("true");
    }



}
