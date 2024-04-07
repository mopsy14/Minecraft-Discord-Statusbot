package mopsy.productions.discord.statusbot;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.dv8tion.jda.api.utils.cache.CacheFlag;

import java.util.ArrayList;
import java.util.List;

import static mopsy.productions.discord.statusbot.ConfigManager.configuration;

public class BotManager {
    public static JDA jda;
    public static List<Long> messageTextChannels = new ArrayList<>();
    public static List<UserChannelPair> messagePrivateChannels = new ArrayList<>();
    public static void regBot(String botToken, String status, StatusbotMain main) {
        if(botToken==null || botToken.equals("enter token here") || botToken.equals("")){
            System.out.println("Statusbot: Invalid Bot Token!");
            System.out.println("Statusbot: Get the token of your discord bot from here: https://discord.com/developers/applications");
            System.out.println("Statusbot: After that put the token in the config file at: "+ConfigManager.configFile.getAbsoluteFile());
            return;
        }
        if(jda == null){
            JDABuilder builder;
            builder = JDABuilder.
                    createDefault(botToken, GatewayIntent.MESSAGE_CONTENT, GatewayIntent.DIRECT_MESSAGES, GatewayIntent.GUILD_MESSAGES, GatewayIntent.GUILD_MEMBERS)
                    .setMemberCachePolicy(MemberCachePolicy.ALL)
                    .disableCache(CacheFlag.VOICE_STATE, CacheFlag.EMOJI, CacheFlag.STICKER, CacheFlag.SCHEDULED_EVENTS)
                    .addEventListeners(new BotEvents(main));
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
