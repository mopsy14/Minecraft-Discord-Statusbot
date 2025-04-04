package mopsy.productions.discord.statusbot;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.concrete.PrivateChannel;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.exceptions.ErrorHandler;
import net.dv8tion.jda.api.requests.ErrorResponse;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

public class EmbedManager {
    public static List<SentEmbedData> sentEmbeds = new ArrayList<>();
    private static final Map<String, Function<StatusbotMain,String>> varSuppliers = new HashMap<>();
    private static BiFunction<StatusbotMain,String,String> backupVarSupplier = ((statusbotMain, varName) -> {
        System.out.println("Statusbot: Unknown variable: "+varName);
        return varName;
    });
    private static String lastEmbedDescription = "";
    private static String lastEmbedTitle = "";
    public static void sendEmbed(StatusbotMain statusbotMain, MessageChannel messageChannel){
        String title = parseEmbedText(statusbotMain,ConfigManager.getStr("embed_title"));
        String description = parseEmbedText(statusbotMain,ConfigManager.getStr("embed_content"));
        messageChannel.sendMessageEmbeds(generateEmbed(title, description)).queue(e->{
            if (e.getChannel() instanceof PrivateChannel)
                sentEmbeds.add(new SentEmbedData(e.getChannel().getIdLong(), e.getIdLong(), e.getChannel().asPrivateChannel().getUser().getIdLong()));
            else
                sentEmbeds.add(new SentEmbedData(e.getChannel().getIdLong(), e.getIdLong()));
        });
    }
    private static MessageEmbed generateEmbed(String title, String description){
         return new EmbedBuilder()
                 .setTitle(title)
                 .setDescription(description)
                 .build();
    }
    public static void tryUpdateAllEmbeds(StatusbotMain statusbotMain){
        String title = parseEmbedText(statusbotMain,ConfigManager.getStr("embed_title"));
        String description = parseEmbedText(statusbotMain,ConfigManager.getStr("embed_content"));
        if (!title.equals(lastEmbedTitle) || !description.equals(lastEmbedDescription)) {
            lastEmbedTitle = title;
            lastEmbedDescription = description;
            updateAllEmbeds(title,description);
        }
    }
    public static void updateAllEmbeds(String title, String description) {
        MessageEmbed embed = generateEmbed(title, description);
        for (int i = sentEmbeds.size()-1; i >= 0; i--) {
            SentEmbedData embedData = sentEmbeds.get(i);
            if (embedData.isInPrivateChannel) {
                PrivateChannel channel = BotManager.jda.getPrivateChannelById(embedData.channel);
                if (channel != null)
                    channel.editMessageEmbedsById(embedData.message, embed).queue(null,
                            new ErrorHandler().handle(ErrorResponse.UNKNOWN_MESSAGE, (exception)->{
                                System.out.println("Statusbot: Embed message with ID " + embedData.message + " could not be found!");
                                System.out.println("Statusbot: Removing the embed from update list");
                                sentEmbeds.remove(embedData);
                            }));
                else {
                    System.out.println("Statusbot: Private channel with ID " + embedData.channel + " for embed could not be found!");
                    System.out.println("Statusbot: Removing the embed from update list");
                    sentEmbeds.remove(i);
                }
            } else {
                TextChannel channel = BotManager.jda.getChannelById(TextChannel.class, embedData.channel);
                if (channel != null)
                    channel.editMessageEmbedsById(embedData.message, embed).queue(null,
                            new ErrorHandler().handle(ErrorResponse.UNKNOWN_MESSAGE, (exception)->{
                                System.out.println("Statusbot: Embed message with ID " + embedData.message + " could not be found!");
                                System.out.println("Statusbot: Removing the embed from update list");
                                sentEmbeds.remove(embedData);
                            }));
                else {
                    System.out.println("Statusbot: Text channel with ID " + embedData.channel + " for embed could not be found!");
                    System.out.println("Statusbot: Removing the embed from update list");
                    sentEmbeds.remove(i);
                }
            }
        }
    }

    public static void regVarSupplier(String varName, Function<StatusbotMain,String> varSupplier){
        varSuppliers.put(varName,varSupplier);
    }
    public static void regBackupVarSupplier(BiFunction<StatusbotMain,String,String> supplier){
        backupVarSupplier=supplier;
    }

    public static String parseEmbedText(StatusbotMain statusbotMain, String inputText){
        String res= "";
        char[] partVarName = null;
        int partVarNameLength = 0;
        boolean readingVarName = false;
        char[] inputChars = inputText.toCharArray();
        for (char c : inputChars) {
            if (c == '$'){
                readingVarName = !readingVarName;
                if(readingVarName){
                    partVarName = new char[inputChars.length-2];
                    partVarNameLength = 0;
                }else{
                    String varName = String.valueOf(partVarName).substring(0,partVarNameLength);
                    Function<StatusbotMain,String> varSupplier = varSuppliers.get(varName);
                    if (varSupplier!=null){
                        res = res.concat(varSupplier.apply(statusbotMain));
                    }else {
                        res = res.concat(backupVarSupplier.apply(statusbotMain,varName));
                    }
                }
            }else{
                if(readingVarName){
                    partVarName[partVarNameLength] = c;
                    partVarNameLength++;
                }else{
                    res = res.concat(String.valueOf(c));
                }
            }
        }
        return res;
    }
}
