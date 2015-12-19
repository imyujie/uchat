package common.src;

import android.os.Message;

/**
 * Created by liyujie on 15/12/19.
 */
public class UMessage {
    private String senderAddress;
    private String senderName;
    private String content;


    public UMessage(String address, String name, String content) {
        this.senderAddress = address;
        this.senderName= name;
        this.content = content;
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
