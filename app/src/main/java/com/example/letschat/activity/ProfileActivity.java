package com.example.letschat.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.letschat.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {

    private String userId,userName,status,userImage,name,currentState = "new",senderUserId,requestType;
    private TextView txtUserName,txtUserStatus,txtName;
    private Button btnSendMessage,btnCancelRequest;
    private CircleImageView imgUser;
    private View mToolbar;
    private ImageView imgBack;

    private DatabaseReference databaseReference,chatRequest,contactRequest,notificationReference;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        mAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference().child("Users");
        chatRequest = FirebaseDatabase.getInstance().getReference().child("Chat Requests");
        contactRequest = FirebaseDatabase.getInstance().getReference().child("Contacts");
        notificationReference = FirebaseDatabase.getInstance().getReference().child("Notifications");

        userId = getIntent().getExtras().get("userId").toString();
        name = getIntent().getExtras().get("userName").toString();
        senderUserId = mAuth.getCurrentUser().getUid();
        Log.e("test","UID:"+userId);

        setIds();
        retrieveUserInfo();

        txtName.setText(name +" Profile");
        imgBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    private void setIds(){
        imgUser = findViewById(R.id.imgUser);
        txtUserName = findViewById(R.id.txtUserName);
        txtUserStatus = findViewById(R.id.txtUserStatus);
        btnSendMessage = findViewById(R.id.btnSendMessage);
        btnCancelRequest = findViewById(R.id.btnCancelRequest);
        mToolbar = findViewById(R.id.toolbar);
        txtName = findViewById(R.id.txtName);
        imgBack = findViewById(R.id.imgBack);
    }

    private void retrieveUserInfo(){
        databaseReference.child(userId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if ((snapshot.exists()) && (snapshot.hasChild("image"))){
                    userName = snapshot.child("username").getValue().toString();
                    userImage = snapshot.child("image").getValue().toString();
                    status = snapshot.child("status").getValue().toString();

                    Picasso.get().load(userImage).placeholder(getDrawable(R.drawable.ic_baseline_person_24)).into(imgUser);
                    txtUserName.setText(userName);
                    txtUserStatus.setText(status);

                    chatRequests();
                }else {
                    userName = snapshot.child("username").getValue().toString();
                    status = snapshot.child("status").getValue().toString();

                    txtUserName.setText(userName);
                    txtUserStatus.setText(status);

                    chatRequests();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void chatRequests(){

        chatRequest.child(senderUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.hasChild(userId)){
                    requestType = snapshot.child(userId).child("requestType").getValue().toString();
                    if (requestType.equals("sent")){
                        currentState = "requestSent";
                        btnSendMessage.setText("Cancel Chat Request");
                    }else if (requestType.equals("received")){
                        currentState = "requestReceived";
                        btnSendMessage.setText("Accept Chat Request");

                        btnCancelRequest.setVisibility(View.VISIBLE);
                        btnCancelRequest.setEnabled(true);
                        btnCancelRequest.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                cancelChatRequest();
                            }
                        });
                    }
                }else {
                    contactRequest.child(senderUserId).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.hasChild(userId)){
                                currentState = "friends";
                                btnSendMessage.setText("Remove this contact");
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        if (!senderUserId.equals(userId)){
            btnSendMessage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    btnSendMessage.setEnabled(false);

                    if (currentState.equals("new")){
                        sendChatRequest();
                    }
                    if (currentState.equals("requestSent")){
                        cancelChatRequest();
                    }
                    if (currentState.equals("requestReceived")){
                        acceptChatRequest();
                    }
                    if (currentState.equals("friends")){
                        removeSpecificContactRequest();
                    }
                }
            });
        }else {
            btnSendMessage.setVisibility(View.GONE);
        }
    }

    private void sendChatRequest(){
        chatRequest.child(senderUserId).child(userId).child("requestType").setValue("sent").addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()){
                    chatRequest.child(userId).child(senderUserId).child("requestType").setValue("received").addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()){
                                HashMap<String,String> chatNotificationMap = new HashMap<>();
                                chatNotificationMap.put("from",senderUserId);
                                chatNotificationMap.put("type","request");

                                notificationReference.child(userId).push().setValue(chatNotificationMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()){
                                            btnSendMessage.setEnabled(true);
                                            currentState = "requestSent";
                                            btnSendMessage.setText("Cancel Chat Request");
                                        }
                                    }
                                });
                            }
                        }
                    });
                }
            }
        });
    }

    private void cancelChatRequest(){
        chatRequest.child(senderUserId).child(userId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()){
                    chatRequest.child(userId).child(senderUserId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()){
                                btnSendMessage.setEnabled(true);
                                currentState = "new";
                                btnSendMessage.setText("Send Message");

                                btnCancelRequest.setVisibility(View.GONE);
                                btnCancelRequest.setEnabled(false);

                            }
                        }
                    });
                }
            }
        });
    }

    private void acceptChatRequest(){
        contactRequest.child(senderUserId).child(userId).child("Contacts").setValue("Saved").addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()){
                    contactRequest.child(userId).child(senderUserId).child("Contacts").setValue("Saved").addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()){
                                chatRequest.child(senderUserId).child(userId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()){
                                            chatRequest.child(userId).child(senderUserId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if (task.isSuccessful()){
                                                        btnSendMessage.setEnabled(true);
                                                        currentState = "friends";
                                                        btnSendMessage.setText("Remove this contact");

                                                        btnCancelRequest.setVisibility(View.GONE);
                                                        btnCancelRequest.setEnabled(false);
                                                    }
                                                }
                                            });
                                        }
                                    }
                                });
                            }
                        }
                    });
                }
            }
        });
    }

    private void removeSpecificContactRequest(){
        contactRequest.child(senderUserId).child(userId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()){
                    contactRequest.child(userId).child(senderUserId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()){
                                btnSendMessage.setEnabled(true);
                                currentState = "new";
                                btnSendMessage.setText("Send Message");

                                btnCancelRequest.setVisibility(View.GONE);
                                btnCancelRequest.setEnabled(false);

                            }
                        }
                    });
                }
            }
        });
    }
}