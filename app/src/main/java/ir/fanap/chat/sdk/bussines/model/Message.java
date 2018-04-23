package ir.fanap.chat.sdk.bussines.model;

public class Message {

    private String peerName;
    private String content;
    private long[] receivers;
    private int priority;
    private long messageId;
    private long ttl;

    public String getPeerName() {
        return peerName;
    }

    public void setPeerName(String peerName) {
        this.peerName = peerName;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public long[] getReceivers() {
        return receivers;
    }

    public void setReceivers(long[] receivers) {
        this.receivers = receivers;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public long getMessageId() {
        return messageId;
    }

    public void setMessageId(long messageId) {
        this.messageId = messageId;
    }

    public long getTtl() {
        return ttl;
    }

    public void setTtl(long ttl) {
        this.ttl = ttl;
    }
}
