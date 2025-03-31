package mopsy.productions.discord.statusbot;

public class SentEmbedData {
    public boolean isInPrivateChannel=false;
    public long channel;
    public long user=0;
    public long message;
    public SentEmbedData(long message,long user, long channel){
        this.user = user;
        this.channel = channel;
        isInPrivateChannel = true;
        this.message = message;
    }
    public SentEmbedData(long message,long channel){
        this.channel = channel;
        this.message = message;
    }
}
