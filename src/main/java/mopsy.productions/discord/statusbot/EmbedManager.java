package mopsy.productions.discord.statusbot;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

public class EmbedManager {
    private static final char[] embedCharacters = ConfigManager.getStr("embed_content").toCharArray();
    private static final Map<String, Function<StatusbotMain,String>> varSuppliers = new HashMap<>();
    public static List<Supplier<String>> embedVarSuppliers = new ArrayList<>();
    public static void sendEmbed(JDA jda, StatusbotMain statusbotMain){
        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setTitle(ConfigManager.getStr("embed_title"));
        embedBuilder.setDescription(createDescription(statusbotMain));

        jda.getPrivateChannels().get(0).sendMessageEmbeds(embedBuilder.build()).queue();
    }

    public static void regVarSupplier(String varName, Function<StatusbotMain,String> varSupplier){
        varSuppliers.put(varName,varSupplier);
    }

    private static String createDescription(StatusbotMain statusbotMain){
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
                    Function<StatusbotMain,String> varSupplier = varSuppliers.get(String.valueOf(partVarName).substring(0,partVarNameLength));
                    res = res.concat(varSupplier==null?"":varSupplier.apply(statusbotMain));
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
