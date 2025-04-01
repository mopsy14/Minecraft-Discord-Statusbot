package mopsy.productions.discord.statusbot;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.concrete.PrivateChannel;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

public class EmbedManager {
    public static List<SentEmbedData> sentEmbeds = new ArrayList<>();
    private static final char[] embedCharacters = ConfigManager.getStr("embed_content").toCharArray();
    private static final Map<String, Function<StatusbotMain,String>> varSuppliers = new HashMap<>();
    private static BiFunction<StatusbotMain,String,String> backupVarSupplier = ((statusbotMain, varName) -> {
        System.out.println("Statusbot: Unknown variable: "+varName);
        return "";
    });
    private static String lastEmbedDescription = "";
    private static String lastEmbedTitle = "";
    public static void sendEmbed(TextChannel textChannel, String title, String description){
        textChannel.sendMessageEmbeds(generateEmbed(title, description)).queue(e->{
            if (textChannel instanceof PrivateChannel privateChannel)
                sentEmbeds.add(new SentEmbedData(e.getIdLong(),privateChannel.getIdLong(),e.getChannel().getIdLong()));
            else
                sentEmbeds.add(new SentEmbedData(e.getIdLong(),e.getChannel().getIdLong()));
        });
    }
    private static MessageEmbed generateEmbed(String title, String description){
         return new EmbedBuilder()
                 .setTitle(title)
                 .setDescription(description)
                 .build();
    }
    public static void updateAllEmbeds(String title, String description){
        if (!title.equals(lastEmbedTitle) || !description.equals(lastEmbedDescription)){
            lastEmbedTitle=title;
            lastEmbedDescription=description;
            MessageEmbed embed = generateEmbed(title, description);
            for (SentEmbedData embedData : sentEmbeds) {
                if (embedData.isInPrivateChannel) {
                    PrivateChannel channel = BotManager.jda.getPrivateChannelById(embedData.channel);
                    if (channel != null)
                        channel.editMessageEmbedsById(embedData.message, embed).queue();
                    else
                        System.out.println("Statusbot: Private channel with ID " + embedData.channel + " for embed could not be found!");
                } else {
                    TextChannel channel = BotManager.jda.getChannelById(TextChannel.class, embedData.channel);
                    if (channel != null)
                        channel.editMessageEmbedsById(embedData.message, embed).queue();
                    else
                        System.out.println("Statusbot: Text channel with ID " + embedData.channel + " for embed could not be found!");
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

    public static String createDescription(StatusbotMain statusbotMain){
        String res= "";
        char[] partVarName = null;
        int partVarNameLength = 0;
        boolean readingVarName = false;
        for (char c : embedCharacters) {
            if (c == '$'){
                readingVarName = !readingVarName;
                if(readingVarName){
                    partVarName = new char[embedCharacters.length-2];
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
