package mopsy.productions.discord.statusbot;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.internal.utils.PermissionUtil;
import org.jetbrains.annotations.NotNull;

public class BotEvents extends ListenerAdapter {
    public static boolean regEvents(){
        if(BotManager.jda == null) return false;

        BotManager.jda.addEventListener(new BotEvents());
        return true;
    }

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        String message = event.getMessage().getContentStripped().toLowerCase();
        switch (message) {
            case "!regchannelformessages" -> {
                if (event.getChannelType() == ChannelType.PRIVATE && ConfigManager.getBool("enable_direct_message_status_messages")) {
                    if(BotManager.messagePrivateChannels.contains(new UserChannelPair(event.getAuthor().getIdLong(), event.getChannel().getIdLong()))){
                        event.getMessage().reply("Channel was already registered, nothing changed").queue();
                    }else{
                        BotManager.messagePrivateChannels.add(new UserChannelPair(event.getAuthor().getIdLong(), event.getChannel().getIdLong()));
                        event.getMessage().reply("Channel registered").queue();
                    }
                }
                if (event.getChannelType() == ChannelType.TEXT && ConfigManager.getBool("enable_text_channel_status_messages")) {
                    if (PermissionUtil.checkPermission(event.getGuildChannel().getPermissionContainer(), event.getMember(), Permission.MANAGE_CHANNEL)) {
                        if(BotManager.messageTextChannels.contains(event.getChannel().getIdLong())){
                            event.getMessage().reply("Channel was already registered, nothing changed").queue();
                        }else{
                            BotManager.messageTextChannels.add(event.getChannel().getIdLong());
                            event.getMessage().reply("Channel registered").queue();
                        }
                    }else{
                        event.getMessage().reply("You need the manage channel permission for this channel to use this command").queue();
                    }
                }
            }
            case "!endregchannelformessages" -> {
                if (event.getChannelType() == ChannelType.PRIVATE && ConfigManager.getBool("enable_direct_message_status_messages")) {
                    if(BotManager.messagePrivateChannels.remove(new UserChannelPair(event.getAuthor().getIdLong(), event.getChannel().getIdLong()))){
                        event.getMessage().reply("The bot will stop sending messages to this channel").queue();
                    }else{
                        event.getMessage().reply("This channel isn't registered yet, so you can't de-register it").queue();
                    }
                }
                if (event.getChannelType() == ChannelType.TEXT && ConfigManager.getBool("enable_text_channel_status_messages")) {
                    if (PermissionUtil.checkPermission(event.getGuildChannel().getPermissionContainer(), event.getMember(), Permission.MANAGE_CHANNEL)) {
                        if(BotManager.messageTextChannels.remove(event.getChannel().getIdLong())){
                            event.getMessage().reply("The bot will stop sending messages to this channel").queue();
                        }else{
                            event.getMessage().reply("This channel isn't registered yet, so you can't de-register it").queue();
                        }
                    }else{
                        event.getMessage().reply("You need the manage channel permission for this channel to use this command").queue();
                    }
                }
            }
            case "!help" -> {
                event.getMessage().reply("Possible commands are:\n!help\n!regChannelForMessages\n!endRegChannelForMessages").queue();
            }
            case "!shutdown" -> {
                event.getMessage().reply("Shutting bot down").queue();
                StatusbotMain.shouldStopBot = true;
            }
        }
    }
}
