package com.sysu.bigmans.uchat;

import android.content.IntentFilter;
import android.os.Bundle;

public class GroupChatActivity extends BaseChatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        chatType = "group-chat";
        super.onCreate(savedInstanceState);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        addItemListener();
        activityReceiver = new ActivityReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction("com.sysu.bigmans.uchat.GROUP_MSG");
        registerReceiver(activityReceiver, filter);

    }

}
