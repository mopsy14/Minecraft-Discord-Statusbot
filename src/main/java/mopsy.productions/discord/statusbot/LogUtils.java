package mopsy.productions.discord.statusbot;

public class LogUtils {
    static IStatusbotMain main = null;

    public static void log(String string, boolean error) {
        main.log(string, error);
    }

    public static void log(String string) {
        main.log(string, false);
    }

    public static void init(IStatusbotMain statusbotMain) {
        LogUtils.main = statusbotMain;
    }
}
