package mopsy.productions.discord.statusbot;

public class SentEmbedData {
    public boolean isInPrivateChannel=false;
    public long channel;
    public long user=0;
    public SentEmbedData(long user, long channel){
        this.user = user;
        this.channel = channel;
        isInPrivateChannel=true;
    }
    public SentEmbedData(long channel){
        this.channel = channel;
    }
}
