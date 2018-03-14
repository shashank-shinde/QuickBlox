package com.sas_apps.quickblox.chat.chat_message;

import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.quickblox.chat.QBChatService;
import com.quickblox.chat.QBIncomingMessagesManager;
import com.quickblox.chat.QBRestChatService;
import com.quickblox.chat.exception.QBChatException;
import com.quickblox.chat.listeners.QBChatDialogMessageListener;
import com.quickblox.chat.model.QBChatDialog;
import com.quickblox.chat.model.QBChatMessage;
import com.quickblox.chat.model.QBDialogType;
import com.quickblox.chat.request.QBMessageGetBuilder;
import com.quickblox.chat.request.QBMessageUpdateBuilder;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.exception.QBResponseException;
import com.sas_apps.quickblox.R;
import com.sas_apps.quickblox.adaptor.ChatMessageAdaptor;
import com.sas_apps.quickblox.qb.common.holder.QBChatMsgHolder;
import com.sas_apps.quickblox.utils.Utils;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smackx.muc.DiscussionHistory;

import java.util.ArrayList;

public class ChatMessageActivity extends AppCompatActivity implements QBChatDialogMessageListener {

    private static final String TAG = "ChatMessageActivity";
    EditText editMessage;
    ListView listChat;
    QBChatDialog qbChatDialog;
    FloatingActionButton fabSend, fabEmoticon;
    ChatMessageAdaptor adaptor;

