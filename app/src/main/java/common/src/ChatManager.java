package common.src;

import android.content.Context;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.util.ArrayList;

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
    private Header header;
    private Context ctx;
    public static final int SEND_MSG_TO_UI_THREAD = 0x123;
    public static final int RECEIVE_MSG_FROM_UI_THREAD = 0x345;
    public static final int SEND_CONTACT_MSG_TO_UI_THREAD = 0x456;
    public static final String CRLF = "\r\n";

    public ChatManager(Socket socket, String ip, Handler handler) {
        this.socket = socket;
        this.address = ip;
        this.handler = handler;
    }

    @Override
    public void run() {
        Log.i("CHAT_MANAGER", "The thread is running");
        try {
            in = this.socket.getInputStream();
            out = this.socket.getOutputStream();

            Log.i("CHAT_MANAGER", "Before enter loop!");

            // this thread is used for receive bytes from other peer
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        receiveAndHandleBytes();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }
            }).start();

            Looper.prepare();

            revHandler = new Handler() {
                @Override
                public void handleMessage(Message sendMsg) {
                    // Receive message from ui thread
                    // Then write to outputstream
                    if (sendMsg.what == RECEIVE_MSG_FROM_UI_THREAD) {
                        UMessage umsg = (UMessage)sendMsg.obj;
                        ChatManager.this.sendMessage(umsg);
                    }
                }
            };

            Log.i("CHAT_MANAGER", "before looper.loop()");
            Looper.loop();
            Log.i("CHAT_MANAGER", "Looper ended!");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 解析 HTTP 头部，以 CRLF 表示一行
     * @param bis
     * @throws IOException
     */
    public void parseHeader(BufferedInputStream bis) throws IOException {
        header = new Header();
        int b;
        byte[] bytes = new byte[1024 * 2];
        int count = 0;
        Log.i("CHAT_MANAGER", "Starting!!!!");

        while ((b = bis.read()) != -1) {
            if (b == 13) {// cr
                int nextByte = bis.read();
                if (nextByte == 10) { //lf

                    if (count != 0) { // header
                        String headerLine = new String(bytes, "UTF-8");
                        Log.i("CHAT_MANAGER", "Header Line: " + headerLine.trim());
                        handleHeaderLine(headerLine.trim());
                        bytes = new byte[1024 * 2];
                        count = 0;
                    } else { // body
                        break;
                    }

                } else {
                    bytes[count++] = (byte) b;
                }
            } else {
                bytes[count++] = (byte) b;
            }
        }
    }

    /**
     * 接收从其他节点到达的字节，并且将这个字节流解析
     * 然后发送到 UI 线程。
     *
     * @throws IOException
     */
    public void receiveAndHandleBytes() throws IOException {

        Log.i("CHAT_MANAGER", "Receive bytes");

        BufferedInputStream bis = new BufferedInputStream(in);
        while (true) {

            parseHeader(bis);
            Log.i("CHAT_MANAGER", "Parse header finished!");

            UMessage umsg;
            Message msg = new Message();
            msg.what = SEND_MSG_TO_UI_THREAD;
            umsg = produceUMessage();
            umsg.setContentType(header.getContentType());

            umsg.setChatType(header.getChatType());
            umsg.setChatRoom(header.getChatRoom());

            if (header.getContentType().equals("text")) { // plain text

                Log.i("CHAT_MANAGER", "Plain text");
                ArrayList<Byte> bytesList;
                bytesList = new ArrayList<>();

                int bt;
                bt = bis.read();
                while (bt != 13 && bt != -1) {
                    bytesList.add(new Byte((byte)bt));
                    bt = bis.read();
                }
                Log.i("CHAT_MANAGER", "End read byte text");

                byte[] textBytes = new byte[bytesList.size()];

                for (int i = 0, len = bytesList.size(); i < len; i++) {
                    textBytes[i] = bytesList.get(i);
                }

                Log.i("CHAT_MANAGER", "End trans text");

                String content = new String(textBytes, "UTF-8");
                umsg.setContent(content);
                Log.i("CHAT_MANAGER", "The content is: " + content);

            } else { // file

                Log.i("CHAT_MANAGER", "File: " +header.getContentType());
                String filePath = Environment.getExternalStorageDirectory().getPath();
                Log.i("FILE_PATH", "filePath=====>" + filePath);
                File file = new File(filePath + "/" + header.getFileName() + "." + header.getContentType());
                FileOutputStream fos = new FileOutputStream(file);
                byte[] writeBytes = new byte[1024];
                int length;
                long len = 0;
                while ((length = bis.read(writeBytes)) != -1) {
                    fos.write(writeBytes, 0, length);
                    len += length;
                    if (len == header.getFileLength()) {
                        break;
                    }

                }
                Log.i("CHAT_MANAGER", "The file saved");
                umsg.setFile(file);
            }


            msg.obj = umsg;
            handler.sendMessage(msg);
            Log.i("CHAT_MANAGER", "send to UI Thread!");
        }

    }

    public void handleHeaderLine(String line) {
        header.addHeaderLine(line);
    }
    /**
     * 产生一条 UMessage, 这个 UMessage 的 Sender 是对方。
     * @return 这个 UMessage 实例
     */
    public UMessage produceUMessage() {
        return new UMessage(ChatManager.this.socket.getInetAddress().getHostAddress(), "He/She");
    }

    /**
     * 从此节点发送消息到目标节点，封装了消息的组装过程
     * @param umsg
     */
    public void sendMessage(UMessage umsg) {
        try {
            // send header
            String header = buildHeader(umsg);
            byte[] headerBytes = (header + CRLF).getBytes("UTF-8");
            write(headerBytes);

            // send body
            if (umsg.getContentType().equals("text")) {
                Log.i("CHAT_MANAGER", "type: text");
                writeString(umsg.getContent());
                out.write(13);
            } else {
                Log.i("CHAT_MANAGER", "type: file");
                writeFile(umsg.getFile());
            }

            // flush stream
//            out.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 创建一个协议头
     * @param umsg
     * @return
     */
    public String buildHeader(UMessage umsg) {
        String senderAddr = "Sender-Address: " + umsg.getSenderAddress();
        String senderName = "Sender-Name: " + umsg.getSenderName();
//        String receiverAddr = "Receiver-Address: " + umsg.getReceiverAddress();
//        String receiverName = "Receiver-Name: " + umsg.getReceiverName();
        String contentType = "Content-Type: " + umsg.getContentType();
        String chatType = "Chat-Type: " + umsg.getChatType();

        String head =  senderAddr + CRLF + senderName + CRLF + /*receiverAddr + CRLF + receiverName + CRLF + */
                contentType + CRLF + chatType + CRLF;

        if (umsg.getChatType().toLowerCase().equals("group-chat")) {
            head += "Chat-Room: " + "123" + CRLF;
        }
        if(!umsg.getContentType().equals("text")) {
            head += "File-Length: "+umsg.getFileLength() + CRLF;
            head += "File-Name: "+umsg.getFilename() + CRLF;
        }

        Log.i("CHAT_MANAGER", "HEADER: " + head);
        return head;
    }


    /**
     * 写一个 byte 序列到 OutputStream
     * @param bytes
     */
    public void write(byte[] bytes) {
        try {
            out.write(bytes);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 写 String 到 OutputStream。
     * @param s
     */
    public void writeString(String s) {
        try {
            byte[] bytes = (s + CRLF).getBytes("UTF-8");
            write(bytes);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 写 File 到 OutputStream。
     * @param file
     * @throws FileNotFoundException
     * @throws IOException
     */
    public void writeFile(File file) throws FileNotFoundException, IOException {
        FileInputStream fis = null;
        boolean isSuccess = false;
        try {
            fis = new FileInputStream(file);
            int length;
            int sendSum = 0;

            byte[] sendBytes = new byte[1024];

            while ((length = fis.read(sendBytes, 0, sendBytes.length)) > 0) {
                sendSum += length;
                out.write(sendBytes, 0, length);
            }
            out.flush();
            if (sendSum == file.length()) {
                isSuccess = true;
            }
        } catch (FileNotFoundException fe) {
            throw fe;
        } catch (IOException e) {
            throw e;
        } finally {
//            out.close();

            if (fis != null) {
                fis.close();
            }
        }

        Log.i("MANAGER", String.valueOf(isSuccess));

    }

    public void setContext(Context ctx) {
        this.ctx = ctx;
    }
    public void saveFile(String fileName, byte[] bytes) {
        try {
            File file = new File(fileName);
            if (!file.exists()) {
                file.createNewFile();
            }
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(bytes);
            fos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e1) {
            e1.printStackTrace();
        }

    }

    public void write(Message msg) {
        try {
            out.write((msg.obj.toString() + CRLF).getBytes("UTF-8"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void receiveBytes() {
        byte[] recvByte = null;
        int length = 0;
        DataInputStream dis = null;

        try {
            dis = new DataInputStream(in);
            recvByte = new byte[1024];
            while ((length = dis.read(recvByte, 0, recvByte.length)) > 0) {

            }
        } catch (IOException e) {

        }
    }


    public Handler getRevHandler() {
        return revHandler;
    }
}
