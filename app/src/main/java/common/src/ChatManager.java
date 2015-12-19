package common.src;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;

/**
 * Created by liyujie on 15/12/18.
 */
public class ChatManager implements Runnable {
    public Socket socket;
    private String address;
    private Handler handler; // send msg to ui thread
    private Handler revHandler; // receive msg from ui thread
    private InputStream in;
    private OutputStream out;
    public static final int SEND_MSG_TO_UI_THREAD = 0x123;
    public static final int RECEIVE_MSG_FROM_UI_THREAD = 0x345;
    public static final int SEND_CONTACT_MSG_TO_UI_THREAD = 0x456;

    public ChatManager(Socket socket, String ip, Handler handler) {
        this.socket = socket;
        this.address = ip;
        this.handler = handler;
    }

    @Override
    public void run() {
        Log.d("CHAT_MANAGER", "The thread is running");
        try {
            in = this.socket.getInputStream();
            out = this.socket.getOutputStream();

            Log.d("CHAT_MANAGER", "Before enter loop!");
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        String content = null;
                        BufferedReader bReader = new BufferedReader(new InputStreamReader(in));
                        while ((content = bReader.readLine()) != null) {
                            UMessage umsg = new UMessage(
                                    ChatManager.this.socket.getInetAddress().getHostAddress(), "Temp", content);
                            Message msg = new Message();
                            msg.what = SEND_MSG_TO_UI_THREAD;
                            msg.obj = umsg;
                            Log.d("CHAT_MANAGER", "The content is: " + content);
                            handler.sendMessage(msg);

                        }
                        Log.d("CHAT_MANAGER", "End while");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }
            }).start();
            Looper.prepare();
            revHandler = new Handler() {
                @Override
                public void handleMessage(Message msg) {
                    if (msg.what == RECEIVE_MSG_FROM_UI_THREAD) {
                        write(msg);
                    }
                }
            };

            Log.d("CHAT_MANAGER", "before looper.loop()");
            Looper.loop();
            Log.d("CHAT_MANAGER", "Looper ended!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void write(Message msg) {
        try {
            out.write((msg.obj.toString() + "\r\n").getBytes("UTF-8"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Handler getRevHandler() {
        return revHandler;
    }
}
