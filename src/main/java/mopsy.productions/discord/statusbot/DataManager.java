package mopsy.productions.discord.statusbot;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class DataManager {
    public static void getAllData(StatusbotMain statusbotMain){
        File dataFileFolder = new File(statusbotMain.getConfigPath()+File.separator+"Statusbot_Data");
        if(dataFileFolder.exists()){
            if(dataFileFolder.isDirectory()){
                getAllData(dataFileFolder);
                return;
            }else if(dataFileFolder.mkdirs()){
                System.out.println("Statusbot: Successfully created the Statusbot_Data folder");
                getAllData(dataFileFolder);
                return;
            }
        }else{
            if(dataFileFolder.mkdirs()){
                System.out.println("Statusbot: Successfully created the Statusbot_Data folder");
                getAllData(dataFileFolder);
                return;
            }
        }
        System.out.println("Statusbot: Something went wrong while creating the Statusbot_Data folder at: "+dataFileFolder.getAbsoluteFile());
    }

    private static void getAllData(File dataFileFolder){
        File messageTextChannelsFile = new File(dataFileFolder, "message_text_channels.txt");
        File messagePrivateChannelsFile = new File(dataFileFolder, "message_private_channels.txt");

        if(createFiles(messageTextChannelsFile,messagePrivateChannelsFile)){
            BotManager.messageTextChannels = readLongsDataFile(messageTextChannelsFile);
            BotManager.messagePrivateChannels = readDoubleLongsDataFile(messagePrivateChannelsFile);
            System.out.println("Statusbot: Read all registered channels from data files");
        }
    }
    private static boolean createFiles(File messageTextChannelsFile, File messagePrivateChannelsFile){
        try {
            if(!messageTextChannelsFile.exists()){
                if(messageTextChannelsFile.createNewFile()){
                    System.out.println("Statusbot: Successfully created the message_text_channels.txt file");
                }else {
                    System.out.println("Statusbot: Something went wrong while creating the message_text_channels.txt file at: " + messageTextChannelsFile.getAbsoluteFile());
                    return false;
                }
            }
            if(!messagePrivateChannelsFile.exists()){
                if(messagePrivateChannelsFile.createNewFile()){
                    System.out.println("Statusbot: Successfully created the message_private_channels.txt file");
                }else {
                    System.out.println("Statusbot: Something went wrong while creating the message_private_channels.txt file at: " + messagePrivateChannelsFile.getAbsoluteFile());
                    return false;
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return true;
    }
    private static List<Long> readLongsDataFile(File file){
        List<Long> res = new ArrayList<>();
        try {
            try(BufferedReader reader = new BufferedReader(new FileReader(file))){
                while (true) {
                    String readChars = reader.readLine();
                    if(readChars!=null){
                        try {
                            res.add(Long.parseLong(readChars));
                        }catch (NumberFormatException numberFormatException){
                            System.out.println("Statusbot: Something went wrong while trying to read a number from "+file.getAbsoluteFile());
                            System.out.println("Statusbot: "+readChars+" isn't a valid long");
                            numberFormatException.printStackTrace();
                        }
                    }else
                        break;
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return res;
    }
    private static List<UserChannelPair> readDoubleLongsDataFile(File file){
        List<UserChannelPair> res = new ArrayList<>();
        try {
            try(BufferedReader reader = new BufferedReader(new FileReader(file))){
                while (true) {
                    String readChars1 = reader.readLine();
                    String readChars2 = reader.readLine();
                    if(readChars1!=null && readChars2!=null){
                        try {
                            res.add(new UserChannelPair(Long.parseLong(readChars1),Long.parseLong(readChars2)));
                        }catch (NumberFormatException numberFormatException){
                            System.out.println("Statusbot: Something went wrong while trying to read a number from "+file.getAbsoluteFile());
                            System.out.println("Statusbot: "+readChars1+" or "+readChars2+" isn't a valid long");
                            numberFormatException.printStackTrace();
                        }
                    }else
                        break;
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return res;
    }
    public static void saveAllData(StatusbotMain statusbotMain){
        File dataFileFolder = new File(statusbotMain.getConfigPath()+File.separator+"Statusbot_Data");
        if(dataFileFolder.exists()){
            if(dataFileFolder.isDirectory()){
                saveAllData(dataFileFolder);
                return;
            }else if(dataFileFolder.mkdirs()){
                saveAllData(dataFileFolder);
                return;
            }
        }else{
            if(dataFileFolder.mkdirs()){
                saveAllData(dataFileFolder);
                return;
            }
        }
        System.out.println("Statusbot: Something went wrong while creating the Statusbot_Data folder at: "+dataFileFolder.getAbsoluteFile());
    }
    private static void saveAllData(File dataFileFolder){
        File messageTextChannelsFile = new File(dataFileFolder, "message_text_channels.txt");
        File messagePrivateChannelsFile = new File(dataFileFolder, "message_private_channels.txt");

        if(createFiles(messageTextChannelsFile,messagePrivateChannelsFile)){
            writeLongsDataFile(messageTextChannelsFile, BotManager.messageTextChannels);
            writeDoubleLongsDataFile(messagePrivateChannelsFile, BotManager.messagePrivateChannels);
        }
    }
    private static void writeLongsDataFile(File file, List<Long> longList){
        try {
            try(BufferedWriter writer = new BufferedWriter(new FileWriter(file))){
                for (int i = 0; i < longList.size(); i++) {
                    writer.write(Long.toString(longList.get(i)));
                    if(i!=longList.size()-1){
                        writer.write("\n");
                    }
                }
            }
            System.out.println("Statusbot: Successfully wrote registered channels to a data file");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    private static void writeDoubleLongsDataFile(File file, List<UserChannelPair> longList){
        try {
            try(BufferedWriter writer = new BufferedWriter(new FileWriter(file))){
                for (int i = 0; i < longList.size(); i++) {
                    writer.write(longList.get(i).user+"\n");
                    writer.write(Long.toString(longList.get(i).channel));
                    if(i!=longList.size()-1){
                        writer.write("\n");
                    }
                }
            }
            System.out.println("Statusbot: Successfully wrote registered channels to a data file");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
