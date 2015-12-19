package com.sysu.bigmans.uchat;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by liyujie on 15/12/19.
 */
public class BubbleAdapter extends BaseAdapter {

    private static final int RECV = 1;
    private static final int SEND = 0;
    private LayoutInflater layoutInflater;
    private Context context;

    private List<ChatBubble> messageList;

    public BubbleAdapter(Context c) {
        context = c;
        messageList = new ArrayList<ChatBubble>();
        this.layoutInflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public BubbleAdapter(Context c, List<ChatBubble> oldList) {
        context = c;
        messageList = oldList;
        this.layoutInflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }
    @Override
    public int getCount() {
        return messageList.size();
    }

    @Override
    public Object getItem(int position) {
        return messageList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ChatBubble msg = messageList.get(position);
        if (getItemViewType(position) == RECV) {
            convertView = layoutInflater.inflate(R.layout.bubble_item, parent, false);
            TextView textview = (TextView) convertView.findViewById(R.id.bubble_text);
            textview.setText(msg.getContent());
        } else {
            convertView = layoutInflater.inflate(R.layout.bubble_item_send, parent, false);
            TextView textview = (TextView) convertView.findViewById(R.id.bubble_text);
            textview.setText(msg.getContent());
        }
        return convertView;
    }

    @Override
    public int getItemViewType(int position) {
        if (messageList.get(position).getType() == ChatBubble.Type.RECV) {
            return RECV;
        } else {
            return SEND;
        }
    }

    public void addMessage(ChatBubble msg) {
        messageList.add(msg);
        notifyDataSetChanged();
    }

    public ChatBubble getMessageAt(int position) {
        return messageList.get(position);
    }
}
