package com.example.letschat.activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.DialogInterface;
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

import com.example.letschat.R;
import com.example.letschat.adapter.MessageAdapter;
import com.example.letschat.model.Messages;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity implements View.OnClickListener{

    private String msgReceiverId, msgReceiverName,msgReceiverImg,chatMessage,msgSenderId,currentTime,check = "",myUrl = "";
    private View chatToolbar;
    private TextView txtUserName,txtLastSeen;
    private CircleImageView imgUser;
    private ImageView imgBack;
    private EditText edtSendMessage;
    private Button btnSend,btnSendFiles;
    private RecyclerView rvChatMessages;
    private final List<Messages> messagesList = new ArrayList<>();
    private LinearLayoutManager linearLayoutManager;
    private MessageAdapter adapter;
    private int REQUEST_CODE_OPEN = 100;
    private Uri fileUri;
    private ProgressDialog progressDialog;

    private FirebaseAuth mAuth;
    private DatabaseReference rootReference;
    private StorageTask uploadTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        mAuth = FirebaseAuth.getInstance();
        msgSenderId = mAuth.getCurrentUser().getUid();
        rootReference = FirebaseDatabase.getInstance().getReference();

        chatToolbar = findViewById(R.id.chatToolbar);
        txtUserName = findViewById(R.id.txtUserName);
        txtLastSeen = findViewById(R.id.txtLastSeen);
        imgUser = findViewById(R.id.imgUser);
        imgBack = findViewById(R.id.imgBack);
        edtSendMessage = findViewById(R.id.edtMessage);
        btnSend = findViewById(R.id.btnSend);
        btnSendFiles = findViewById(R.id.btnSendFiles);
        rvChatMessages = findViewById(R.id.rvChatMessages);
        progressDialog = new ProgressDialog(this);

        msgReceiverId = getIntent().getStringExtra("userId").toString();
        msgReceiverName = getIntent().getStringExtra("userName").toString();
        msgReceiverImg = getIntent().getStringExtra("userImg").toString();

        txtUserName.setText(msgReceiverName);
        Picasso.get().load(msgReceiverImg).placeholder(R.drawable.ic_baseline_person_24).into(imgUser);
        imgBack.setOnClickListener(this);
        btnSend.setOnClickListener(this);
        btnSendFiles.setOnClickListener(this);

        adapter = new MessageAdapter(messagesList);
        linearLayoutManager = new LinearLayoutManager(this);
        rvChatMessages.setLayoutManager(linearLayoutManager);
        rvChatMessages.setAdapter(adapter);

        lastSeenStatus();
    }

    @Override
    protected void onStart() {
        super.onStart();

        rootReference.child("Messages").child(msgSenderId).child(msgReceiverId).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                Messages messages = snapshot.getValue(Messages.class);
                messagesList.add(messages);
                adapter.notifyDataSetChanged();

                rvChatMessages.smoothScrollToPosition(rvChatMessages.getAdapter().getItemCount());
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

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
                sendMessage();
                break;
            case R.id.btnSendFiles:
                sendFiles();
                break;
        }
    }

    private void sendMessage(){
        chatMessage = edtSendMessage.getText().toString();

        if (TextUtils.isEmpty(chatMessage)){
            Toast.makeText(this, "Please type your message", Toast.LENGTH_SHORT).show();
        }else {
            String msgSenderRef = "Messages/" +msgSenderId + "/" + msgReceiverId;
            String msgReceiverRef = "Messages/" +msgReceiverId + "/" + msgSenderId;

            DatabaseReference userMsgReference = rootReference.child("Messages").child(msgSenderId).child(msgReceiverId).push();

            String msgPushId = userMsgReference.getKey();

            Calendar calendarTime = Calendar.getInstance();
            SimpleDateFormat currentTimeFormat = new SimpleDateFormat("hh:mm a");
            currentTime = currentTimeFormat.format(calendarTime.getTime());

            Map<String, String> msgTextBody = new HashMap<String, String>();
            msgTextBody.put("message",chatMessage);
            msgTextBody.put("type","text");
            msgTextBody.put("from",msgSenderId);
            msgTextBody.put("time",currentTime);
            msgTextBody.put("to",msgReceiverId);
            msgTextBody.put("messageId",msgPushId);

            Map<String, Object> msgBodyDetails = new HashMap<String, Object>();
            msgBodyDetails.put(msgSenderRef + "/" + msgPushId, msgTextBody);
            msgBodyDetails.put(msgReceiverRef + "/" + msgPushId, msgTextBody);

            rootReference.updateChildren(msgBodyDetails).addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {
                    if (task.isSuccessful()){
                        Toast.makeText(ChatActivity.this, "Message Sent Successfully", Toast.LENGTH_SHORT).show();
                    }else {
                        Toast.makeText(ChatActivity.this, "Error", Toast.LENGTH_SHORT).show();
                    }
                    edtSendMessage.setText("");
                }
            });
        }
    }

    private void sendFiles(){
        CharSequence options[] = new CharSequence[]{
                "Images",
                "PDF Files",
                "MS Word Files"
        };
        AlertDialog.Builder builder = new AlertDialog.Builder(ChatActivity.this);
        builder.setTitle("Select the Title");
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int i) {
                if (i==0){
                    check = "image";
                    Intent intent = new Intent();
                    intent.setAction(Intent.ACTION_GET_CONTENT);
                    intent.setType("image/*");
                    startActivityForResult(Intent.createChooser(intent,"Select Image"),REQUEST_CODE_OPEN);
//                    Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
//                    intent.addCategory(Intent.CATEGORY_OPENABLE);
//                    intent.setType("*/*");
//                    String[] mimetypes = {"image/*", "pdf/*", "docx/*"};
//                    intent.putExtra(Intent.EXTRA_MIME_TYPES, mimetypes);
//                    startActivityForResult(intent, REQUEST_CODE_OPEN);
                }
                if (i==1){
                    check = "pdf";
                    Intent intent = new Intent();
                    intent.setAction(Intent.ACTION_GET_CONTENT);
                    intent.setType("application/pdf");
                    startActivityForResult(Intent.createChooser(intent,"Select PDF files"),REQUEST_CODE_OPEN);
                }
                if (i==2){
                    check = "docx";
                    Intent intent = new Intent();
                    intent.setAction(Intent.ACTION_GET_CONTENT);
                    intent.setType("application/msword");
                    startActivityForResult(Intent.createChooser(intent,"Select Ms Word files"),REQUEST_CODE_OPEN);
                }
            }
        });
        builder.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_OPEN && resultCode == RESULT_OK && data != null && data.getData() != null){

            fileUri = data.getData();

            if (!check.equals("image")){

                progressDialog.setTitle("Sending Files");
                progressDialog.setMessage("Please wait...");
                progressDialog.setCanceledOnTouchOutside(false);
                progressDialog.show();

                StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("Document Files");

                final String msgSenderRef = "Messages/" +msgSenderId + "/" + msgReceiverId;
                final String msgReceiverRef = "Messages/" +msgReceiverId + "/" + msgSenderId;

                DatabaseReference userMsgReference = rootReference.child("Messages").child(msgSenderId).child(msgReceiverId).push();

                final String msgPushId = userMsgReference.getKey();

                StorageReference filePath = storageReference.child(msgPushId + "." + check);

                uploadTask = filePath.putFile(fileUri);
                uploadTask.continueWithTask(new Continuation() {
                    @Override
                    public Object then(@NonNull Task task) throws Exception {
                        if (!task.isSuccessful()){
                            throw task.getException();
                        }
                        return filePath.getDownloadUrl();
                    }
                }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        if (task.isSuccessful()) {
                            Uri downloadUri = task.getResult();
                            Log.e("test", "Image: " + downloadUri);
                            if (downloadUri != null) {
                                myUrl = downloadUri.toString();
                            }

                            Map<String,String> msgTextBody = new HashMap<>();
                            msgTextBody.put("message", myUrl);
                            msgTextBody.put("name", fileUri.getLastPathSegment());
                            msgTextBody.put("type", check);
                            msgTextBody.put("from", msgSenderId);
                            msgTextBody.put("time", currentTime);
                            msgTextBody.put("to", msgReceiverId);
                            msgTextBody.put("messageId", msgPushId);

                            Map<String,Object> msgBodyDetails = new HashMap<String, Object>();
                            msgBodyDetails.put(msgSenderRef + "/" + msgPushId, msgTextBody);
                            msgBodyDetails.put(msgReceiverRef + "/" + msgPushId, msgTextBody);

                            rootReference.updateChildren(msgBodyDetails);
                            progressDialog.dismiss();
                        }

                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                        Toast.makeText(ChatActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

            }else {
                StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("Image Files");

                final String msgSenderRef = "Messages/" +msgSenderId + "/" + msgReceiverId;
                final String msgReceiverRef = "Messages/" +msgReceiverId + "/" + msgSenderId;

                DatabaseReference userMsgReference = rootReference.child("Messages").child(msgSenderId).child(msgReceiverId).push();

                final String msgPushId = userMsgReference.getKey();

                StorageReference filePath = storageReference.child(msgPushId + "." + "jpg");

                uploadTask = filePath.putFile(fileUri);
                uploadTask.continueWithTask(new Continuation() {
                    @Override
                    public Object then(@NonNull Task task) throws Exception {
                        if (!task.isSuccessful()){
                            throw task.getException();
                        }
                        return filePath.getDownloadUrl();
                    }
                }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        if (task.isSuccessful()) {
                            Uri downloadUri = task.getResult();
                            Log.e("test", "Image: " + downloadUri);
                            if (downloadUri != null) {
                                myUrl = downloadUri.toString();
                            }

                            Map<String, String> msgTextBody = new HashMap<>();
                            msgTextBody.put("message", myUrl);
                            msgTextBody.put("name", fileUri.getLastPathSegment());
                            msgTextBody.put("type", check);
                            msgTextBody.put("from", msgSenderId);
                            msgTextBody.put("time", currentTime);
                            msgTextBody.put("to", msgReceiverId);
                            msgTextBody.put("messageId", msgPushId);

                            Map<String, Object> msgBodyDetails = new HashMap<String, Object>();
                            msgBodyDetails.put(msgSenderRef + "/" + msgPushId, msgTextBody);
                            msgBodyDetails.put(msgReceiverRef + "/" + msgPushId, msgTextBody);

                            rootReference.updateChildren(msgBodyDetails).addOnCompleteListener(new OnCompleteListener() {
                                @Override
                                public void onComplete(@NonNull Task task) {
                                    if (task.isSuccessful()) {
                                        Toast.makeText(ChatActivity.this, "Message Sent Successfully", Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(ChatActivity.this, "Error", Toast.LENGTH_SHORT).show();
                                    }
                                    edtSendMessage.setText("");
                                }
                            });
                        }
                    }
                });

            }
        }
    }

    private void lastSeenStatus(){
        rootReference.child("Users").child(msgReceiverId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.child("userStatus").hasChild("status")){
                    String status = snapshot.child("userStatus").child("status").getValue().toString();
                    String date = snapshot.child("userStatus").child("date").getValue().toString();
                    String time = snapshot.child("userStatus").child("time").getValue().toString();

                    if (status.equals("online")){
                        txtLastSeen.setText("Online");
                    }else if (status.equals("offline")){
                        txtLastSeen.setText("Last Seen: " + date + " " +time);
                    }
                }else {
                    txtLastSeen.setText("offline");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}