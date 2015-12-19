package com.sysu.bigmans.uchat;

/**
 * Created by liyujie on 15/12/19.
 */
public class ChatBubble {
    private String sender;
    private String content;
    public static enum Type {
        RECV,
        SEND
    };

    private Type type;

    public String getSender() {
        return sender;
    }

    public void setSender(String s) {
        sender = s;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String s) {
        content = s;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type t) {
        type = t;
    }

}
