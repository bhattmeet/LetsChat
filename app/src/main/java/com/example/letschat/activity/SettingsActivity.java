package com.example.letschat.activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.letschat.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class SettingsActivity extends AppCompatActivity implements View.OnClickListener{

    private EditText edtUserName,edtStatus;
    private Button btnUpdateProfile;
    private ImageView imgBack;
    private CircleImageView imgUser;
    private View mToolbar;
    private TextView txtName;
    private Context mContext;
    private String userName,status,currentUserID,errorMessage,retriveUserName,retriveStatus,retriveUserImage;
    private FirebaseAuth mAuth;
    private DatabaseReference RootRef;
    private static final int gallery = 1;
    private StorageReference storageReference;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        mContext = this;

        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        RootRef = FirebaseDatabase.getInstance().getReference();
        storageReference = FirebaseStorage.getInstance().getReference().child("Profile Image");

        mToolbar = findViewById(R.id.toolbar);
        txtName = findViewById(R.id.txtName);
        imgBack = findViewById(R.id.imgBack);
        edtUserName = findViewById(R.id.edtUserName);
        edtStatus = findViewById(R.id.edtStatus);
        imgUser = findViewById(R.id.imgUser);
        btnUpdateProfile = findViewById(R.id.btnUpdateProfile);
        progressDialog = new ProgressDialog(this);

        txtName.setText(getString(R.string.settings));
        imgBack.setOnClickListener(this);
        btnUpdateProfile.setOnClickListener(this);
        imgUser.setOnClickListener(this);

        retriveUserInfo();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.imgBack:
                onBackPressed();
                break;
            case R.id.btnUpdateProfile:
                updateSettings();
                break;
            case R.id.imgUser:
                Intent galleryIntent = new Intent();
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent,gallery);
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == gallery && resultCode == RESULT_OK && data != null){
            Uri imageUri = data.getData();

            CropImage.activity()
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .start(this);
        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK){
                progressDialog.setTitle("Set Profile Image");
                progressDialog.setMessage("Please wait...");
                progressDialog.setCanceledOnTouchOutside(false);
                progressDialog.show();
                Uri resultUri = result.getUri();

                StorageReference filePath = storageReference.child(currentUserID + ".jpg");
                Log.e("test","resultUri: "+resultUri);
                Log.e("test","FilePath: "+filePath);
                filePath.putFile(resultUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                final String downloadUri = uri.toString();
                                RootRef.child("Users").child(currentUserID).child("image").setValue(downloadUri).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            Toast.makeText(SettingsActivity.this, "Profile image uploaded successfully.", Toast.LENGTH_SHORT).show();
                                            progressDialog.dismiss();
                                        } else {
                                            String message = task.getException().getMessage();
                                            Toast.makeText(SettingsActivity.this, "Error: " + message, Toast.LENGTH_SHORT).show();
                                            progressDialog.dismiss();
                                        }
                                    }
                                });
                            }
                        });
                    }
                });
            }
        }
    }

    private void updateSettings(){
        userName = edtUserName.getText().toString().trim();
        status = edtStatus.getText().toString().trim();

        if (TextUtils.isEmpty(userName)){
            Toast.makeText(mContext, "Please enter username", Toast.LENGTH_SHORT).show();
        }else if (TextUtils.isEmpty(status)){
            Toast.makeText(mContext, "Please enter your status", Toast.LENGTH_SHORT).show();
        }else {
            HashMap<String,Object> profileMap = new HashMap<>();
            profileMap.put("uid",currentUserID);
            profileMap.put("username",userName);
            profileMap.put("status",status);

            RootRef.child("Users").child(currentUserID).updateChildren(profileMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()){
                        Intent intent = new Intent(SettingsActivity.this,HomeActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                        Toast.makeText(mContext, "Profile Updated Successfully", Toast.LENGTH_SHORT).show();
                    }else {
                        errorMessage = task.getException().toString();
                        Toast.makeText(mContext, "Error:"+errorMessage, Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    private void retriveUserInfo(){
        RootRef.child("Users").child(currentUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if ((snapshot.exists()) && (snapshot.hasChild("username")) && (snapshot.hasChild("image"))){
                    retriveUserName = snapshot.child("username").getValue().toString();
                    retriveStatus = snapshot.child("status").getValue().toString();
                    retriveUserImage = snapshot.child("image").getValue().toString();

                    edtUserName.setText(retriveUserName);
                    edtStatus.setText(retriveStatus);
                    Picasso.get().load(retriveUserImage).placeholder(getDrawable(R.drawable.ic_baseline_person_24)).into(imgUser);
                    Log.e("test","Image: "+retriveUserImage);
                    Log.e("test","Name: "+retriveUserName);
                    Log.e("test","Status: "+retriveStatus);
                }else if ((snapshot.exists()) && (snapshot.hasChild("username"))){
                    retriveUserName = snapshot.child("username").getValue().toString();
                    retriveStatus = snapshot.child("status").getValue().toString();

                    edtUserName.setText(retriveUserName);
                    edtStatus.setText(retriveStatus);
                }else {
                    Toast.makeText(mContext, "Please update your information", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}