package mopsy.productions.discord.statusbot;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.requests.ErrorResponse;
import net.dv8tion.jda.internal.utils.PermissionUtil;
import org.jetbrains.annotations.NotNull;

public class BotEvents extends ListenerAdapter {
    public static IStatusbotMain main;
    public BotEvents(IStatusbotMain main){
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
                        LogUtils.log("Private channel with ID "+event.getChannel().getIdLong()+" of user: "+event.getAuthor().getName()+" with ID "+event.getAuthor().getIdLong()+" was registered");
                    }
                }
                if (event.getChannelType() == ChannelType.TEXT && ConfigManager.getBool("enable_text_channel_status_messages")) {
                    if (PermissionUtil.checkPermission(event.getGuildChannel().getPermissionContainer(), event.getMember(), Permission.MANAGE_CHANNEL)) {
                        if(BotManager.messageTextChannels.contains(event.getChannel().getIdLong())){
                            event.getMessage().reply("Channel was already registered, nothing changed").queue();
                        }else{
                            BotManager.messageTextChannels.add(event.getChannel().getIdLong());
                            event.getMessage().reply("Channel registered").queue();
                            LogUtils.log("Text channel with ID "+event.getChannel().getIdLong()+" of server: "+event.getGuild().getName()+" with ID "+event.getGuild().getIdLong()+" was registered");
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
                    LogUtils.log("Private channel with ID " + event.getChannel().getIdLong() + " of user: " + event.getAuthor().getName() + " with ID " + event.getAuthor().getIdLong() + " requested an embed");
                }
                if (event.getChannelType() == ChannelType.TEXT) {
                    if (PermissionUtil.checkPermission(event.getGuildChannel().getPermissionContainer(), event.getMember(), Permission.MANAGE_CHANNEL)) {
                        EmbedManager.sendEmbed(main,event.getChannel().asTextChannel());
                        LogUtils.log("Text channel with ID " + event.getChannel().getIdLong() + " of server: " + event.getGuild().getName() + " with ID " + event.getGuild().getIdLong() + " now has an embed");
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
                        LogUtils.log("Private channel with ID "+event.getChannel().getIdLong()+" of user: "+event.getAuthor().getName()+" with ID "+event.getAuthor().getIdLong()+" was de-registered");
                    }else{
                        event.getMessage().reply("This channel isn't registered yet, so you can't de-register it").queue();
                    }
                }
                if (event.getChannelType() == ChannelType.TEXT && ConfigManager.getBool("enable_text_channel_status_messages")) {
                    if (PermissionUtil.checkPermission(event.getGuildChannel().getPermissionContainer(), event.getMember(), Permission.MANAGE_CHANNEL)) {
                        if(BotManager.messageTextChannels.remove(event.getChannel().getIdLong())){
                            event.getMessage().reply("The bot will stop sending messages to this channel").queue();
                            LogUtils.log("Text channel with ID "+event.getChannel().getIdLong()+" of server: "+event.getGuild().getName()+" with ID "+event.getGuild().getIdLong()+" was de-registered");
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
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        String commandName = event.getName();
        switch (commandName) {
            case "help": {
                event.reply("Possible commands are:\n/help\n/sendembed\n/regchannelformessages\n/endregchannelformessages").setEphemeral(true).queue();
                break;
            }
            case "sendembed": {
                if (event.getChannelType() == ChannelType.PRIVATE) {
                    EmbedManager.sendEmbed(main, event.getChannel());
                    LogUtils.log("Private channel with ID " + event.getChannel().getIdLong() + " of user: " + event.getUser().getName() + " with ID " + event.getUser().getIdLong() + " requested an embed");
                }
                if (event.getChannelType() == ChannelType.TEXT) {
                    EmbedManager.sendEmbed(main, event.getChannel().asTextChannel());
                    event.reply("Embed sent").setEphemeral(true).queue();
                    LogUtils.log("Text channel with ID " + event.getChannel().getIdLong() + " of server: " + event.getGuild().getName() + " with ID " + event.getGuild().getIdLong() + " now has an embed");
                }
                break;
            }
            case "regchannelformessages": {
                if (event.getChannelType() == ChannelType.PRIVATE && ConfigManager.getBool("enable_direct_message_status_messages")) {
                    if (BotManager.messagePrivateChannels.contains(new UserChannelPair(event.getUser().getIdLong(), event.getChannel().getIdLong()))) {
                        event.reply("Channel was already registered, nothing changed").setEphemeral(true).queue();
                    } else {
                        BotManager.messagePrivateChannels.add(new UserChannelPair(event.getUser().getIdLong(), event.getChannel().getIdLong()));
                        event.reply("Channel registered").setEphemeral(true).queue();
                        LogUtils.log("Private channel with ID " + event.getChannel().getIdLong() + " of user: " + event.getUser().getName() + " with ID " + event.getUser().getIdLong() + " was registered");
                    }
                }
                if (event.getChannelType() == ChannelType.TEXT && ConfigManager.getBool("enable_text_channel_status_messages")) {
                    if (BotManager.messageTextChannels.contains(event.getChannel().getIdLong())) {
                        event.reply("Channel was already registered, nothing changed").setEphemeral(true).queue();
                    } else {
                        BotManager.messageTextChannels.add(event.getChannel().getIdLong());
                        event.reply("Channel registered").setEphemeral(true).queue();
                        LogUtils.log("Text channel with ID " + event.getChannel().getIdLong() + " of server: " + event.getGuild().getName() + " with ID " + event.getGuild().getIdLong() + " was registered");
                    }
                }
                break;
            }
            case "endregchannelformessages": {
                if (event.getChannelType() == ChannelType.PRIVATE && ConfigManager.getBool("enable_direct_message_status_messages")) {
                    if (BotManager.messagePrivateChannels.remove(new UserChannelPair(event.getUser().getIdLong(), event.getChannel().getIdLong()))) {
                        event.reply("The bot will stop sending messages to this channel").setEphemeral(true).queue();
                        LogUtils.log("Private channel with ID " + event.getChannel().getIdLong() + " of user: " + event.getUser().getName() + " with ID " + event.getUser().getIdLong() + " was de-registered");
                    } else {
                        event.reply("This channel isn't registered yet, so you can't de-register it").setEphemeral(true).queue();
                    }
                }
                if (event.getChannelType() == ChannelType.TEXT && ConfigManager.getBool("enable_text_channel_status_messages")) {
                    if (BotManager.messageTextChannels.remove(event.getChannel().getIdLong())) {
                        event.reply("The bot will stop sending messages to this channel").setEphemeral(true).queue();
                        LogUtils.log("Text channel with ID " + event.getChannel().getIdLong() + " of server: " + event.getGuild().getName() + " with ID " + event.getGuild().getIdLong() + " was de-registered");
                    } else {
                        event.reply("This channel isn't registered yet, so you can't de-register it").setEphemeral(true).queue();
                    }
                }
                break;
            }
            default: {
                event.reply("Unknown command: " + commandName).setEphemeral(true).queue();
                break;
            }
        }
    }

    @Override
    public void onReady(@NotNull ReadyEvent event) {
        BotManager.jda.updateCommands().addCommands(
                Commands.slash("help", "Sends a list of commands you can use")
                        .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.MANAGE_CHANNEL)),

                Commands.slash("sendembed", "Sends an embed to the channel you are currently in")
                        .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.MANAGE_CHANNEL)),

                Commands.slash("regchannelformessages", "Registers the current channel for status messages")
                        .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.MANAGE_CHANNEL)),

                Commands.slash("endregchannelformessages", "De-registers the current channel for status messages")
                        .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.MANAGE_CHANNEL))
        ).queue();

        for(UserChannelPair id : BotManager.messagePrivateChannels) {
            try {
                BotManager.jda.openPrivateChannelById(id.user).complete();
            } catch (Exception e) {
                LogUtils.log("An exception occurred, while loading private channel of user with id:" + id.user, true);
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
                    LogUtils.log("The user with ID " + embedData.user + " couldn't be found", true);
                    LogUtils.log("Removing the embed from update list", true);
                    EmbedManager.sentEmbeds.remove(i);
                }else{
                    LogUtils.log("An exception occurred, while loading private channel of user with id:" + embedData.user, true);
                    e.printStackTrace();
                }
            } catch (Exception e) {
                LogUtils.log("An exception occurred, while loading private channel of user with id:" + embedData.user, true);
                e.printStackTrace();
            }
        }
        main.onBotReady();
    }
}
