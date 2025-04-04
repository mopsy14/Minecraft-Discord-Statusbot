package mopsy.productions.discord.statusbot;

public class SentEmbedData {
    public boolean isInPrivateChannel=false;
    public long channel;
    public long user=0;
    public long message;
    public SentEmbedData(long channel, long message, long user){
        this.user = user;
        this.channel = channel;
        isInPrivateChannel = true;
        this.message = message;
    }
    public SentEmbedData(long channel, long message){
        this.channel = channel;
        this.message = message;
    }
}
