package minet.src;

import android.util.Log;

import java.util.HashMap;

import common.src.ChatManager;

/**
 * Created by liyujie on 15/12/18.
 */
public class ClientSocketsManager {
    private HashMap<String, ChatManager> container;
    private static ClientSocketsManager manager = new ClientSocketsManager();
    private ClientSocketsManager() {
        container = new HashMap<String, ChatManager>();
    }
    public static ClientSocketsManager getInstance() {
        return manager;
    }

    public ChatManager get(String address) {
        return container.get(address);
    }
    public void add(String address, ChatManager session) {
        container.put(address, session);
        Log.i("CLIENT_SOCKETS_MANAGER", "IP address(" + address + ") was added in.");
    }


}
