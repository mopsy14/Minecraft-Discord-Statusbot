package mopsy.productions.discord.statusbot;

import java.util.List;

import static mopsy.productions.discord.statusbot.ConfigManager.configuration;

public class Parser {

    static String createStatusMessage(List<String> players){
        if(ConfigManager.initialized) {
            String Cme = configuration.getString("status_message");
            String Csepstr = configuration.getString("player_separator_text");
            String Cnpm = configuration.getString("no_player_message");

            return shorten(players.size() == 0 ? Cnpm : readCme(Cme, Csepstr, players));
        }else
            return "";
    }
    private static String shorten(String str){
        return str.length()>128?str.substring(0,124).concat("..."):str;
    }
    private static String readCme(String Cme, String Csepstr, List<String> players){
        String res= "";
        char[] CmeChars = Cme.toCharArray();
        char[] partVarName = null;
        int partVarNameLength = 0;
        boolean readingVarName = false;
        for (char c : CmeChars) {
            if (c == '$'){
                readingVarName = !readingVarName;
                if(readingVarName){
                    partVarName = new char[Cme.length()-2];
                    partVarNameLength = 0;
                }else{
                    res = res.concat(getFromVarName(String.valueOf(partVarName).substring(0,partVarNameLength), Csepstr, players));
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

    private static String getFromVarName(String varName, String Csepstr, List<String> players){
        switch (varName){
            case"AOP":
                return String.valueOf(players.size());
            case"PL":
                return String.join(Csepstr, players);
            default:
                System.out.println("Statusbot: Unknown variable: " + varName);
                return "";
        }
    }
}
