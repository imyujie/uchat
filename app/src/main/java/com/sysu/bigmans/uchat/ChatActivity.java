package com.sysu.bigmans.uchat;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import common.src.ChatManager;
import minet.src.ClientSocketsManager;
import mirot.src.ServerSocketsManager;

public class ChatActivity extends AppCompatActivity {

    EditText edit_msg;
    ListView chatList;

    BubbleAdapter bAdapter;
    ChatManager manager;
    String name;
    String address;
    private class ActivityReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String content = intent.getStringExtra("content");
            String senderAddress = intent.getStringExtra("sender");

            if (senderAddress.equals(address)) {
                ChatBubble m = new ChatBubble();
                m.setContent(content);
                m.setType(ChatBubble.Type.RECV);
                m.setSender("");
                bAdapter.addMessage(m);
            }
        }
    }
    private ActivityReceiver activityReceiver;
    private void renderListView() {
        chatList = (ListView) findViewById(R.id.bubble_list);
        bAdapter = new BubbleAdapter(this);
        chatList.setAdapter(bAdapter);
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

        Log.d("CHAT_ACTIVITY", "name: " + name + "; address: " + address + "; role: " + Integer.toString(role));

        if (role == MainActivity.ROLE_CLIENT) {
            manager = ClientSocketsManager.getInstance().get(address);
            Log.d("CHAT_ACTIVITY", "Role: ROLE_CLIENT");
        } else {
            manager = ServerSocketsManager.getInstance().get(address);
            Log.d("CHAT_ACTIVITY", "Role: ROLE_SERVER");
        }

        renderListView();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        this.setTitle("test");

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        fab.hide();

        edit_msg = (EditText) findViewById(R.id.edit_msg);
        Button sendButton = (Button) findViewById(R.id.send_msg);

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String text = edit_msg.getText().toString();
                Log.d("CHAT_ACTIVITY", "The text is: " + text);
                try {
                    Message msg = new Message();
                    msg.what = ChatManager.RECEIVE_MSG_FROM_UI_THREAD;
                    msg.obj = text;

                    ChatBubble m = new ChatBubble();
                    m.setContent(text);
                    m.setSender("test sender");
                    m.setType(ChatBubble.Type.SEND);

                    bAdapter.addMessage(m);

                    manager.getRevHandler().sendMessage(msg);
                    edit_msg.setText("");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        activityReceiver = new ActivityReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction("com.sysu.bigmans.uchat.RECEIVE_MSG");


        registerReceiver(activityReceiver, filter);

    }

}