    int menuIndexClicked = -1;
    boolean isEditMode;
    QBChatMessage editedMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_message);
        listChat = findViewById(R.id.list_chatHistory);
        editMessage = findViewById(R.id.edit_inputMessage);
        fabSend = findViewById(R.id.fab_sendMessage);
        fabEmoticon = findViewById(R.id.fab_emoticon);
        initChat();
        loadAllMessages();
        fabSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage(editMessage.getText().toString());
            }
        });

        registerForContextMenu(listChat);
    }

    private void sendMessage(String message) {
        if (!isEditMode) {

            QBChatMessage qbChatMessage = new QBChatMessage();
            qbChatMessage.setBody(message);
            qbChatMessage.setSenderId(QBChatService.getInstance().getUser().getId());
            qbChatMessage.setSaveToHistory(true);
            try {
                qbChatDialog.sendMessage(qbChatMessage);
            } catch (SmackException.NotConnectedException e) {
                Log.e(TAG, "sendMessage: " + e.getMessage());
                e.printStackTrace();
            }

//        QBChatMsgHolder.getInstance().putMessage(qbChatDialog.getDialogId(), qbChatMessage);
//        ArrayList<QBChatMessage> messages = QBChatMsgHolder.getInstance().getChatMessageByDialogId(qbChatDialog.getDialogId());
//        adaptor = new ChatMessageAdaptor(getBaseContext(), messages);
//        listChat.setAdapter(adaptor);
//        adaptor.notifyDataSetChanged();


            //  bug fix
            if (qbChatDialog.getType() == QBDialogType.PRIVATE) {
                QBChatMsgHolder.getInstance().putMessage(qbChatMessage.getDialogId(), qbChatMessage);
                ArrayList<QBChatMessage> messages = QBChatMsgHolder.getInstance().getChatMessageByDialogId(qbChatMessage.getDialogId());
                adaptor = new ChatMessageAdaptor(getBaseContext(), messages);
                listChat.setAdapter(adaptor);
                adaptor.notifyDataSetChanged();
            }
            editMessage.setText("");
            editMessage.setFocusable(true);
        } else {
            Toast.makeText(this, "Updating message", Toast.LENGTH_SHORT).show();
            QBMessageUpdateBuilder messageUpdateBuilder = new QBMessageUpdateBuilder();
            messageUpdateBuilder.updateText(editMessage.getText().toString()).markDelivered().markRead();
            QBRestChatService.updateMessage(editedMessage.getId(), qbChatDialog.getDialogId(), messageUpdateBuilder)
                    .performAsync(new QBEntityCallback<Void>() {
                        @Override
                        public void onSuccess(Void aVoid, Bundle bundle) {
                            loadAllMessages();
                            isEditMode = false;
                            editMessage.setText("");
                        }

                        @Override
                        public void onError(QBResponseException e) {
                            Toast.makeText(ChatMessageActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                            Log.e(TAG, "onError: QBResponseException " + e.getMessage());
                        }
                    });
        }
    }

    private void loadAllMessages() {

        QBMessageGetBuilder messageGetBuilder = new QBMessageGetBuilder();
        messageGetBuilder.setLimit(100);
        if (qbChatDialog != null) {
            QBRestChatService.getDialogMessages(qbChatDialog, messageGetBuilder).performAsync(new QBEntityCallback<ArrayList<QBChatMessage>>() {
                @Override
                public void onSuccess(ArrayList<QBChatMessage> qbChatMessages, Bundle bundle) {
                    //

                    QBChatMsgHolder.getInstance().putMessages(qbChatDialog.getDialogId(), qbChatMessages);

                    adaptor = new ChatMessageAdaptor(getBaseContext(), qbChatMessages);
                    listChat.setAdapter(adaptor);
                    adaptor.notifyDataSetChanged();
                }

                @Override
                public void onError(QBResponseException e) {
                    Log.e(TAG, "onError: " + e.getMessage());
                }
            });
        }
    }

    private void initChat() {
        qbChatDialog = (QBChatDialog) getIntent().getSerializableExtra(Utils.CHAT_INTENT);
        qbChatDialog.initForChat(QBChatService.getInstance());
        // incoming message
        QBIncomingMessagesManager incomingMessagesManager = QBChatService.getInstance().getIncomingMessagesManager();
        incomingMessagesManager.addDialogMessageListener(new QBChatDialogMessageListener() {
            @Override
            public void processMessage(String s, QBChatMessage qbChatMessage, Integer integer) {

            }

            @Override
            public void processError(String s, QBChatException e, QBChatMessage qbChatMessage, Integer integer) {
                Log.e(TAG, "processError: " + e.getMessage());
            }
        });


        // Add join group
        if (qbChatDialog.getType() == QBDialogType.PUBLIC_GROUP || qbChatDialog.getType() == QBDialogType.GROUP) {
            DiscussionHistory discussionHistory = new DiscussionHistory();
            discussionHistory.setMaxStanzas(0);

            qbChatDialog.join(discussionHistory, new QBEntityCallback() {
                @Override
                public void onSuccess(Object o, Bundle bundle) {

                }

                @Override
                public void onError(QBResponseException e) {
                    Log.e(TAG, "onError: QBResponseException " + e.getMessage());
                }
            });
        }

        qbChatDialog.addMessageListener(this);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        getMenuInflater().inflate(R.menu.chat_msg_menu, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {

        AdapterView.AdapterContextMenuInfo menuInfo = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        menuIndexClicked = menuInfo.position;

        switch (item.getItemId()) {
            case R.id.menu_deleteMsg:
                deleteMessage();
                break;
            case R.id.menu_updateMsg:
                updateMessage();
                break;
        }
        return true;
    }

    private void deleteMessage() {
        Toast.makeText(this, "Deleting message", Toast.LENGTH_SHORT).show();
        editedMessage = QBChatMsgHolder.getInstance()
                .getChatMessageByDialogId(qbChatDialog.getDialogId())
                .get(menuIndexClicked);
        QBRestChatService.deleteMessage(editedMessage.getId(), false)
                .performAsync(new QBEntityCallback<Void>() {
                    @Override
                    public void onSuccess(Void aVoid, Bundle bundle) {
                        loadAllMessages();
                    }

                    @Override
                    public void onError(QBResponseException e) {
                        Log.e(TAG, "onError: QBResponseException " + e.getMessage());
                        Toast.makeText(ChatMessageActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void updateMessage() {
        editedMessage = QBChatMsgHolder.getInstance()
                .getChatMessageByDialogId(qbChatDialog.getDialogId())
                .get(menuIndexClicked);
        editMessage.setText(editedMessage.getBody());
        isEditMode = true;
    }

    @Override
    protected void onStop() {
        super.onStop();
        qbChatDialog.removeMessageListrener(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        qbChatDialog.removeMessageListrener(this);
    }

    @Override
    public void processMessage(String s, QBChatMessage qbChatMessage, Integer integer) {
        QBChatMsgHolder.getInstance().putMessage(qbChatMessage.getDialogId(), qbChatMessage);
        ArrayList<QBChatMessage> messages = QBChatMsgHolder.getInstance().getChatMessageByDialogId(qbChatMessage.getDialogId());
        adaptor = new ChatMessageAdaptor(getBaseContext(), messages);
        listChat.setAdapter(adaptor);
        adaptor.notifyDataSetChanged();
    }

    @Override
    public void processError(String s, QBChatException e, QBChatMessage qbChatMessage, Integer integer) {
        Log.e(TAG, "processError: QBChatException " + e.getMessage());
    }
}
