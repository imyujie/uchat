package minet.src;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import java.net.Socket;

import common.src.ChatManager;

/**
 * Created by liyujie on 15/12/9.
 */
public class Minet extends Thread {

    public static final int DEFAULT_PORT = 9000;
    private Handler handler;
    private String address;
    private Socket socket;
    private ChatManager client;
    private Context mContext;

    public Minet(Context c, String ip_address, Handler handler) {
        this.address = ip_address;
        this.mContext = c;
        this.handler = handler;
    }
    @Override
    public void run() {
        try {
            socket = new Socket(this.address, DEFAULT_PORT);
            client = new ChatManager(socket, this.address, handler);
            ClientSocketsManager.getInstance().add(this.address, client);
            new Thread(client).start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public ChatManager getChatManager() {
        return client;
    }

    public Handler getRevHandler() {
        return client.getRevHandler();
    }
}
