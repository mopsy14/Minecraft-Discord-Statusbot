package mopsy.productions.discord.statusbot;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class DataManager {
    public static void getAllData(StatusbotMain statusbotMain) {
        File dataFileFolder = new File(statusbotMain.getConfigPath() + File.separator + "Statusbot_Data");
        if (dataFileFolder.exists()) {
            if (dataFileFolder.isDirectory()) {
                getAllData(dataFileFolder);
                return;
            } else if (dataFileFolder.mkdirs()) {
                System.out.println("Statusbot: Successfully created the Statusbot_Data folder");
                getAllData(dataFileFolder);
                return;
            }
        } else {
            if (dataFileFolder.mkdirs()) {
                System.out.println("Statusbot: Successfully created the Statusbot_Data folder");
                getAllData(dataFileFolder);
                return;
            }
        }
        System.out.println("Statusbot: Something went wrong while creating the Statusbot_Data folder at: " + dataFileFolder.getAbsoluteFile());
    }

    private static void getAllData(File dataFileFolder) {
        File messageTextChannelsFile = new File(dataFileFolder, "message_text_channels.txt");
        File messagePrivateChannelsFile = new File(dataFileFolder, "message_private_channels.txt");
        File embedMessagesFile = new File(dataFileFolder, "embed_messages.txt");
        File privateEmbedsFile = new File(dataFileFolder, "private_embed_messages.txt");

        if (createFiles(messageTextChannelsFile, messagePrivateChannelsFile, embedMessagesFile, privateEmbedsFile)) {
            BotManager.messageTextChannels = readLongsDataFile(messageTextChannelsFile);
            BotManager.messagePrivateChannels = readDoubleLongsDataFile(messagePrivateChannelsFile);
            EmbedManager.sentEmbeds = readEmbedDataFiles(embedMessagesFile,privateEmbedsFile);
            System.out.println("Statusbot: Read all registered channels from data files");
        }
    }

    private static boolean createFiles(File messageTextChannelsFile, File messagePrivateChannelsFile, File embedMessagesFile, File privateEmbedsFile) {
        try {
            if (!messageTextChannelsFile.exists()) {
                if (messageTextChannelsFile.createNewFile()) {
                    System.out.println("Statusbot: Successfully created the message_text_channels.txt file");
                } else {
                    System.out.println("Statusbot: Something went wrong while creating the message_text_channels.txt file at: " + messageTextChannelsFile.getAbsoluteFile());
                    return false;
                }
            }
            if (!messagePrivateChannelsFile.exists()) {
                if (messagePrivateChannelsFile.createNewFile()) {
                    System.out.println("Statusbot: Successfully created the message_private_channels.txt file");
                } else {
                    System.out.println("Statusbot: Something went wrong while creating the message_private_channels.txt file at: " + messagePrivateChannelsFile.getAbsoluteFile());
                    return false;
                }
            }
            if (!embedMessagesFile.exists()) {
                if (embedMessagesFile.createNewFile()) {
                    System.out.println("Statusbot: Successfully created the embed_messages.txt file");
                } else {
                    System.out.println("Statusbot: Something went wrong while creating the embed_messages.txt file at: " + embedMessagesFile.getAbsoluteFile());
                    return false;
                }
            }
            if (!privateEmbedsFile.exists()) {
                if (privateEmbedsFile.createNewFile()) {
                    System.out.println("Statusbot: Successfully created the private_embed_messages.txt file");
                } else {
                    System.out.println("Statusbot: Something went wrong while creating the private_embed_messages.txt file at: " + privateEmbedsFile.getAbsoluteFile());
                    return false;
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return true;
    }

    private static List<Long> readLongsDataFile(File file) {
        List<Long> res = new ArrayList<>();
        try {
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                while (true) {
                    String readChars = reader.readLine();
                    if (readChars != null) {
                        try {
                            res.add(Long.parseLong(readChars));
                        } catch (NumberFormatException numberFormatException) {
                            System.out.println("Statusbot: Something went wrong while trying to read a number from " + file.getAbsoluteFile());
                            System.out.println("Statusbot: " + readChars + " isn't a valid long");
                            numberFormatException.printStackTrace();
                        }
                    } else
                        break;
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return res;
    }

    private static List<UserChannelPair> readDoubleLongsDataFile(File file) {
        List<UserChannelPair> res = new ArrayList<>();
        try {
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                while (true) {
                    String readChars1 = reader.readLine();
                    String readChars2 = reader.readLine();
                    if (readChars1 != null && readChars2 != null) {
                        try {
                            res.add(new UserChannelPair(Long.parseLong(readChars1), Long.parseLong(readChars2)));
                        } catch (NumberFormatException numberFormatException) {
                            System.out.println("Statusbot: Something went wrong while trying to read a number from " + file.getAbsoluteFile());
                            System.out.println("Statusbot: " + readChars1 + " or " + readChars2 + " isn't a valid long");
                            numberFormatException.printStackTrace();
                        }
                    } else
                        break;
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return res;
    }

    private static List<SentEmbedData> readEmbedDataFiles(File embeds_file, File private_embeds_file) {
        List<SentEmbedData> res = new ArrayList<>();
        try {
            try (BufferedReader reader = new BufferedReader(new FileReader(embeds_file))) {
                while (true) {
                    String channel = reader.readLine();
                    String message = reader.readLine();
                    if (channel != null && message != null) {
                        try {
                            res.add(new SentEmbedData(Long.parseLong(channel), Long.parseLong(message)));
                        } catch (NumberFormatException numberFormatException) {
                            System.out.println("Statusbot: Something went wrong while trying to read a number from " + embeds_file.getAbsoluteFile());
                            System.out.println("Statusbot: " + channel + " or " + message + " isn't a valid long");
                            numberFormatException.printStackTrace();
                        }
                    } else
                        break;
                }
            }
            try (BufferedReader reader = new BufferedReader(new FileReader(private_embeds_file))) {
                while (true) {
                    String channel = reader.readLine();
                    String message = reader.readLine();
                    String user = reader.readLine();
                    if (channel != null && message != null && user != null) {
                        try {
                            res.add(new SentEmbedData(Long.parseLong(channel), Long.parseLong(message), Long.parseLong(user)));
                        } catch (NumberFormatException numberFormatException) {
                            System.out.println("Statusbot: Something went wrong while trying to read a number from " + private_embeds_file.getAbsoluteFile());
                            System.out.println("Statusbot: " + channel + ", " + message + "or"+ user + " isn't a valid long");
                            numberFormatException.printStackTrace();
                        }
                    } else
                        break;
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return res;
    }

    public static void saveAllData(StatusbotMain statusbotMain) {
        File dataFileFolder = new File(statusbotMain.getConfigPath() + File.separator + "Statusbot_Data");
        if (dataFileFolder.exists()) {
            if (dataFileFolder.isDirectory()) {
                saveAllData(dataFileFolder);
                return;
            } else if (dataFileFolder.mkdirs()) {
                saveAllData(dataFileFolder);
                return;
            }
        } else {
            if (dataFileFolder.mkdirs()) {
                saveAllData(dataFileFolder);
                return;
            }
        }
        System.out.println("Statusbot: Something went wrong while creating the Statusbot_Data folder at: " + dataFileFolder.getAbsoluteFile());
    }

    private static void saveAllData(File dataFileFolder) {
        File messageTextChannelsFile = new File(dataFileFolder, "message_text_channels.txt");
        File messagePrivateChannelsFile = new File(dataFileFolder, "message_private_channels.txt");
        File embedMessagesFile = new File(dataFileFolder, "embed_messages.txt");
        File privateEmbedsFile = new File(dataFileFolder, "private_embed_messages.txt");

        if (createFiles(messageTextChannelsFile, messagePrivateChannelsFile, embedMessagesFile, privateEmbedsFile)) {
            writeLongsDataFile(messageTextChannelsFile, BotManager.messageTextChannels);
            writeDoubleLongsDataFile(messagePrivateChannelsFile, BotManager.messagePrivateChannels);
            writeEmbedDataFiles(embedMessagesFile, privateEmbedsFile, EmbedManager.sentEmbeds);
        }
    }

    private static void writeLongsDataFile(File file, List<Long> longList) {
        try {
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
                for (int i = 0; i < longList.size(); i++) {
                    writer.write(Long.toString(longList.get(i)));
                    if (i != longList.size() - 1) {
                        writer.write("\n");
                    }
                }
            }
            System.out.println("Statusbot: Successfully wrote registered channels to a data file");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void writeDoubleLongsDataFile(File file, List<UserChannelPair> longList) {
        try {
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
                for (int i = 0; i < longList.size(); i++) {
                    writer.write(longList.get(i).user + "\n");
                    writer.write(Long.toString(longList.get(i).channel));
                    if (i != longList.size() - 1) {
                        writer.write("\n");
                    }
                }
            }
            System.out.println("Statusbot: Successfully wrote registered channels to a data file");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void writeEmbedDataFiles(File embed_file, File private_embed_file, List<SentEmbedData> embedDataList) {
        try {
            try (BufferedWriter private_writer = new BufferedWriter(new FileWriter(private_embed_file));
                 BufferedWriter writer = new BufferedWriter(new FileWriter(embed_file))) {
                for (SentEmbedData sentEmbedData : embedDataList) {
                    if (sentEmbedData.isInPrivateChannel) {
                        private_writer.write(sentEmbedData.channel + "\n");
                        private_writer.write(sentEmbedData.message + "\n");
                        private_writer.write(sentEmbedData.user + "\n");
                    } else {
                        writer.write(sentEmbedData.channel + "\n");
                        writer.write(sentEmbedData.message + "\n");
                    }
                }

            }
            System.out.println("Statusbot: Successfully wrote sent embeds to a data file");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
