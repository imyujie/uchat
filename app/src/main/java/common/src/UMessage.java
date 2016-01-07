package common.src;

import android.os.Message;

import java.io.File;

/**
 * Created by liyujie on 15/12/19.
 */
public class UMessage {
    private String senderAddress;
    private String senderName;

    private String receiverAddress;
    private String receiverName;

    private String contentType;
    private File file;
    private String content;
    private String filename;
    private long fileLength;

    private String chatType;
    private int chatRoom;


    public UMessage(String address, String name) {
        this.senderAddress = address;
        this.senderName= name;
    }

    public void setChatType(String chatType) {
        this.chatType = chatType;
    }

    public String getChatType() {
        return chatType;
    }

    public void setChatRoom(int chatRoom) {
        this.chatRoom = chatRoom;
    }

    public int getChatRoom() {
        return chatRoom;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getFilename() {
        String name = file.getName();
        return (name.split("\\."))[0];
    }

    public long getFileLength() {
        return file.length();
    }

    public void setFileLength(long fileLength) {
        this.fileLength = fileLength;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public File getFile() {
        return file;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getReceiverAddress() {
        return receiverAddress;
    }

    public void setReceiverAddress(String receiverAddress) {
        this.receiverAddress = receiverAddress;
    }

    public void setReceiverName(String receiverName) {
        this.receiverName = receiverName;
    }

    public String getReceiverName() {
        return receiverName;
    }

    public void setSenderAddress(String senderAddress) {
        this.senderAddress = senderAddress;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public String getContent() {
        return content;
    }

    public String getSenderAddress() {
        return senderAddress;
    }

    public String getSenderName() {
        return senderName;
    }


}
