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

import com.github.library.bubbleview.BubbleTextView;
import com.quickblox.chat.QBChatService;
import com.quickblox.chat.model.QBChatMessage;
import com.sas_apps.quickblox.R;
import com.sas_apps.quickblox.qb.common.holder.QBUsersHolder;

import java.util.ArrayList;

public class ChatMessageAdaptor extends BaseAdapter {

    private Context context;
    private ArrayList<QBChatMessage> qbChatMessages;

    public ChatMessageAdaptor(Context context, ArrayList<QBChatMessage> qbChatMessages) {
        this.context = context;
        this.qbChatMessages = qbChatMessages;
    }

    @Override
    public int getCount() {
        return qbChatMessages.size();
    }

    @Override
    public Object getItem(int position) {
        return qbChatMessages.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (convertView == null) {
            LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            assert layoutInflater != null;
            if (qbChatMessages.get(position).getSenderId().equals(QBChatService.getInstance().getUser().getId())) {
                view = layoutInflater.inflate(R.layout.layout_send_message, null);
                BubbleTextView bubbleTextView = view.findViewById(R.id.bubble_send_msg);
                bubbleTextView.setText(qbChatMessages.get(position).getBody());
            } else {

                view = layoutInflater.inflate(R.layout.layout_received_message, null);
                BubbleTextView bubbleTextView = view.findViewById(R.id.bubble_received_msg);
                bubbleTextView.setText(qbChatMessages.get(position).getBody());
                TextView sender = view.findViewById(R.id.text_msgSender);
                sender.setText(QBUsersHolder.getInstance().getUserById(qbChatMessages.get(position).getSenderId()).getFullName());

            }
        }

        return view;
    }
}
