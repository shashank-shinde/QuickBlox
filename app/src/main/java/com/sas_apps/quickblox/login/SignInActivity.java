package com.sas_apps.quickblox.login;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.quickblox.auth.session.QBSettings;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.users.QBUsers;
import com.quickblox.users.model.QBUser;
import com.sas_apps.quickblox.chat.chat_list.ChatListActivity;
import com.sas_apps.quickblox.R;

public class SignInActivity extends AppCompatActivity {

    private static final String TAG = "SignInActivity";
    static final String APP_ID = "68714";
    static final String AUTH_KEY = "6PJgPAtNJ5pFg59";
    static final String AUTH_SECRET = "eXWeP-UgYJaqywR";
    static final String ACCOUNT_KEY = "D7Gr3HqLsqwr4pGUmAoP";

    EditText editPassword, editUsername;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);
        editUsername = findViewById(R.id.edit_username);
        editPassword = findViewById(R.id.edit_password);
        QuickBloxInit();
    }



    public void attemptSignIn(View view) {
        final String userName = editUsername.getText().toString();
        final String password = editPassword.getText().toString();
        QBUser qbUser = new QBUser(userName, password);
        QBUsers.signIn(qbUser).performAsync(new QBEntityCallback<QBUser>() {
            @Override
            public void onSuccess(QBUser qbUser, Bundle bundle) {
                Toast.makeText(SignInActivity.this, "Sign in successful", Toast.LENGTH_SHORT).show();
                Intent intent=new Intent(SignInActivity.this, ChatListActivity.class);
                intent.putExtra("userName",userName);
                intent.putExtra("password",password);
                startActivity(intent);
                finish();

            }

            @Override
            public void onError(QBResponseException e) {
                Toast.makeText(SignInActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void QuickBloxInit() {
        QBSettings.getInstance().init(getApplicationContext(), APP_ID, AUTH_KEY, AUTH_SECRET);
        QBSettings.getInstance().setAccountKey(ACCOUNT_KEY);
    }
    public void openSignUpActivity(View view) {
        startActivity(new Intent(SignInActivity.this, SignUpActivity.class));
    }
}
