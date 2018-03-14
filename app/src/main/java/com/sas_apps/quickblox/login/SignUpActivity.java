package com.sas_apps.quickblox.login;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.quickblox.auth.QBAuth;
import com.quickblox.auth.session.QBSession;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.users.QBUsers;
import com.quickblox.users.model.QBUser;
import com.sas_apps.quickblox.R;

public class SignUpActivity extends AppCompatActivity {

    private static final String TAG = "SignUpActivity";
    Button btnSignIn, btnSignUp;
    EditText editPassword, editUsername, editFullName,editEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        editUsername = findViewById(R.id.edit_NewUsername);
        editPassword = findViewById(R.id.edit_NewPassword);
        editFullName = findViewById(R.id.edit_FullName);
        editEmail=findViewById(R.id.edit_Email);
        QuickBloxRegistration();
    }


    public void attemptSignUp(View view) {
        String userName = editUsername.getText().toString();
        String password = editPassword.getText().toString();
        String fullName=editFullName.getText().toString();
        String email=editEmail.getText().toString();
        QBUser qbUser = new QBUser(userName, password);
        qbUser.setFullName(fullName);
        qbUser.setEmail(email);
        QBUsers.signUp(qbUser).performAsync(new QBEntityCallback<QBUser>() {
            @Override
            public void onSuccess(QBUser qbUser, Bundle bundle) {
                Toast.makeText(SignUpActivity.this, "Sign in sucsessful. Please sign in", Toast.LENGTH_SHORT).show();
                finish();
            }

            @Override
            public void onError(QBResponseException e) {
                Toast.makeText(SignUpActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }


    private void QuickBloxRegistration() {
        QBAuth.createSession().performAsync(new QBEntityCallback<QBSession>() {
            @Override
            public void onSuccess(QBSession qbSession, Bundle bundle) {

            }

            @Override
            public void onError(QBResponseException e) {
                Log.e(TAG, "onError: " + e.getMessage());
            }
        });
    }

    public void openSignInActivity(View view) {
        finish();
    }
}
