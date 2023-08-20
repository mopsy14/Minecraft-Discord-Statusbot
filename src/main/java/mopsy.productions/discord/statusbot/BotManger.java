package mopsy.productions.discord.statusbot;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;

import static mopsy.productions.discord.statusbot.ConfigManager.configuration;

public class BotManger {
    public static JDA jda;
    public static void regBot(String botToken, String status) {
        if(botToken==null || botToken.equals("enter token here") || botToken.equals("")){
            System.out.println("Statusbot: Invalid Bot Token!");
            System.out.println("Statusbot: Get the token of your discord bot from here: https://discord.com/developers/applications");
            System.out.println("Statusbot: After that put the token in the statusbot_config.yml file.");
            return;
        }
        if(jda == null){
            JDABuilder builder;
            builder = JDABuilder.createDefault(botToken);
            if (!(status == null || status.equals(""))) {
                switch (configuration.getString("status_mode").toLowerCase()) {
                    case "playing":
                        builder.setActivity(Activity.playing(status));
                        break;
                    case "competing":
                        builder.setActivity(Activity.competing(status));
                        break;
                    case "listening":
                        builder.setActivity(Activity.listening(status));
                        break;
                    case "streaming":
                        builder.setActivity(Activity.streaming(status, null));
                        break;
                    case "watching":
                        builder.setActivity(Activity.watching(status));
                        break;
                    default:
                        builder.setActivity(Activity.of(Activity.ActivityType.PLAYING, status));
                        System.out.println("Statusbot: invalid discord bot status mode: " + configuration.getString("status_mode"));
                        System.out.println("Statusbot: valid statuses are: playing, competing, listening, watching (Streaming is currently disabled)");
                        break;
                }
            }else{
                builder.setActivity(null);
            }
            jda = builder.build();
        }else{
            switch (configuration.getString("status_mode").toLowerCase()) {
                case "playing":
                    jda.getPresence().setActivity(Activity.playing(status));
                    break;
                case "competing":
                    jda.getPresence().setActivity(Activity.competing(status));
                    break;
                case "listening":
                    jda.getPresence().setActivity(Activity.listening(status));
                    break;
                case "streaming":
                    jda.getPresence().setActivity(Activity.streaming(status, null));
                    break;
                case "watching":
                    jda.getPresence().setActivity(Activity.watching(status));
                    break;
                default:
                    jda.getPresence().setActivity(Activity.of(Activity.ActivityType.PLAYING, status));
                    System.out.println("Statusbot: invalid discord bot status mode: " + configuration.getString("status_mode"));
                    System.out.println("Statusbot: valid statuses are: playing, competing, listening, watching (Streaming is currently disabled)");
                    break;
            }
        }
    }
}