package com.sas_apps.quickblox.profile;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.quickblox.chat.QBChatService;
import com.quickblox.content.QBContent;
import com.quickblox.content.model.QBFile;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.users.QBUsers;
import com.quickblox.users.model.QBUser;
import com.sas_apps.quickblox.R;
import com.sas_apps.quickblox.chat.chat_list.ChatListActivity;
import com.sas_apps.quickblox.login.SignInActivity;
import com.sas_apps.quickblox.qb.common.holder.QBUsersHolder;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {

    private static final String TAG = "ProfileActivity";
    EditText editOldPassword, editNewPassword, editNewName, editNewEmail, editNewPhoneNumber;
    Button buttonUpdateProfile;
    ProgressDialog progressDialog;
    CircleImageView imageProfilePic;
    private static final int REQUEST_CODE = 4;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        progressDialog = new ProgressDialog(ProfileActivity.this);
        editOldPassword = findViewById(R.id.edit_oldPassword);
        editNewPassword = findViewById(R.id.edit_newPassword);
        editNewName = findViewById(R.id.edit_newFullName);
        editNewEmail = findViewById(R.id.edit_newEmail);
        editNewPhoneNumber = findViewById(R.id.edit_newPhoneNumber);
        buttonUpdateProfile = findViewById(R.id.button_updateProfile);
        imageProfilePic = findViewById(R.id.image_user);
        loadCurrentUserProfile();

        buttonUpdateProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateProfile();
            }
        });

        imageProfilePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select Image"), REQUEST_CODE);
            }
        });
    }

    private void loadCurrentUserProfile() {

        QBUsers.getUser(QBChatService.getInstance().getUser().getId())
                .performAsync(new QBEntityCallback<QBUser>() {
                    @Override
                    public void onSuccess(QBUser qbUser, Bundle bundle) {
                        QBUsersHolder.getInstance().putUser(qbUser);
                        if (qbUser.getFileId() != null) {
                            int picId = qbUser.getFileId();
                            QBContent.getFile(picId)
                                    .performAsync(new QBEntityCallback<QBFile>() {
                                        @Override
                                        public void onSuccess(QBFile qbFile, Bundle bundle) {
                                            String imageUri = qbFile.getPublicUrl();
                                            Picasso.with(ProfileActivity.this)
                                                    .load(imageUri)
                                                    .into(imageProfilePic);

                                        }

                                        @Override
                                        public void onError(QBResponseException e) {
                                            Log.e(TAG, "onError: " + e.getMessage());
                                        }
                                    });
                        }
                    }

                    @Override
                    public void onError(QBResponseException e) {
                        Log.e(TAG, "onError: " + e.getMessage());
                    }
                });

        //Todo this method is not working
        QBUser qbUser = QBChatService.getInstance().getUser();
        editNewEmail.setText(qbUser.getEmail());
        editNewName.setText(qbUser.getFullName());
        qbUser.setPhone(qbUser.getPhone());
        Log.d(TAG, "loadCurrentUserProfile: " + qbUser.getEmail());
    }

    private void updateProfile() {

        //Todo check edit text is empty
        String oldPassword = editOldPassword.getText().toString();
        String newPassword = editNewPassword.getText().toString();
        String newName = editNewName.getText().toString();
        String newEmail = editNewEmail.getText().toString();
        String phoneNumber = editNewPhoneNumber.getText().toString();
        QBUser qbUser = new QBUser();
        qbUser.setId(QBChatService.getInstance().getUser().getId());
        qbUser.setOldPassword(oldPassword);
        qbUser.setPassword(newPassword);
        qbUser.setFullName(newName);
        qbUser.setEmail(newEmail);
        qbUser.setPhone(phoneNumber);
        progressDialog.setMessage("Updating profile. Please wait...");
        progressDialog.show();
        QBUsers.updateUser(qbUser).performAsync(new QBEntityCallback<QBUser>() {
            @Override
            public void onSuccess(QBUser qbUser, Bundle bundle) {
                Toast.makeText(ProfileActivity.this, qbUser.getLogin() + " updated.", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(QBResponseException e) {
                Log.e(TAG, "onError: QBResponseException " + e.getMessage());
                Toast.makeText(ProfileActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
        progressDialog.dismiss();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "onActivityResult: started");
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_CODE) {
                Uri imageUri = data.getData();
                final ProgressDialog progressDialog = new ProgressDialog(this);
                Log.d(TAG, "onActivityResult: Updating...");
                progressDialog.setMessage("Updating...");
                progressDialog.setCanceledOnTouchOutside(false);
                progressDialog.show();

                try {
                    InputStream inputStream = getContentResolver().openInputStream(imageUri);
                    final Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                    ByteArrayOutputStream bao = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.PNG, 50, bao);
                    File file = new File(Environment.getExternalStorageDirectory() + "/image.png");
                    FileOutputStream fos = new FileOutputStream(file);
                    fos.write(bao.toByteArray());
                    fos.flush();
                    fos.close();

                    if ((int) file.length() / 1024 >= (1024 * 100)) {
                        progressDialog.dismiss();
                        Toast.makeText(this, "Too large file. Select other image", Toast.LENGTH_SHORT).show();
                    } else {
                        QBContent.uploadFileTask(file, true, null)
                                .performAsync(new QBEntityCallback<QBFile>() {
                                    @Override
                                    public void onSuccess(QBFile qbFile, Bundle bundle) {
                                        QBUser user = new QBUser();
                                        user.setId(QBChatService.getInstance().getUser().getId());
                                        user.setFileId(Integer.parseInt(qbFile.getId().toString()));
                                        QBUsers.updateUser(user)
                                                .performAsync(new QBEntityCallback<QBUser>() {
                                                    @Override
                                                    public void onSuccess(QBUser qbUser, Bundle bundle) {
                                                        progressDialog.dismiss();
                                                        imageProfilePic.setImageBitmap(bitmap);
                                                    }

                                                    @Override
                                                    public void onError(QBResponseException e) {
                                                        Log.e(TAG, "onError: " + e.getMessage());
                                                    }
                                                });
                                    }

                                    @Override
                                    public void onError(QBResponseException e) {
                                        Log.e(TAG, "onError: " + e.getMessage());
                                    }
                                });

                    }
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                progressDialog.dismiss();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.profile_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_signOut:
                signOut();
                break;
        }

        return true;
    }

    private void signOut() {

        //Todo clear previous activities
        QBUsers.signOut().performAsync(new QBEntityCallback<Void>() {
            @Override
            public void onSuccess(Void aVoid, Bundle bundle) {
                QBChatService.getInstance().logout(new QBEntityCallback<Void>() {
                    @Override
                    public void onSuccess(Void aVoid, Bundle bundle) {
                        Toast.makeText(ProfileActivity.this, "Sign out", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(ProfileActivity.this, SignInActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                        finish();
                    }

                    @Override
                    public void onError(QBResponseException e) {
                        Log.e(TAG, "onError: QBResponseException " + e.getMessage());
                    }
                });
            }

            @Override
            public void onError(QBResponseException e) {
                Log.e(TAG, "onError: QBResponseException " + e.getMessage());
            }
        });
    }
}
