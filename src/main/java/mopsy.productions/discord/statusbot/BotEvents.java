package mopsy.productions.discord.statusbot;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.internal.utils.PermissionUtil;
import org.jetbrains.annotations.NotNull;

public class BotEvents extends ListenerAdapter {
    public static StatusbotMain main;
    public BotEvents(StatusbotMain main){
        BotEvents.main = main;
    }

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        String message = event.getMessage().getContentStripped().toLowerCase();
        switch (message) {
            case "!regchannelformessages": {
                if (event.getChannelType() == ChannelType.PRIVATE && ConfigManager.getBool("enable_direct_message_status_messages")) {
                    if(BotManager.messagePrivateChannels.contains(new UserChannelPair(event.getAuthor().getIdLong(), event.getChannel().getIdLong()))){
                        event.getMessage().reply("Channel was already registered, nothing changed").queue();
                    }else{
                        BotManager.messagePrivateChannels.add(new UserChannelPair(event.getAuthor().getIdLong(), event.getChannel().getIdLong()));
                        event.getMessage().reply("Channel registered").queue();
                        System.out.println("Private channel with ID "+event.getChannel().getIdLong()+" of user: "+event.getAuthor().getName()+" with ID "+event.getAuthor().getIdLong()+" was registered");
                    }
                }
                if (event.getChannelType() == ChannelType.TEXT && ConfigManager.getBool("enable_text_channel_status_messages")) {
                    if (PermissionUtil.checkPermission(event.getGuildChannel().getPermissionContainer(), event.getMember(), Permission.MANAGE_CHANNEL)) {
                        if(BotManager.messageTextChannels.contains(event.getChannel().getIdLong())){
                            event.getMessage().reply("Channel was already registered, nothing changed").queue();
                        }else{
                            BotManager.messageTextChannels.add(event.getChannel().getIdLong());
                            event.getMessage().reply("Channel registered").queue();
                            System.out.println("Text channel with ID "+event.getChannel().getIdLong()+" of server: "+event.getGuild().getName()+" with ID "+event.getGuild().getIdLong()+" was registered");
                        }
                    }else{
                        event.getMessage().reply("You need the manage channel permission for this channel to use this command").queue();
                    }
                }
                break;
            }
            case "!endregchannelformessages": {
                if (event.getChannelType() == ChannelType.PRIVATE && ConfigManager.getBool("enable_direct_message_status_messages")) {
                    if(BotManager.messagePrivateChannels.remove(new UserChannelPair(event.getAuthor().getIdLong(), event.getChannel().getIdLong()))){
                        event.getMessage().reply("The bot will stop sending messages to this channel").queue();
                        System.out.println("Private channel with ID "+event.getChannel().getIdLong()+" of user: "+event.getAuthor().getName()+" with ID "+event.getAuthor().getIdLong()+" was de-registered");
                    }else{
                        event.getMessage().reply("This channel isn't registered yet, so you can't de-register it").queue();
                    }
                }
                if (event.getChannelType() == ChannelType.TEXT && ConfigManager.getBool("enable_text_channel_status_messages")) {
                    if (PermissionUtil.checkPermission(event.getGuildChannel().getPermissionContainer(), event.getMember(), Permission.MANAGE_CHANNEL)) {
                        if(BotManager.messageTextChannels.remove(event.getChannel().getIdLong())){
                            event.getMessage().reply("The bot will stop sending messages to this channel").queue();
                            System.out.println("Text channel with ID "+event.getChannel().getIdLong()+" of server: "+event.getGuild().getName()+" with ID "+event.getGuild().getIdLong()+" was de-registered");
                        }else{
                            event.getMessage().reply("This channel isn't registered yet, so you can't de-register it").queue();
                        }
                    }else{
                        event.getMessage().reply("You need the manage channel permission for this channel to use this command").queue();
                    }
                }
                break;
            }
            case "!help": {
                event.getMessage().reply("Possible commands are:\n!help\n!regChannelForMessages\n!endRegChannelForMessages").queue();
                break;
            }
        }
    }

    @Override
    public void onReady(ReadyEvent event) {
        main.onBotReady();
    }
}
