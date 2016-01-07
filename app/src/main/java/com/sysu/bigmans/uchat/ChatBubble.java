package com.sysu.bigmans.uchat;

/**
 * Created by liyujie on 15/12/19.
 */
public class ChatBubble {
    private String sender;
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



    public Type getType() {
        return type;
    }

    public void setType(Type t) {
        type = t;
    }

}
