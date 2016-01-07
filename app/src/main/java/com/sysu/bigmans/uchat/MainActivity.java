package com.sysu.bigmans.uchat;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;


public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    private ContactAdapter cAdapter;
    public static final int ROLE_SERVER = 1;
    public static final int ROLE_CLIENT = 0;
    public static final String serverIP = "192.168.43.22";
    private Dialog alertDialog;
    private LayoutInflater layoutInflater;
    private ListView contactList;

    public class ActivityReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            String ip_addr;
            switch (action) {
                case "com.sysu.bigmans.uchat.ADD_CONTACT_BROADCAST":
                    ip_addr = intent.getStringExtra("address");
                    Contact c = new Contact("Anonymous", "", ip_addr, ROLE_SERVER);
                    MainActivity.this.addContact(c);
                    break;
                case "com.sysu.bigmans.uchat.RECEIVE_MSG":
                    ip_addr = intent.getStringExtra("address");
                    Log.i("MAIN_ACTIVITY", "New message comes!");
                    // TODO: NOTIFY USERS
                    break;
                default:
                    break;
            }

        }
    }
    private ActivityReceiver activityReceiver;


    private void renderListView() {
        contactList = (ListView) findViewById(R.id.chatsList);
        cAdapter = new ContactAdapter(this);
        contactList.setAdapter(cAdapter);
    }

    public void addContact(Contact c) {
        cAdapter.addContact(c);
    }

    public void addChatListener() {
        final MainActivity that = this;
        contactList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Contact c = cAdapter.getContactAt(position);
                String ip_addr = c.getAddress();
                String name = c.getName();

                Intent intent = new Intent(that, SingleChatActivity.class);
                intent.putExtra("name", name);
                intent.putExtra("address", ip_addr);
                intent.putExtra("role", c.getRole());
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        this.layoutInflater = (LayoutInflater) this
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View inputView = layoutInflater.inflate(R.layout.input_dialog, null);
        alertDialog = new AlertDialog.Builder(this)
                .setTitle("请输入IP地址")
                .setView(inputView)
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Log.i("MAIN_ACTIVITY", "Confirm button was clicked!");
                        EditText t = (EditText) inputView.findViewById(R.id.input_address);
                        String ip_address = t.getText().toString();

                        ((UApplication)getApplication()).createClient(ip_address);

                        Log.i("MAIN_ACTIVITY", "Started client");

                        Contact c = new Contact("Anonymous", "", ip_address, ROLE_CLIENT);
                        addContact(c);
                        Log.i("MAIN_ACTIVITY", "IP(" + ip_address + ") was added!");

                        alertDialog.hide();
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        alertDialog.hide();
                    }
                })
                .create();

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alertDialog.show();
            }
        });

        // 加载 ListView
        renderListView();

        IntentFilter filter = new IntentFilter();
        filter.addAction("com.sysu.bigmans.uchat.ADD_CONTACT_BROADCAST");
        filter.addAction("com.sysu.bigmans.uchat.RECEIVE_MSG");

        activityReceiver = new ActivityReceiver();
        registerReceiver(activityReceiver, filter);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        // 添加点击事件监听器
        addChatListener();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camara) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {
            Intent intent = new Intent(MainActivity.this, GroupChatActivity.class);
            ((UApplication)getApplication()).createGroupClient(serverIP);
            intent.putExtra("name", "test");
            intent.putExtra("address", serverIP);
            intent.putExtra("role", ROLE_CLIENT);
            startActivity(intent);
        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
