package com.sas_apps.quickblox.adaptor;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.amulyakhare.textdrawable.TextDrawable;
import com.amulyakhare.textdrawable.util.ColorGenerator;
import com.quickblox.chat.model.QBChatDialog;
import com.quickblox.content.QBContent;
import com.quickblox.content.model.QBFile;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.exception.QBResponseException;
import com.sas_apps.quickblox.R;
import com.sas_apps.quickblox.qb.common.holder.QBUnreadCountHolder;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

/*
 * Created by Shashank Shinde.
 */

public class ChatListAdaptor extends BaseAdapter {

    private static final String TAG = "ChatListAdaptor";
    Context context;
    ArrayList<QBChatDialog> qbChatDialogsList;
    TextView textChatHeader, textChatMessage;
    ImageView imageChat, imageUnreadCount;

    public ChatListAdaptor(Context context, ArrayList<QBChatDialog> qbChatDialogsList) {
        this.context = context;
        this.qbChatDialogsList = qbChatDialogsList;
    }

    @Override
    public int getCount() {
        return qbChatDialogsList.size();
    }

    @Override
    public Object getItem(int position) {
        return qbChatDialogsList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            LayoutInflater layoutInflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = layoutInflater.inflate(R.layout.layout_chat_list, null);
            textChatHeader = view.findViewById(R.id.text_chatListHeader);
            textChatMessage = view.findViewById(R.id.text_chatListMessage);
            imageChat = view.findViewById(R.id.image_chatList);
            imageUnreadCount = view.findViewById(R.id.image_unreadCount);
            textChatHeader.setText(qbChatDialogsList.get(position).getName());
            textChatMessage.setText(qbChatDialogsList.get(position).getLastMessage());

            ColorGenerator colorGenerator = ColorGenerator.MATERIAL;
            int random = colorGenerator.getRandomColor();
            if (qbChatDialogsList.get(position).getPhoto().equals("null")) {
                TextDrawable.IBuilder builder = TextDrawable
                        .builder()
                        .beginConfig()
                        .width(4)
                        .endConfig()
                        .round();
                TextDrawable textDrawable = builder
                        .build(textChatHeader
                                .getText()
                                .toString()
                                .substring(0, 1)
                                .toUpperCase(), random);
                imageChat.setImageDrawable(textDrawable);

            } else {
                QBContent.getFile(Integer.parseInt(qbChatDialogsList.get(position).getPhoto()))
                        .performAsync(new QBEntityCallback<QBFile>() {
                            @Override
                            public void onSuccess(QBFile qbFile, Bundle bundle) {
                                String imageUrl = qbFile.getPublicUrl();
                                Picasso.with(context)
                                        .load(imageUrl)
                                        .resize(50, 50)
                                        .centerCrop()
                                        .into(imageChat);
                            }

                            @Override
                            public void onError(QBResponseException e) {
                                Log.e(TAG, "onError: " + e.getMessage());
                            }
                        });
            }

            //  Unread count
            TextDrawable.IBuilder unreadCountBuilder = TextDrawable
                    .builder()
                    .beginConfig()
                    .width(4)
                    .endConfig()
                    .round();
            int unreadCount = QBUnreadCountHolder.getInstance().getBundle()
                    .getInt(qbChatDialogsList.get(position).getDialogId());
            Log.d(TAG, qbChatDialogsList.get(position).getName() + " getView: UnreadCount =" + unreadCount);
            if (unreadCount > 0) {
                TextDrawable textDrawableUnread = unreadCountBuilder
                        .build(String.valueOf(unreadCount),
                                Color.RED);
                imageUnreadCount.setImageDrawable(textDrawableUnread);
            }
        }

        return view;
    }
}
