package mirot.src;

import java.util.HashMap;

import common.src.ChatManager;

/**
 * Created by liyujie on 15/12/18.
 */
public class ServerSocketsManager {
    private static ServerSocketsManager manager = new ServerSocketsManager();
    private HashMap<String, ChatManager> container;
    private ServerSocketsManager() {
        container = new HashMap<String, ChatManager>();
    }

    public static ServerSocketsManager getInstance() {
        return manager;
    }

    public void add(String address, ChatManager session) {
        container.put(address, session);
    }

    public ChatManager get(String address) {
        return container.get(address);
    }
}
