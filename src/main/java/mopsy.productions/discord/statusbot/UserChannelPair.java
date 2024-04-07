package mopsy.productions.discord.statusbot;

public class UserChannelPair {
    public long user;
    public long channel;
    public UserChannelPair(long user, long channel){
        this.user = user;
        this.channel = channel;
    }

    @Override
    public boolean equals(Object obj) {
        if(super.equals(obj)) return true;
        if(obj instanceof UserChannelPair){
            UserChannelPair ucp = (UserChannelPair)obj;
            return user == ucp.user && channel == ucp.channel;
        }
        return false;
    }
}
