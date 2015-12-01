package com.sysu.bigmans.uchat;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatActivity extends AppCompatActivity {

    private void renderListView() {
        ListView chatList = (ListView) findViewById(R.id.bubble_list);
        String[] msgs = new String[] {"我有一只小毛驴我从来都不骑，啦啦啦，我是用来充数的拉", "哈哈哈，哦~"};
        List<Map<String, Object>> listItems = new ArrayList<Map<String, Object>>();

        for (int i = 0; i < msgs.length; i++) {
            Map<String, Object> listItem = new HashMap<String, Object>();
            listItem.put("msg", msgs[i]);
            listItems.add(listItem);
        }
        SimpleAdapter simpleAdapter = new SimpleAdapter(this, listItems, R.layout.bubble_item,
                new String[]{"msg"}, new int[] {R.id.bubble_text});

        chatList.setAdapter(simpleAdapter);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        Intent intent = getIntent();
        String message = intent.getStringExtra("target");
        Log.d("mylog", message);
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
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

}
