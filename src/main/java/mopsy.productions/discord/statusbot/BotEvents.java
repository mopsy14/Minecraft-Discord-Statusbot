package mopsy.productions.discord.statusbot;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.ErrorResponse;
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
            case "!sendembed": {
                if (event.getChannelType() == ChannelType.PRIVATE) {
                    EmbedManager.sendEmbed(main,event.getChannel());
                    System.out.println("Private channel with ID " + event.getChannel().getIdLong() + " of user: " + event.getAuthor().getName() + " with ID " + event.getAuthor().getIdLong() + " requested an embed");
                }
                if (event.getChannelType() == ChannelType.TEXT) {
                    if (PermissionUtil.checkPermission(event.getGuildChannel().getPermissionContainer(), event.getMember(), Permission.MANAGE_CHANNEL)) {
                        EmbedManager.sendEmbed(main,event.getChannel().asTextChannel());
                        System.out.println("Text channel with ID " + event.getChannel().getIdLong() + " of server: " + event.getGuild().getName() + " with ID " + event.getGuild().getIdLong() + " now has an embed");
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
                event.getMessage().reply("Possible commands are:\n!help\n!regChannelForMessages\n!endRegChannelForMessages\n!sendEmbed").queue();
                break;
            }
        }
    }

    @Override
    public void onReady(@NotNull ReadyEvent event) {
        for(UserChannelPair id : BotManager.messagePrivateChannels) {
            try {
                BotManager.jda.openPrivateChannelById(id.user).complete();
            } catch (Exception e) {
                System.out.println("An exception occurred, while loading private channel of user with id:" + id.user);
                e.printStackTrace();
            }
        }
        for (int i = EmbedManager.sentEmbeds.size()-1; i >= 0; i--) {
            SentEmbedData embedData = EmbedManager.sentEmbeds.get(i);
            if(!embedData.isInPrivateChannel)
                continue;
            try {
                BotManager.jda.openPrivateChannelById(embedData.user).complete();
            } catch (ErrorResponseException e){
                if(e.getErrorResponse() == ErrorResponse.UNKNOWN_USER) {
                    System.out.println("Statusbot: The user with ID " + embedData.user + " couldn't be found");
                    System.out.println("Statusbot: Removing the embed from update list");
                    EmbedManager.sentEmbeds.remove(i);
                }else{
                    System.out.println("An exception occurred, while loading private channel of user with id:" + embedData.user);
                    e.printStackTrace();
                }
            } catch (Exception e) {
                System.out.println("An exception occurred, while loading private channel of user with id:" + embedData.user);
                e.printStackTrace();
            }
        }
        main.onBotReady();
    }
}
