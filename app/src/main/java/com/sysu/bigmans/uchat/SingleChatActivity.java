package com.sysu.bigmans.uchat;

import android.content.IntentFilter;
import android.os.Bundle;

public class SingleChatActivity extends BaseChatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        chatType = "single-chat";
        super.onCreate(savedInstanceState);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        addItemListener();
        activityReceiver = new ActivityReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction("com.sysu.bigmans.uchat.RECEIVE_MSG");
        registerReceiver(activityReceiver, filter);

    }

}
