package com.sas_apps.quickblox.chat.chat_list;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.quickblox.auth.QBAuth;
import com.quickblox.auth.session.BaseService;
import com.quickblox.auth.session.QBSession;
import com.quickblox.chat.QBChatService;
import com.quickblox.chat.QBIncomingMessagesManager;
import com.quickblox.chat.QBRestChatService;
import com.quickblox.chat.QBSystemMessagesManager;
import com.quickblox.chat.exception.QBChatException;
import com.quickblox.chat.listeners.QBChatDialogMessageListener;
import com.quickblox.chat.listeners.QBSystemMessageListener;
import com.quickblox.chat.model.QBChatDialog;
import com.quickblox.chat.model.QBChatMessage;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.exception.BaseServiceException;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.core.request.QBRequestGetBuilder;
import com.quickblox.users.QBUsers;
import com.quickblox.users.model.QBUser;
import com.sas_apps.quickblox.R;
import com.sas_apps.quickblox.adaptor.ChatListAdaptor;
import com.sas_apps.quickblox.chat.chat_message.ChatMessageActivity;
import com.sas_apps.quickblox.profile.ProfileActivity;
import com.sas_apps.quickblox.qb.common.holder.QBChatDialogHolder;
import com.sas_apps.quickblox.qb.common.holder.QBUnreadCountHolder;
import com.sas_apps.quickblox.qb.common.holder.QBUsersHolder;
import com.sas_apps.quickblox.utils.Utils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import static com.quickblox.auth.QBAuth.createSession;

