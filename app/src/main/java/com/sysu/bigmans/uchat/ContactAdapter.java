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
public class ContactAdapter extends BaseAdapter {

    private LayoutInflater layoutInflater;
    private List<Contact> contactList;
    private Context context;


    public ContactAdapter(Context c) {
        contactList = new ArrayList<Contact>();
        context = c;
        this.layoutInflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return contactList.size();
    }

    @Override
    public Object getItem(int position) {
        return contactList.get(position);
    }

    public Contact getContactAt(int position) {
        return contactList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Contact c = contactList.get(position);
        convertView = layoutInflater.inflate(R.layout.chat_list_item, parent, false);
        TextView nameField = (TextView) convertView.findViewById(R.id.name);
        TextView addrField = (TextView) convertView.findViewById(R.id.address);
        nameField.setText(c.getName());
        addrField.setText(c.getAddress());

        return convertView;
    }

    public void addContact(Contact c) {
        contactList.add(c);
        notifyDataSetChanged();
    }

    public void addContacts(ArrayList<Contact> contacts) {
        contactList.addAll(contacts);
        notifyDataSetChanged();
    }
}
