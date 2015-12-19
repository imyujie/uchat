package mirot.src;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import common.src.ChatManager;

/**
 * Created by liyujie on 15/12/9.
 */
public class Mirot extends Thread {
    public static ArrayList<Socket> socketList = new ArrayList<Socket>();
    public static final int DEFAULT_PORT = 9000;

    private ServerSocket socket = null;
    private boolean state = false; // true when looping
    private Handler handler;
    private ChatManager client;
    private Context mContext;

    public Mirot(Context c, Handler handler) {
        this.handler = handler;
        this.mContext = c;
        try {
            socket = new ServerSocket(DEFAULT_PORT);
            state = true;
            Log.d("MIROT", "Server started. Waiting for connect...");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    @Override
    public void run() {
        // blocking here
        while (state) {
            Log.d("MIROT", "Enter loop!");
            try {
                Socket clientSocket = socket.accept(); // create a new socket for connection
                String ip_address = clientSocket.getInetAddress().getHostAddress();
                sendIPAddress(ip_address);
                Log.d("MIROT", "Accepted socket from ip address: " + ip_address);

                client = new ChatManager(clientSocket, ip_address, handler);
                ServerSocketsManager.getInstance().add(ip_address, client); // Store this client manager

                new Thread(client).start();
                Log.d("MIROT", "Thread started...");
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

    private void sendIPAddress(String ip) {
        Message msg = new Message();
        msg.what = ChatManager.SEND_CONTACT_MSG_TO_UI_THREAD;
        msg.obj = ip;

        handler.sendMessage(msg);

    }
    public Handler getRevHandler() {
        return client.getRevHandler();
    }

    public ChatManager getChatManager() {
        return client;
    }
}

