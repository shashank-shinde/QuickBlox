package com.sas_apps.quickblox.chat.chat_list;

import android.app.ProgressDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.quickblox.chat.QBChatService;
import com.quickblox.chat.QBRestChatService;
import com.quickblox.chat.QBSystemMessagesManager;
import com.quickblox.chat.model.QBChatDialog;
import com.quickblox.chat.model.QBChatMessage;
import com.quickblox.chat.model.QBDialogType;
import com.quickblox.chat.utils.DialogUtils;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.users.QBUsers;
import com.quickblox.users.model.QBUser;
import com.sas_apps.quickblox.R;
import com.sas_apps.quickblox.adaptor.UsersListAdaptor;
import com.sas_apps.quickblox.qb.common.Common;
import com.sas_apps.quickblox.qb.common.holder.QBUsersHolder;

import org.jivesoftware.smack.SmackException;

import java.util.ArrayList;

public class UsersListActivity extends AppCompatActivity {

    private static final String TAG = "UsersListActivity";
    ListView listUsers;
    Button buttonSelect;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users_list);
        listUsers = findViewById(R.id.list_users);
        buttonSelect = findViewById(R.id.button_selectContact);
        getAllUsers();
        listUsers.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        buttonSelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int count = listUsers.getCount();
                if (listUsers.getCheckedItemPositions().size() == 1) {
                    createPersonalChat(listUsers.getCheckedItemPositions());
                } else if (listUsers.getCheckedItemPositions().size() > 1) {
                    createGroupChat(listUsers.getCheckedItemPositions());
                } else {
                    Toast.makeText(UsersListActivity.this,
                            "Select at least one contact", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void createPersonalChat(SparseBooleanArray checkedItemPositions) {
        final ProgressDialog progressDialog = new ProgressDialog(UsersListActivity.this);
        progressDialog.setMessage("Please wait...");
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();

        int count = listUsers.getCount();
        for (int i = 0; i < count; i++) {
            if ((checkedItemPositions.get(i))) {
                final QBUser qbUser = (QBUser) listUsers.getItemAtPosition(i);
                QBChatDialog chatDialog = DialogUtils.buildPrivateDialog(qbUser.getId());
                QBRestChatService.createChatDialog(chatDialog)
                        .performAsync(new QBEntityCallback<QBChatDialog>() {
                            @Override
                            public void onSuccess(QBChatDialog qbChatDialog, Bundle bundle) {
                                Toast.makeText(UsersListActivity.this, "Chat created",
                                        Toast.LENGTH_SHORT).show();
                                //     progressDialog.dismiss();

                                //      Send system message to notify user of creation of new chat
                                QBSystemMessagesManager systemMessagesManager = QBChatService.getInstance().getSystemMessagesManager();
                                QBChatMessage qbChatMessage = new QBChatMessage();
                                qbChatMessage.setRecipientId(qbUser.getId());
                                qbChatMessage.setBody(qbChatDialog.getDialogId());
                                try {
                                    systemMessagesManager.sendSystemMessage(qbChatMessage);
                                } catch (SmackException.NotConnectedException e) {
                                    e.printStackTrace();
                                    Log.e(TAG, "onSuccess: SmackException.NotConnectedException " + e.getMessage());
                                }


                                finish();
                            }

                            @Override
                            public void onError(QBResponseException e) {
                                //     progressDialog.dismiss();
                                Log.e(TAG, "onError: " + e.getMessage());
                            }
                        });
            }
        }
        progressDialog.dismiss();
    }


    private void createGroupChat(SparseBooleanArray checkedItemPositions) {

        final ProgressDialog progressDialog = new ProgressDialog(UsersListActivity.this);
        progressDialog.setMessage("Please wait...");
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();
        int count = listUsers.getCount();
        ArrayList<Integer> idList = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            if ((checkedItemPositions.get(i))) {
                QBUser qbUser = (QBUser) listUsers.getItemAtPosition(i);
                idList.add(qbUser.getId());
            }
        }

        // Create chat
        QBChatDialog qbChatDialog = new QBChatDialog();
        qbChatDialog.setName(Common.createChatDialogName(idList));
        qbChatDialog.setType(QBDialogType.GROUP);
        qbChatDialog.setOccupantsIds(idList);

        QBRestChatService.createChatDialog(qbChatDialog)
                .performAsync(new QBEntityCallback<QBChatDialog>() {
                    @Override
                    public void onSuccess(QBChatDialog qbChatDialog, Bundle bundle) {
                        Toast.makeText(UsersListActivity.this, "Group created",
                                Toast.LENGTH_SHORT).show();
                        progressDialog.dismiss();
                        //      Send system message to notify user of creation of new chat
                        QBSystemMessagesManager systemMessagesManager = QBChatService.getInstance().getSystemMessagesManager();
                        QBChatMessage qbChatMessage = new QBChatMessage();
                        qbChatMessage.setBody(qbChatDialog.getDialogId());
                        for (int i = 0; i < qbChatDialog.getOccupants().size(); i++) {
                            qbChatMessage.setRecipientId(qbChatDialog.getOccupants().get(i));

                            try {
                                systemMessagesManager.sendSystemMessage(qbChatMessage);
                            } catch (SmackException.NotConnectedException e) {
                                e.printStackTrace();
                                Log.e(TAG, "onSuccess: SmackException.NotConnectedException " + e.getMessage());
                            }
                        }


                        finish();
                    }

                    @Override
                    public void onError(QBResponseException e) {
                        progressDialog.dismiss();
                        Log.e(TAG, "onError: " + e.getMessage());
                    }
                });

    }

    private void getAllUsers() {
        QBUsers.getUsers(null).performAsync(new QBEntityCallback<ArrayList<QBUser>>() {
            @Override
            public void onSuccess(ArrayList<QBUser> qbUsers, Bundle bundle) {

                QBUsersHolder.getInstance().putUsers(qbUsers);

                ArrayList<QBUser> qbUsersList = new ArrayList<>();
                for (QBUser user : qbUsers) {
                    if (!user.getLogin().equals(QBChatService.getInstance().getUser().getLogin())) {
                        qbUsersList.add(user);
                    }
                }
                UsersListAdaptor adaptor = new UsersListAdaptor(getBaseContext(), qbUsersList);
                listUsers.setAdapter(adaptor);
                adaptor.notifyDataSetChanged();
            }

            @Override
            public void onError(QBResponseException e) {
                e.printStackTrace();
                Log.e(TAG, "onError: " + e.getMessage());
            }
        });
    }
}
