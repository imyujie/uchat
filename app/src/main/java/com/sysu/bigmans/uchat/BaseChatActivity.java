package com.sysu.bigmans.uchat;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Message;
import android.os.PersistableBundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import com.nononsenseapps.filepicker.FilePickerActivity;

import java.io.File;

import common.src.ChatManager;
import common.src.UMessage;
import minet.src.ClientSocketsManager;
import mirot.src.ServerSocketsManager;

/**
 * Created by liyujie on 16/1/6.
 */
public class BaseChatActivity extends AppCompatActivity {

    protected BubbleAdapter bAdapter;
    protected ListView chatList;
    protected EditText edit_msg;
    public final static int FILE_CODE = 1;

    protected String address;
    protected String chatType;
    protected String name;
    protected ChatManager manager;
    protected Button fileSelectorBtn;

    protected UMessage produceUMessage() {

        UMessage uu = new UMessage("test", "Me");
        uu.setChatType(chatType);
        return uu;

    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i("Main", "onCreate");
        setContentView(R.layout.activity_chat);
        Intent intent = getIntent();
        name = intent.getStringExtra("name");
        address = intent.getStringExtra("address");
        int role = intent.getIntExtra("role", -1);

        Log.i("BASE_CHAT_ACTIVITY", "name: " + name + "; address: " + address + "; role: " + Integer.toString(role));

        if (role == MainActivity.ROLE_CLIENT) {
            manager = ClientSocketsManager.getInstance().get(address);
            Log.i("BASE_CHAT_ACTIVITY", "Role: ROLE_CLIENT");
        } else {
            manager = ServerSocketsManager.getInstance().get(address);
            Log.i("BASE_CHAT_ACTIVITY", "Role: ROLE_SERVER");
        }
        manager.setContext(this);
        renderListView();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        this.setTitle("test");

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.hide();

        addSelectButtonListener();

        edit_msg = (EditText) findViewById(R.id.edit_msg);

        Button sendButton = (Button) findViewById(R.id.send_msg);

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String text = edit_msg.getText().toString();
                Log.i("CHAT_ACTIVITY", "The text is: " + text);
                try {
                    Message msg = new Message();

                    UMessage umsg = BaseChatActivity.this.produceUMessage();
                    umsg.setContentType("text");
                    umsg.setContent(text);

                    msg.what = ChatManager.RECEIVE_MSG_FROM_UI_THREAD;
                    msg.obj = umsg;

                    TextBubble m = new TextBubble();
                    m.setContent(text);
                    m.setSender("test sender");
                    m.setType(ChatBubble.Type.SEND);

                    bAdapter.addMessage(m);

                    Log.i("BASE_CHAT_ACTIVITY", "Append Message!");

                    manager.getRevHandler().sendMessage(msg);// 将消息发送给 Socket 线程
                    edit_msg.setText("");

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }


    protected void addSelectButtonListener() {
        fileSelectorBtn = (Button) findViewById(R.id.choose_file);
        fileSelectorBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i("CHAT_ACTIVITY", "File button clicked!");

                Intent i = new Intent(BaseChatActivity.this, FilePickerActivity.class);

                i.putExtra(FilePickerActivity.EXTRA_ALLOW_MULTIPLE, false);
                i.putExtra(FilePickerActivity.EXTRA_ALLOW_CREATE_DIR, false);
                i.putExtra(FilePickerActivity.EXTRA_MODE, FilePickerActivity.MODE_FILE);

                i.putExtra("name", BaseChatActivity.this.name);
                i.putExtra("address", BaseChatActivity.this.address);

                i.putExtra(FilePickerActivity.EXTRA_START_PATH, Environment.getExternalStorageDirectory().getPath());

                startActivityForResult(i, FILE_CODE);
            }
        });
    }
    protected class ActivityReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String content = intent.getStringExtra("content");
            String senderAddress = intent.getStringExtra("sender");
            String contentType = intent.getStringExtra("content-type");
            File file = (File)intent.getExtras().get("file");

            if (senderAddress.equals(address)) {
                if (contentType.equals("text")) {
                    TextBubble m = new TextBubble();
                    m.setContent(content);
                    m.setType(ChatBubble.Type.RECV);
                    m.setSender("");
                    bAdapter.addMessage(m);
                } else {
                    FileBubble m = new FileBubble();
                    m.setFile(file);
                    m.setFileType(contentType);
                    m.setType(ChatBubble.Type.RECV);
                    m.setSender("");
                    bAdapter.addMessage(m);
                }

            }
        }
    }
    protected ActivityReceiver activityReceiver;

    protected void addItemListener() {
        chatList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ChatBubble cb = (ChatBubble) bAdapter.getItem(position);
                if (cb instanceof FileBubble) {
                    FileBubble fb = (FileBubble) cb;
                    Intent intent = new Intent("android.intent.action.VIEW");
                    intent.addCategory("android.intent.category.DEFAULT");
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    Uri uri = Uri.fromFile(fb.getFile());
                    String mimeType = BaseChatActivity.this.getMIMEType(fb.getFile());
                    intent.setDataAndType(uri, mimeType);
                    startActivity(intent);
                }
            }
        });
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == FILE_CODE && resultCode == Activity.RESULT_OK) {
            if (data.getBooleanExtra(FilePickerActivity.EXTRA_ALLOW_MULTIPLE, false)) {
                // For JellyBean and above
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    ClipData clip = data.getClipData();

                    if (clip != null) {
                        for (int i = 0; i < clip.getItemCount(); i++) {
                            Uri uri = clip.getItemAt(i).getUri();
                            // Do something with the URI
                        }
                    }
                    // For Ice Cream Sandwich
                }

            } else {
                Uri uri = data.getData();
                Log.i("CHAT_ACTIVITY", uri.toString());
                Log.i("CHAT_ACTIVITY", "File: " + uri.getPath());
                File file = new File(uri.getPath());

                // get post fix
                String fileName=file.getName();
                Log.i("CHAT_ACTIVITY", fileName);
                String[] token = fileName.split("\\.");
                Log.i("CHAT_ACTIVITY", token[0]);
                Log.i("CHAT_ACTIVITY", token[1]);
                String postFix = token[1];

                Log.i("CHAT_ACTIVITY", "Postfix: " + postFix);

                UMessage umsg = produceUMessage();
                umsg.setContentType(postFix);

                umsg.setFile(file);
                umsg.setFilename("yes");

                // send message
                BaseChatActivity.this.manager.sendMessage(umsg);
                // Do something with the URI
                FileBubble fmsg = new FileBubble();
                fmsg.setFile(file);
                fmsg.setType(ChatBubble.Type.SEND);
                fmsg.setSender("");

                BaseChatActivity.this.bAdapter.addMessage(fmsg);
            }
        }
    }


    protected String getMIMEType(File file) {
        String type="*/*";
        String fName=file.getName();
        //获取后缀名前的分隔符"."在fName中的位置。
        int dotIndex = fName.lastIndexOf(".");
        if(dotIndex < 0){
            return type;
        }
        /* 获取文件的后缀名 */
        String end=fName.substring(dotIndex,fName.length()).toLowerCase();
        if(end=="")return type;
        //在MIME和文件类型的匹配表中找到对应的MIME类型。
        for(int i=0;i<MIME_MapTable.length;i++){
            if(end.equals(MIME_MapTable[i][0]))
                type = MIME_MapTable[i][1];
        }
        return type;
    }

    protected void renderListView() {
        chatList = (ListView) findViewById(R.id.bubble_list);
        bAdapter = new BubbleAdapter(this);
        chatList.setAdapter(bAdapter);
    }

    private final String[][] MIME_MapTable={
            //{后缀名，    MIME类型}
            {".3gp",    "video/3gpp"},
            {".apk",    "application/vnd.android.package-archive"},
            {".asf",    "video/x-ms-asf"},
            {".avi",    "video/x-msvideo"},
            {".bin",    "application/octet-stream"},
            {".bmp",      "image/bmp"},
            {".c",        "text/plain"},
            {".class",    "application/octet-stream"},
            {".conf",    "text/plain"},
            {".cpp",    "text/plain"},
            {".doc",    "application/msword"},
            {".exe",    "application/octet-stream"},
            {".gif",    "image/gif"},
            {".gtar",    "application/x-gtar"},
            {".gz",        "application/x-gzip"},
            {".h",        "text/plain"},
            {".htm",    "text/html"},
            {".html",    "text/html"},
            {".jar",    "application/java-archive"},
            {".java",    "text/plain"},
            {".jpeg",    "image/jpeg"},
            {".jpg",    "image/jpeg"},
            {".js",        "application/x-javascript"},
            {".log",    "text/plain"},
            {".m3u",    "audio/x-mpegurl"},
            {".m4a",    "audio/mp4a-latm"},
            {".m4b",    "audio/mp4a-latm"},
            {".m4p",    "audio/mp4a-latm"},
            {".m4u",    "video/vnd.mpegurl"},
            {".m4v",    "video/x-m4v"},
            {".mov",    "video/quicktime"},
            {".mp2",    "audio/x-mpeg"},
            {".mp3",    "audio/x-mpeg"},
            {".mp4",    "video/mp4"},
            {".mpc",    "application/vnd.mpohun.certificate"},
            {".mpe",    "video/mpeg"},
            {".mpeg",    "video/mpeg"},
            {".mpg",    "video/mpeg"},
            {".mpg4",    "video/mp4"},
            {".mpga",    "audio/mpeg"},
            {".msg",    "application/vnd.ms-outlook"},
            {".ogg",    "audio/ogg"},
            {".pdf",    "application/pdf"},
            {".png",    "image/png"},
            {".pps",    "application/vnd.ms-powerpoint"},
            {".ppt",    "application/vnd.ms-powerpoint"},
            {".prop",    "text/plain"},
            {".rar",    "application/x-rar-compressed"},
            {".rc",        "text/plain"},
            {".rmvb",    "audio/x-pn-realaudio"},
            {".rtf",    "application/rtf"},
            {".sh",        "text/plain"},
            {".tar",    "application/x-tar"},
            {".tgz",    "application/x-compressed"},
            {".txt",    "text/plain"},
            {".wav",    "audio/x-wav"},
            {".wma",    "audio/x-ms-wma"},
            {".wmv",    "audio/x-ms-wmv"},
            {".wps",    "application/vnd.ms-works"},
            //{".xml",    "text/xml"},
            {".xml",    "text/plain"},
            {".z",        "application/x-compress"},
            {".zip",    "application/zip"},
            {"",        "*/*"}
    };

}
