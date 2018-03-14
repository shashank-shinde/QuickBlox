package com.sas_apps.quickblox.adaptor;
/*
 * Created by Shashank Shinde.
 */

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.quickblox.users.model.QBUser;

import java.util.ArrayList;

public class UsersListAdaptor extends BaseAdapter {

    private Context context;
    private ArrayList<QBUser> qbUsersList;

    public UsersListAdaptor(Context context, ArrayList<QBUser> qbUsersList) {
        this.context = context;
        this.qbUsersList = qbUsersList;
    }

    @Override
    public int getCount() {
        return qbUsersList.size();
    }

    @Override
    public Object getItem(int position) {
        return qbUsersList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view=convertView;
        if(convertView==null){
            LayoutInflater layoutInflater= (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view=layoutInflater.inflate(android.R.layout.simple_list_item_multiple_choice,null);
            TextView textView=view.findViewById(android.R.id.text1);
            textView.setText(qbUsersList.get(position).getLogin());
        }

        return view;
    }
}
