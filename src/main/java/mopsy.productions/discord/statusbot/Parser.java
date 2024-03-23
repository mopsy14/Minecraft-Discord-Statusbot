package mopsy.productions.discord.statusbot;

import java.util.List;
import java.util.function.Supplier;

import static mopsy.productions.discord.statusbot.ConfigManager.configuration;

public class Parser {

    //Status message
    static String createStatusMessage(Supplier<List<String>> playersSupplier, int playerAmount){
        if(ConfigManager.initialized) {
            String Cme = configuration.getString("status_message");
            String Csepstr = configuration.getString("player_separator_text");
            String Cnpm = configuration.getString("no_player_message");

            return shorten(playerAmount==0 ? Cnpm : readCme(Cme, Csepstr, playersSupplier, playerAmount));
        }else
            return "";
    }
    private static String shorten(String str){
        return str.length()>128?str.substring(0,124).concat("..."):str;
    }
    private static String readCme(String Cme, String Csepstr, Supplier<List<String>> players, int playerAmount){
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
                    res = res.concat(getFromVarName(String.valueOf(partVarName).substring(0,partVarNameLength), Csepstr, players, playerAmount));
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
    private static String getFromVarName(String varName, String Csepstr, Supplier<List<String>> playersSupplier, int playerAmount){
        switch (varName){
            case"AOP":
                return String.valueOf(playerAmount);
            case"PL":
                return String.join(Csepstr, playersSupplier.get());
            default:
                System.out.println("Statusbot: Unknown variable: " + varName);
                return "";
        }
    }

    //Join message
    static String createJoinMessage(Supplier<List<String>> playersSupplier, String player, int playerAmount){
        if(ConfigManager.initialized) {
            String Jme = configuration.getString("join_message");
            String Csepstr = configuration.getString("player_separator_text");
            return createJoinMessage(Jme, Csepstr, player, playersSupplier, playerAmount);
        }else
            return "";
    }
    private static String createJoinMessage(String Jme, String Csepstr, String joinedPlayer, Supplier<List<String>> playersSupplier, int playerAmount){
        String res= "";
        char[] CmeChars = Jme.toCharArray();
        char[] partVarName = null;
        int partVarNameLength = 0;
        boolean readingVarName = false;
        for (char c : CmeChars) {
            if (c == '$'){
                readingVarName = !readingVarName;
                if(readingVarName){
                    partVarName = new char[Jme.length()-2];
                    partVarNameLength = 0;
                }else{
                    res = res.concat(getJoinFromVarName(String.valueOf(partVarName).substring(0,partVarNameLength), joinedPlayer, Csepstr, playersSupplier, playerAmount));
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
    private static String getJoinFromVarName(String varName, String joinedPlayer, String Csepstr, Supplier<List<String>> playersSupplier, int playerAmount){
        switch (varName){
            case"AOP":
                return String.valueOf(playerAmount);
            case"PL":
                return String.join(Csepstr, playersSupplier.get());
            case"CPL":
                return joinedPlayer;
            default:
                System.out.println("Statusbot: Unknown variable: " + varName);
                return "";
        }
    }

    //Leave message
    static String createLeaveMessage(Supplier<List<String>> playersSupplier, String player, int playerAmount){
        if(ConfigManager.initialized) {
            String Lme = configuration.getString("leave_message");
            String Csepstr = configuration.getString("player_separator_text");
            return createLeaveMessage(Lme, Csepstr, player, playersSupplier, playerAmount);
        }else
            return "";
    }
    private static String createLeaveMessage(String Lme, String Csepstr, String joinedPlayer, Supplier<List<String>> playersSupplier, int playerAmount){
        String res= "";
        char[] CmeChars = Lme.toCharArray();
        char[] partVarName = null;
        int partVarNameLength = 0;
        boolean readingVarName = false;
        for (char c : CmeChars) {
            if (c == '$'){
                readingVarName = !readingVarName;
                if(readingVarName){
                    partVarName = new char[Lme.length()-2];
                    partVarNameLength = 0;
                }else{
                    res = res.concat(getLeaveFromVarName(String.valueOf(partVarName).substring(0,partVarNameLength), joinedPlayer, Csepstr, playersSupplier, playerAmount));
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
    private static String getLeaveFromVarName(String varName, String leftPlayer, String Csepstr, Supplier<List<String>> playersSupplier, int playerAmount){
        switch (varName){
            case"AOP":
                return String.valueOf(playerAmount);
            case"PL":
                return String.join(Csepstr, playersSupplier.get());
            case"CPL":
                return leftPlayer;
            default:
                System.out.println("Statusbot: Unknown variable: " + varName);
                return "";
        }
    }

    //Start message
    static String createStartMessage(){
        if(ConfigManager.initialized) {
            return configuration.getString("start_message");
        }else
            return "";
    }

    //Stop message
    static String createStopMessage(){
        if(ConfigManager.initialized) {
            return configuration.getString("stop_message");
        }else
            return "";
    }
}