public class ChatListActivity extends AppCompatActivity implements QBSystemMessageListener,
        QBChatDialogMessageListener {

    private static final String TAG = "ChatListActivity";
    ListView listChat;
    FloatingActionButton fabNewChat;
    ChatListAdaptor adaptor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_list);
        listChat = findViewById(R.id.list_chat);
        fabNewChat = findViewById(R.id.fab_newChat);

        createChatSession();
        loadChatHistory();
        fabNewChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ChatListActivity.this, UsersListActivity.class);
                startActivity(intent);
            }
        });
        listChat.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                QBChatDialog qbChatDialog = (QBChatDialog) listChat.getAdapter().getItem(position);
                Intent intent = new Intent(ChatListActivity.this, ChatMessageActivity.class);
                intent.putExtra(Utils.CHAT_INTENT, qbChatDialog);
                startActivity(intent);
            }
        });
        listChat.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                final QBChatDialog chatDialog = (QBChatDialog) listChat.getAdapter().getItem(position);
                QBRestChatService.deleteDialog(chatDialog.getDialogId(), false)
                        .performAsync(new QBEntityCallback<Void>() {
                            @Override
                            public void onSuccess(Void aVoid, Bundle bundle) {
                                QBChatDialogHolder.getInstance().removeDialog(chatDialog.getDialogId());
                                listChat.setAdapter(adaptor);
                                adaptor.notifyDataSetChanged();
                                Toast.makeText(ChatListActivity.this, "Deteted", Toast.LENGTH_SHORT).show();
                                Log.d(TAG, "onSuccess: Deleted");
                            }

                            @Override
                            public void onError(QBResponseException e) {
                                Log.e(TAG, "onError: " + e.getMessage());
                            }
                        });
                return true;
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadChatHistory();
    }

    private void loadChatHistory() {
        QBRequestGetBuilder requestGetBuilder = new QBRequestGetBuilder();
        requestGetBuilder.setLimit(100);
        QBRestChatService.getChatDialogs(null, requestGetBuilder)
                .performAsync(new QBEntityCallback<ArrayList<QBChatDialog>>() {
                    @Override
                    public void onSuccess(final ArrayList<QBChatDialog> qbChatDialogs, Bundle bundle) {

                        QBChatDialogHolder.getInstance().putDialogs(qbChatDialogs);
                        //   Unread count
//                        ChatListAdaptor adaptor = new ChatListAdaptor(getBaseContext(), qbChatDialogs);
//                        listChat.setAdapter(adaptor);
//                        adaptor.notifyDataSetChanged();

                        Set<String> setIds = new HashSet<>();
                        for (QBChatDialog chatDialog : qbChatDialogs) {
                            setIds.add(chatDialog.getDialogId());
                        }
                        //  get unread count
                        QBRestChatService.getTotalUnreadMessagesCount(setIds
                                , QBUnreadCountHolder.getInstance().getBundle())
                                .performAsync(new QBEntityCallback<Integer>() {
                                    @Override
                                    public void onSuccess(Integer integer, Bundle bundle) {
                                        QBUnreadCountHolder.getInstance().setBundle(bundle);
                                        adaptor = new ChatListAdaptor(getBaseContext()
                                                , QBChatDialogHolder.getInstance().getAllChatDialogs());
                                        listChat.setAdapter(adaptor);
                                        adaptor.notifyDataSetChanged();
                                    }

                                    @Override
                                    public void onError(QBResponseException e) {
                                        Log.e(TAG, "onError: QBResponseException " + e.getMessage());
                                    }
                                });

                    }

                    @Override
                    public void onError(QBResponseException e) {
                        Log.e(TAG, "onError: " + e.getMessage());
                    }
                });
    }

    private void createChatSession() {
        final ProgressDialog progressDialog = new ProgressDialog(ChatListActivity.this);
        progressDialog.setMessage("Searching history. Please wait...");
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();
        String userName, password;
        userName = getIntent().getStringExtra("userName");
        password = getIntent().getStringExtra("password");

        // Load all users and save to QBUserHolder

        QBUsers.getUsers(null).performAsync(new QBEntityCallback<ArrayList<QBUser>>() {
            @Override
            public void onSuccess(ArrayList<QBUser> qbUsers, Bundle bundle) {
                QBUsersHolder.getInstance().putUsers(qbUsers);
            }

            @Override
            public void onError(QBResponseException e) {
                Log.e(TAG, "onError: QBResponseException " + e.getMessage());
            }
        });

        Log.d(TAG, "createChatSession: USERNAME AND PASSWORD  " + userName + "  " + password);
        final QBUser qbUser = new QBUser(userName, password);
        QBAuth.createSession(qbUser).performAsync(new QBEntityCallback<QBSession>() {
            @Override
            public void onSuccess(QBSession qbSession, Bundle bundle) {
                qbUser.setId(qbSession.getUserId());
                try {
                    qbUser.setPassword(BaseService.getBaseService().getToken());
                } catch (BaseServiceException e) {
                    e.printStackTrace();
                    progressDialog.dismiss();
                }
                QBChatService.getInstance().login(qbUser, new QBEntityCallback() {
                    @Override
                    public void onSuccess(Object o, Bundle bundle) {
                        progressDialog.dismiss();
                        QBSystemMessagesManager systemMessagesManager = QBChatService.getInstance().getSystemMessagesManager();
                        systemMessagesManager.addSystemMessageListener(ChatListActivity.this);

                        //  incoming msg listener
                        QBIncomingMessagesManager incomingMessagesManager = QBChatService.getInstance().getIncomingMessagesManager();
                        incomingMessagesManager.addDialogMessageListener(ChatListActivity.this);
                    }

                    @Override
                    public void onError(QBResponseException e) {
                        progressDialog.dismiss();
                        Log.e(TAG, "onError: QBResponseException1 " + e.getMessage());
                    }
                });
            }

            @Override
            public void onError(QBResponseException e) {
                Log.e(TAG, "onError: QBResponseException2 " + e.getMessage());
            }
        });
    }


    @Override
    public void processMessage(QBChatMessage qbChatMessage) {

        //  Receive system message regarding creation of new chat

        QBRestChatService.getChatDialogById(qbChatMessage.getBody())
                .performAsync(new QBEntityCallback<QBChatDialog>() {
                    @Override
                    public void onSuccess(QBChatDialog qbChatDialog, Bundle bundle) {
                        QBChatDialogHolder.getInstance().putDialog(qbChatDialog);
                        ArrayList<QBChatDialog> adaptorSource = QBChatDialogHolder
                                .getInstance().getAllChatDialogs();
                        ChatListAdaptor adaptor = new ChatListAdaptor(getBaseContext(), adaptorSource);
                        listChat.setAdapter(adaptor);
                        adaptor.notifyDataSetChanged();
                    }

                    @Override
                    public void onError(QBResponseException e) {
                        Log.e(TAG, "onError: QBResponseException " + e.getMessage());
                    }
                });
    }

    @Override
    public void processError(QBChatException e, QBChatMessage qbChatMessage) {
        Log.e(TAG, "processError: QBChatException " + e.getMessage());
    }

    @Override
    public void processMessage(String s, QBChatMessage qbChatMessage, Integer integer) {
        loadChatHistory();
    }

    @Override
    public void processError(String s, QBChatException e, QBChatMessage qbChatMessage, Integer integer) {
        Log.e(TAG, "processError: " + e.getMessage());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.chat_list_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_profile:
                Intent intent = new Intent(ChatListActivity.this, ProfileActivity.class);
                startActivity(intent);
                break;
        }

        return true;
    }
}
