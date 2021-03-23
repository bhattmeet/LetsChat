package com.example.letschat.activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.icu.text.Edits;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.letschat.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Objects;

public class GroupChatActivity extends AppCompatActivity implements View.OnClickListener
{

    private View mToolbar;
    private Button btnSend;
    private EditText edtMessage;
    private ScrollView scrollView;
    private TextView txtGroupChat,txtName;
    private ImageView imgBack;
    private String groupName,currentUserID,currentUserName,message,currentDate,currentTime,messageKey;
    private FirebaseAuth mAuth;
    private DatabaseReference userRef,groupRef,grpMessageKeyRef;
    private String chatDate,chatMessage,chatTime,chatName;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_chat);

        groupName = getIntent().getStringExtra("groupName").toString();
        Log.e("test","grpName: "+groupName);

        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        userRef = FirebaseDatabase.getInstance().getReference().child("Users");
        groupRef = FirebaseDatabase.getInstance().getReference().child("Groups").child(groupName);

        setIds();

        getUserInfo();
    }

    private void setIds(){
        mToolbar = findViewById(R.id.toolbar);
        txtName = findViewById(R.id.txtName);
        imgBack = findViewById(R.id.imgBack);
        btnSend = findViewById(R.id.btnSend);
        edtMessage = findViewById(R.id.edtMessage);
        scrollView = findViewById(R.id.scrollView);
        txtGroupChat = findViewById(R.id.txtGroupChat);

        txtName.setText(groupName);
        imgBack.setOnClickListener(this);
        btnSend.setOnClickListener(this);
    }

    @Override
    protected void onStart() {
        super.onStart();

        groupRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                if (snapshot.exists()){
                    displayMessages(snapshot);
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                if (snapshot.exists()){
                    displayMessages(snapshot);
                }
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.imgBack:
                onBackPressed();
                break;
            case R.id.btnSend:
                saveMessageData();

                edtMessage.setText("");

                scrollView.fullScroll(ScrollView.FOCUS_DOWN);
                break;
        }
    }

    private void getUserInfo(){
        userRef.child(currentUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    currentUserName = snapshot.child("username").getValue().toString();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void saveMessageData(){
        message = edtMessage.getText().toString();
        messageKey = groupRef.push().getKey();

        if (TextUtils.isEmpty(message)) {
            Toast.makeText(this, "Please type your message", Toast.LENGTH_SHORT).show();
        }else {
            Calendar calendarDate = Calendar.getInstance();
            SimpleDateFormat currentDateFormat = new SimpleDateFormat("MMM dd,yyyy");
            currentDate = currentDateFormat.format(calendarDate.getTime());

            Calendar calendarTime = Calendar.getInstance();
            SimpleDateFormat currentTimeFormat = new SimpleDateFormat("hh:mm a");
            currentTime = currentTimeFormat.format(calendarTime.getTime());

            HashMap<String,Object> grpMessageKey = new HashMap<>();
            groupRef.updateChildren(grpMessageKey);

            grpMessageKeyRef = groupRef.child(messageKey);
            HashMap<String,Object> messageInfoMap = new HashMap<>();
            messageInfoMap.put("username",currentUserName);
            messageInfoMap.put("message",message);
            messageInfoMap.put("date",currentDate);
            messageInfoMap.put("time",currentTime);
            grpMessageKeyRef.updateChildren(messageInfoMap);
        }
    }

    private void displayMessages(DataSnapshot dataSnapshot){
        Iterator iterator = dataSnapshot.getChildren().iterator();

        while (iterator.hasNext()){
            chatDate = (String) ((DataSnapshot)iterator.next()).getValue();
            chatMessage = (String) ((DataSnapshot)iterator.next()).getValue();
            chatTime = (String) ((DataSnapshot)iterator.next()).getValue();
            chatName = (String) ((DataSnapshot)iterator.next()).getValue();

            Log.e("test","Date: "+chatDate);
            Log.e("test","Time: "+chatTime);
            Log.e("test","Name: "+chatName);
            Log.e("test","Message: "+chatMessage);

            txtGroupChat.setVisibility(View.VISIBLE);
            txtGroupChat.append(chatName + " \n" + chatMessage + "\n" + chatTime + "   " + chatDate + "\n\n\n");
            scrollView.fullScroll(ScrollView.FOCUS_DOWN);
        }
    }
}